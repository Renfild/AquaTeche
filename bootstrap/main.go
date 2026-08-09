package main

import (
	"archive/zip"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"syscall"
	"time"
)

// LoliLand-style bootstrap:
// small AquaTech.exe → downloads AquaTechLauncher.zip → extracts to %LOCALAPPDATA%\AquaTech\app → runs launcher.

const (
	// Stable URL in repo/Pages — points at current launcher zip on GitHub Releases.
	manifestURL = "https://raw.githubusercontent.com/Renfild/AquaTeche/main/docs/bootstrap.json"
	userAgent   = "AquaTechBootstrap/1.0"
)

type manifest struct {
	Version      string `json:"version"`
	LauncherZip  string `json:"launcher_zip"`  // URL or filename on same release
	LauncherExe  string `json:"launcher_exe"`  // relative path inside extract / app dir
	ReleaseBase  string `json:"release_base"`  // optional base URL for relative names
}

func main() {
	defer func() {
		if r := recover(); r != nil {
			msgBox("AquaTech", fmt.Sprintf("Критическая ошибка: %v", r))
		}
	}()

	ui := newProgressUI()
	ui.SetStatus("AquaTech — подготовка…")
	ui.SetProgress(2)

	root := filepath.Join(os.Getenv("LOCALAPPDATA"), "AquaTech")
	appDir := filepath.Join(root, "app")
	verFile := filepath.Join(root, "version.txt")
	logFile := filepath.Join(root, "bootstrap.log")
	_ = os.MkdirAll(root, 0o755)
	logf(logFile, "start %s", time.Now().Format(time.RFC3339))

	ui.SetStatus("Проверяем обновления…")
	ui.SetProgress(8)
	man, err := fetchManifest(manifestURL)
	if err != nil {
		logf(logFile, "manifest err: %v", err)
		// Offline warm path: launch existing install if present
		exe := findLauncher(appDir, "AquaTechLauncher.exe")
		if exe != "" {
			ui.SetStatus("Офлайн — запускаем установленный лаунчер…")
			ui.SetProgress(95)
			if err := startDetached(exe, appDir); err != nil {
				msgBox("AquaTech", "Не удалось запустить лаунчер:\n"+err.Error())
			}
			ui.Close()
			return
		}
		msgBox("AquaTech", "Нет интернета и лаунчер ещё не установлен.\n\n"+err.Error())
		ui.Close()
		return
	}
	logf(logFile, "manifest version=%s zip=%s", man.Version, man.LauncherZip)

	needUpdate := true
	if b, err := os.ReadFile(verFile); err == nil {
		if strings.TrimSpace(string(b)) == man.Version {
			if findLauncher(appDir, man.LauncherExe) != "" {
				needUpdate = false
			}
		}
	}

	if needUpdate {
		ui.SetStatus(fmt.Sprintf("Скачиваем лаунчер %s…", man.Version))
		zipURL := resolveURL(man)
		tmpZip := filepath.Join(root, "AquaTechLauncher.zip.part")
		finalZip := filepath.Join(root, "AquaTechLauncher.zip")
		if err := downloadFile(zipURL, tmpZip, func(p float64) {
			ui.SetProgress(10 + int(p*70))
			ui.SetStatus(fmt.Sprintf("Скачиваем лаунчер… %d%%", int(p*100)))
		}); err != nil {
			logf(logFile, "download err: %v", err)
			msgBox("AquaTech", "Не удалось скачать лаунчер:\n"+err.Error())
			ui.Close()
			return
		}
		_ = os.Remove(finalZip)
		if err := os.Rename(tmpZip, finalZip); err != nil {
			_ = copyFile(tmpZip, finalZip)
			_ = os.Remove(tmpZip)
		}

		ui.SetStatus("Распаковываем…")
		ui.SetProgress(85)
		stage := filepath.Join(root, "app_new")
		_ = os.RemoveAll(stage)
		if err := unzip(finalZip, stage); err != nil {
			logf(logFile, "unzip err: %v", err)
			msgBox("AquaTech", "Не удалось распаковать лаунчер:\n"+err.Error())
			ui.Close()
			return
		}
		// Prefer nested folder if zip contains AquaTechLauncher/
		src := stage
		if entries, _ := os.ReadDir(stage); len(entries) == 1 && entries[0].IsDir() {
			src = filepath.Join(stage, entries[0].Name())
		}
		_ = os.RemoveAll(appDir)
		if err := os.Rename(src, appDir); err != nil {
			if err := copyDir(src, appDir); err != nil {
				logf(logFile, "install err: %v", err)
				msgBox("AquaTech", "Не удалось установить лаунчер:\n"+err.Error())
				ui.Close()
				return
			}
			_ = os.RemoveAll(stage)
		} else {
			_ = os.RemoveAll(stage)
		}
		_ = os.WriteFile(verFile, []byte(man.Version+"\n"), 0o644)
		_ = os.Remove(finalZip)
		logf(logFile, "installed %s", man.Version)
	} else {
		ui.SetStatus("Лаунчер актуален")
		ui.SetProgress(90)
	}

	exeName := man.LauncherExe
	if exeName == "" {
		exeName = "AquaTechLauncher.exe"
	}
	exe := findLauncher(appDir, exeName)
	if exe == "" {
		msgBox("AquaTech", "Лаунчер установлен, но AquaTechLauncher.exe не найден.")
		ui.Close()
		return
	}

	ui.SetStatus("Запуск…")
	ui.SetProgress(98)
	if err := startDetached(exe, filepath.Dir(exe)); err != nil {
		msgBox("AquaTech", "Не удалось запустить лаунчер:\n"+err.Error())
		ui.Close()
		os.Exit(1)
		return
	}
	ui.SetProgress(100)
	time.Sleep(100 * time.Millisecond)
	ui.Close()
	os.Exit(0)
}

func resolveURL(man *manifest) string {
	u := strings.TrimSpace(man.LauncherZip)
	if strings.HasPrefix(u, "http://") || strings.HasPrefix(u, "https://") {
		return u
	}
	base := strings.TrimRight(strings.TrimSpace(man.ReleaseBase), "/")
	if base == "" {
		base = "https://github.com/Renfild/AquaTeche/releases/download/client-latest"
	}
	return base + "/" + strings.TrimLeft(u, "/")
}

func fetchManifest(url string) (*manifest, error) {
	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("User-Agent", userAgent)
	client := &http.Client{Timeout: 30 * time.Second}
	res, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer res.Body.Close()
	if res.StatusCode >= 400 {
		return nil, fmt.Errorf("HTTP %d", res.StatusCode)
	}
	var man manifest
	if err := json.NewDecoder(res.Body).Decode(&man); err != nil {
		return nil, err
	}
	if man.Version == "" || man.LauncherZip == "" {
		return nil, fmt.Errorf("некорректный bootstrap.json")
	}
	if man.LauncherExe == "" {
		man.LauncherExe = "AquaTechLauncher.exe"
	}
	return &man, nil
}

func downloadFile(url, dest string, progress func(float64)) error {
	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
		return err
	}
	req.Header.Set("User-Agent", userAgent)
	client := &http.Client{Timeout: 0}
	res, err := client.Do(req)
	if err != nil {
		return err
	}
	defer res.Body.Close()
	if res.StatusCode >= 400 {
		return fmt.Errorf("HTTP %d", res.StatusCode)
	}
	_ = os.MkdirAll(filepath.Dir(dest), 0o755)
	f, err := os.Create(dest)
	if err != nil {
		return err
	}
	defer f.Close()

	var written int64
	total := res.ContentLength
	buf := make([]byte, 256*1024)
	for {
		n, er := res.Body.Read(buf)
		if n > 0 {
			if _, err := f.Write(buf[:n]); err != nil {
				return err
			}
			written += int64(n)
			if progress != nil && total > 0 {
				progress(float64(written) / float64(total))
			}
		}
		if er == io.EOF {
			break
		}
		if er != nil {
			return er
		}
	}
	if progress != nil {
		progress(1)
	}
	return nil
}

func unzip(src, dest string) error {
	r, err := zip.OpenReader(src)
	if err != nil {
		return err
	}
	defer r.Close()
	if err := os.MkdirAll(dest, 0o755); err != nil {
		return err
	}
	for _, f := range r.File {
		name := filepath.Clean(f.Name)
		if strings.HasPrefix(name, "..") {
			continue
		}
		outPath := filepath.Join(dest, name)
		if !strings.HasPrefix(outPath, filepath.Clean(dest)+string(os.PathSeparator)) && outPath != filepath.Clean(dest) {
			continue
		}
		if f.FileInfo().IsDir() {
			_ = os.MkdirAll(outPath, 0o755)
			continue
		}
		if err := os.MkdirAll(filepath.Dir(outPath), 0o755); err != nil {
			return err
		}
		rc, err := f.Open()
		if err != nil {
			return err
		}
		w, err := os.OpenFile(outPath, os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0o644)
		if err != nil {
			rc.Close()
			return err
		}
		_, err = io.Copy(w, rc)
		w.Close()
		rc.Close()
		if err != nil {
			return err
		}
	}
	return nil
}

func findLauncher(root, name string) string {
	if name == "" {
		name = "AquaTechLauncher.exe"
	}
	direct := filepath.Join(root, name)
	if st, err := os.Stat(direct); err == nil && !st.IsDir() {
		return direct
	}
	var found string
	_ = filepath.Walk(root, func(path string, info os.FileInfo, err error) error {
		if err != nil || info.IsDir() {
			return nil
		}
		if strings.EqualFold(info.Name(), name) {
			found = path
			return io.EOF
		}
		return nil
	})
	return found
}

func startDetached(exe, dir string) error {
	cmd := exec.Command(exe)
	cmd.Dir = dir
	cmd.SysProcAttr = &syscall.SysProcAttr{
		CreationFlags: syscall.CREATE_NEW_PROCESS_GROUP | 0x00000008, // DETACHED_PROCESS
	}
	cmd.Stdout = nil
	cmd.Stderr = nil
	if err := cmd.Start(); err != nil {
		return err
	}
	if cmd.Process != nil {
		_ = cmd.Process.Release()
	}
	return nil
}

func copyFile(src, dst string) error {
	in, err := os.Open(src)
	if err != nil {
		return err
	}
	defer in.Close()
	out, err := os.Create(dst)
	if err != nil {
		return err
	}
	defer out.Close()
	_, err = io.Copy(out, in)
	return err
}

func copyDir(src, dst string) error {
	return filepath.Walk(src, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		rel, err := filepath.Rel(src, path)
		if err != nil {
			return err
		}
		target := filepath.Join(dst, rel)
		if info.IsDir() {
			return os.MkdirAll(target, 0o755)
		}
		if err := os.MkdirAll(filepath.Dir(target), 0o755); err != nil {
			return err
		}
		return copyFile(path, target)
	})
}

func logf(path, format string, args ...any) {
	f, err := os.OpenFile(path, os.O_CREATE|os.O_APPEND|os.O_WRONLY, 0o644)
	if err != nil {
		return
	}
	defer f.Close()
	fmt.Fprintf(f, format+"\n", args...)
}
