package main

import (
	"archive/zip"
	"encoding/base64"
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
	userAgent = "AquaTechBootstrap/1.1"
)

// Prefer API + jsDelivr. raw.githubusercontent.com/.../main often lags tip for a long time.
var manifestURLs = []string{
	"https://aquatech.santcrail.workers.dev/bootstrap.json",
	"https://aquateche.store/bootstrap.json",
	"https://api.github.com/repos/Renfild/AquaTeche/contents/docs/bootstrap.json?ref=main",
	"https://cdn.jsdelivr.net/gh/Renfild/AquaTeche@main/docs/bootstrap.json",
	"https://raw.githubusercontent.com/Renfild/AquaTeche/main/docs/bootstrap.json",
}

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
	man, err := fetchBestManifest(manifestURLs)
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

	needUpdate := NeedsUpdate(localVer(verFile), man.Version, findLauncher(appDir, man.LauncherExe) != "")

	if needUpdate {
		ui.SetStatus(fmt.Sprintf("Скачиваем лаунчер %s…", man.Version))
		zipURL := ResolveZipURL(man.LauncherZip, man.ReleaseBase)
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
		_ = exec.Command("taskkill", "/F", "/IM", "AquaTechLauncher.exe").Run()
		time.Sleep(100 * time.Millisecond)
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

func versionKey(v string) (int, int, int, bool) {
	v = strings.TrimSpace(v)
	parts := strings.Split(v, ".")
	if len(parts) < 2 {
		return 0, 0, 0, false
	}
	nums := make([]int, 3)
	for i := 0; i < 3 && i < len(parts); i++ {
		n := 0
		for _, ch := range parts[i] {
			if ch < '0' || ch > '9' {
				break
			}
			n = n*10 + int(ch-'0')
		}
		nums[i] = n
	}
	return nums[0], nums[1], nums[2], true
}

func versionNewer(a, b string) bool {
	a1, a2, a3, aok := versionKey(a)
	b1, b2, b3, bok := versionKey(b)
	if !aok {
		return false
	}
	if !bok {
		return true
	}
	if a1 != b1 {
		return a1 > b1
	}
	if a2 != b2 {
		return a2 > b2
	}
	return a3 > b3
}

func fetchBestManifest(urls []string) (*manifest, error) {
	if len(urls) == 0 {
		return nil, fmt.Errorf("список URL пуст")
	}
	type res struct {
		man *manifest
		err error
	}
	ch := make(chan res, len(urls))
	for _, u := range urls {
		go func(url string) {
			man, err := fetchManifest(url)
			ch <- res{man: man, err: err}
		}(u)
	}

	var best *manifest
	var lastErr error
	for i := 0; i < len(urls); i++ {
		r := <-ch
		if r.err != nil {
			lastErr = r.err
			continue
		}
		if best == nil || versionNewer(r.man.Version, best.Version) {
			best = r.man
		}
	}
	if best == nil {
		if lastErr != nil {
			return nil, lastErr
		}
		return nil, fmt.Errorf("не удалось загрузить bootstrap.json")
	}
	return best, nil
}

func localVer(path string) string {
	b, err := os.ReadFile(path)
	if err != nil {
		return ""
	}
	return strings.TrimSpace(string(b))
}

func fetchManifest(url string) (*manifest, error) {
	// Bust intermediary caches (raw.githubusercontent / CDN) so version bumps apply immediately.
	bust := url
	if strings.Contains(url, "?") {
		bust = url + "&t=" + fmt.Sprintf("%d", time.Now().Unix())
	} else {
		bust = url + "?t=" + fmt.Sprintf("%d", time.Now().Unix())
	}
	req, err := http.NewRequest(http.MethodGet, bust, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("User-Agent", userAgent)
	req.Header.Set("Cache-Control", "no-cache")
	if strings.Contains(url, "api.github.com") {
		req.Header.Set("Accept", "application/vnd.github.raw")
	}
	client := &http.Client{Timeout: 10 * time.Second}
	res, err := client.Do(req)
	if err != nil {
		return nil, err
	}
	defer res.Body.Close()
	if res.StatusCode >= 400 {
		return nil, fmt.Errorf("HTTP %d", res.StatusCode)
	}
	body, err := io.ReadAll(res.Body)
	if err != nil {
		return nil, err
	}
	// Contents API without Accept: raw returns JSON envelope with base64 content.
	if strings.Contains(url, "api.github.com") && len(body) > 0 && body[0] == '{' {
		var envelope struct {
			Content  string `json:"content"`
			Encoding string `json:"encoding"`
		}
		if err := json.Unmarshal(body, &envelope); err == nil && envelope.Content != "" {
			enc := envelope.Encoding
			if enc == "" || enc == "base64" {
				cleaned := strings.ReplaceAll(envelope.Content, "\n", "")
				decoded, err := base64.StdEncoding.DecodeString(cleaned)
				if err != nil {
					return nil, err
				}
				body = decoded
			}
		}
	}
	var man manifest
	if err := json.Unmarshal(body, &man); err != nil {
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
	candidates := []string{name, "AquaTechLauncher.exe", "AquaTech.exe"}
	for _, cand := range candidates {
		if cand == "" {
			continue
		}
		direct := filepath.Join(root, cand)
		if st, err := os.Stat(direct); err == nil && !st.IsDir() {
			return direct
		}
	}
	var found string
	_ = filepath.Walk(root, func(path string, info os.FileInfo, err error) error {
		if err != nil || info.IsDir() {
			return nil
		}
		for _, cand := range candidates {
			if cand != "" && strings.EqualFold(info.Name(), cand) {
				found = path
				return io.EOF
			}
		}
		if strings.HasSuffix(strings.ToLower(info.Name()), ".exe") && found == "" {
			found = path
		}
		return nil
	})
	return found
}

func startDetached(exe, dir string) error {
	cmd := exec.Command(exe)
	cmd.Dir = dir
	cmd.SysProcAttr = &syscall.SysProcAttr{
		CreationFlags: 0x00000200, // CREATE_NEW_PROCESS_GROUP
	}
	return cmd.Start()
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
