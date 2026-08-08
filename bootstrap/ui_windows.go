package main

import (
	"runtime"
	"syscall"
	"unsafe"
)

var (
	user32                       = syscall.NewLazyDLL("user32.dll")
	kernel32                     = syscall.NewLazyDLL("kernel32.dll")
	gdi32                        = syscall.NewLazyDLL("gdi32.dll")
	procRegisterClassExW         = user32.NewProc("RegisterClassExW")
	procCreateWindowExW          = user32.NewProc("CreateWindowExW")
	procShowWindow               = user32.NewProc("ShowWindow")
	procUpdateWindow             = user32.NewProc("UpdateWindow")
	procDestroyWindow            = user32.NewProc("DestroyWindow")
	procDefWindowProcW           = user32.NewProc("DefWindowProcW")
	procGetMessageW              = user32.NewProc("GetMessageW")
	procTranslateMessage         = user32.NewProc("TranslateMessage")
	procDispatchMessageW         = user32.NewProc("DispatchMessageW")
	procPostQuitMessage          = user32.NewProc("PostQuitMessage")
	procSetWindowTextW           = user32.NewProc("SetWindowTextW")
	procSendMessageW             = user32.NewProc("SendMessageW")
	procMessageBoxW              = user32.NewProc("MessageBoxW")
	procGetModuleHandleW         = kernel32.NewProc("GetModuleHandleW")
	procLoadCursorW              = user32.NewProc("LoadCursorW")
	procPostMessageW             = user32.NewProc("PostMessageW")
	procSetTimer                 = user32.NewProc("SetTimer")
	procKillTimer                = user32.NewProc("KillTimer")
	procInvalidateRect           = user32.NewProc("InvalidateRect")
	procBeginPaint               = user32.NewProc("BeginPaint")
	procEndPaint                 = user32.NewProc("EndPaint")
	procFillRect                 = user32.NewProc("FillRect")
	procCreateSolidBrush         = gdi32.NewProc("CreateSolidBrush")
	procDeleteObject             = gdi32.NewProc("DeleteObject")
	procSetBkMode                = gdi32.NewProc("SetBkMode")
	procSetTextColor             = gdi32.NewProc("SetTextColor")
	procTextOutW                 = gdi32.NewProc("TextOutW")
	procGetClientRect            = user32.NewProc("GetClientRect")
)

const (
	WS_OVERLAPPED     = 0x00000000
	WS_CAPTION        = 0x00C00000
	WS_SYSMENU        = 0x00080000
	WS_VISIBLE        = 0x10000000
	WS_MINIMIZEBOX    = 0x00020000
	CW_USEDEFAULT     = 0x80000000
	SW_SHOW           = 5
	WM_DESTROY        = 0x0002
	WM_PAINT          = 0x000F
	WM_TIMER          = 0x0113
	WM_CLOSE          = 0x0010
	WM_USER           = 0x0400
	WM_SETSTATUS      = WM_USER + 1
	WM_SETPROGRESS    = WM_USER + 2
	IDC_ARROW         = 32512
	COLOR_WINDOW      = 5
	MB_OK             = 0x00000000
	MB_ICONERROR      = 0x00000010
	TRANSPARENT       = 1
)

type wndClassEx struct {
	Size       uint32
	Style      uint32
	WndProc    uintptr
	ClsExtra   int32
	WndExtra   int32
	Instance   syscall.Handle
	Icon       syscall.Handle
	Cursor     syscall.Handle
	Background syscall.Handle
	MenuName   *uint16
	ClassName  *uint16
	IconSm     syscall.Handle
}

type point struct{ X, Y int32 }
type msg struct {
	Hwnd    syscall.Handle
	Message uint32
	WParam  uintptr
	LParam  uintptr
	Time    uint32
	Pt      point
}
type rect struct{ Left, Top, Right, Bottom int32 }
type paintStruct struct {
	Hdc         syscall.Handle
	Erase       int32
	RcPaint     rect
	Restore     int32
	IncUpdate   int32
	RgbReserved [32]byte
}

type progressUI struct {
	hwnd     syscall.Handle
	status   string
	progress int
	done     chan struct{}
}

func newProgressUI() *progressUI {
	ui := &progressUI{status: "AquaTech", progress: 0, done: make(chan struct{})}
	ready := make(chan struct{})
	go func() {
		runtime.LockOSThread()
		ui.run(ready)
	}()
	<-ready
	return ui
}

func (ui *progressUI) run(ready chan struct{}) {
	className, _ := syscall.UTF16PtrFromString("AquaTechBootstrapWnd")
	title, _ := syscall.UTF16PtrFromString("AquaTech")
	hInstance, _, _ := procGetModuleHandleW.Call(0)
	hCursor, _, _ := procLoadCursorW.Call(0, uintptr(IDC_ARROW))

	wndProc := syscall.NewCallback(ui.wndProc)
	wc := wndClassEx{
		Size:       uint32(unsafe.Sizeof(wndClassEx{})),
		WndProc:    wndProc,
		Instance:   syscall.Handle(hInstance),
		Cursor:     syscall.Handle(hCursor),
		Background: syscall.Handle(COLOR_WINDOW + 1),
		ClassName:  className,
	}
	procRegisterClassExW.Call(uintptr(unsafe.Pointer(&wc)))

	width, height := int32(460), int32(140)
	hwnd, _, _ := procCreateWindowExW.Call(
		0,
		uintptr(unsafe.Pointer(className)),
		uintptr(unsafe.Pointer(title)),
		uintptr(WS_OVERLAPPED|WS_CAPTION|WS_SYSMENU|WS_VISIBLE|WS_MINIMIZEBOX),
		uintptr(CW_USEDEFAULT),
		uintptr(CW_USEDEFAULT),
		uintptr(width),
		uintptr(height),
		0, 0, hInstance, 0,
	)
	ui.hwnd = syscall.Handle(hwnd)
	procShowWindow.Call(hwnd, SW_SHOW)
	procUpdateWindow.Call(hwnd)
	procSetTimer.Call(hwnd, 1, 50, 0)
	close(ready)

	var m msg
	for {
		ret, _, _ := procGetMessageW.Call(uintptr(unsafe.Pointer(&m)), 0, 0, 0)
		if int32(ret) <= 0 {
			break
		}
		procTranslateMessage.Call(uintptr(unsafe.Pointer(&m)))
		procDispatchMessageW.Call(uintptr(unsafe.Pointer(&m)))
	}
	close(ui.done)
}

func (ui *progressUI) wndProc(hwnd syscall.Handle, msg uint32, wParam, lParam uintptr) uintptr {
	switch msg {
	case WM_SETSTATUS:
		if lParam != 0 {
			ui.status = utf16PtrToString((*uint16)(unsafe.Pointer(lParam)))
		}
		procInvalidateRect.Call(uintptr(hwnd), 0, 1)
		return 0
	case WM_SETPROGRESS:
		ui.progress = int(wParam)
		if ui.progress < 0 {
			ui.progress = 0
		}
		if ui.progress > 100 {
			ui.progress = 100
		}
		procInvalidateRect.Call(uintptr(hwnd), 0, 1)
		return 0
	case WM_TIMER:
		procInvalidateRect.Call(uintptr(hwnd), 0, 1)
		return 0
	case WM_PAINT:
		var ps paintStruct
		hdc, _, _ := procBeginPaint.Call(uintptr(hwnd), uintptr(unsafe.Pointer(&ps)))
		var rc rect
		procGetClientRect.Call(uintptr(hwnd), uintptr(unsafe.Pointer(&rc)))

		bg, _, _ := procCreateSolidBrush.Call(0x00140B05) // deep ocean BGR
		procFillRect.Call(hdc, uintptr(unsafe.Pointer(&rc)), bg)
		procDeleteObject.Call(bg)

		// progress track
		bar := rect{Left: 24, Top: 70, Right: rc.Right - 24, Bottom: 92}
		track, _, _ := procCreateSolidBrush.Call(0x00231810)
		procFillRect.Call(hdc, uintptr(unsafe.Pointer(&bar)), track)
		procDeleteObject.Call(track)
		fillW := int32(float64(bar.Right-bar.Left) * float64(ui.progress) / 100.0)
		if fillW > 0 {
			fill := rect{Left: bar.Left, Top: bar.Top, Right: bar.Left + fillW, Bottom: bar.Bottom}
			accent, _, _ := procCreateSolidBrush.Call(0x00F2F200) // aqua-ish
			procFillRect.Call(hdc, uintptr(unsafe.Pointer(&fill)), accent)
			procDeleteObject.Call(accent)
		}

		procSetBkMode.Call(hdc, TRANSPARENT)
		procSetTextColor.Call(hdc, 0x00E8F0FF)
		status, _ := syscall.UTF16FromString(ui.status)
		procTextOutW.Call(hdc, 24, 28, uintptr(unsafe.Pointer(&status[0])), uintptr(len(status)-1))
		pct, _ := syscall.UTF16FromString(itoa(ui.progress) + "%")
		procTextOutW.Call(hdc, 24, 100, uintptr(unsafe.Pointer(&pct[0])), uintptr(len(pct)-1))

		procEndPaint.Call(uintptr(hwnd), uintptr(unsafe.Pointer(&ps)))
		return 0
	case WM_CLOSE:
		// ignore close during install — user can wait
		return 0
	case WM_DESTROY:
		procKillTimer.Call(uintptr(hwnd), 1)
		procPostQuitMessage.Call(0)
		return 0
	}
	ret, _, _ := procDefWindowProcW.Call(uintptr(hwnd), uintptr(msg), wParam, lParam)
	return ret
}

func (ui *progressUI) SetStatus(s string) {
	if ui.hwnd == 0 {
		return
	}
	p, _ := syscall.UTF16PtrFromString(s)
	procSendMessageW.Call(uintptr(ui.hwnd), WM_SETSTATUS, 0, uintptr(unsafe.Pointer(p)))
}

func (ui *progressUI) SetProgress(p int) {
	if ui.hwnd == 0 {
		return
	}
	procSendMessageW.Call(uintptr(ui.hwnd), WM_SETPROGRESS, uintptr(p), 0)
}

func (ui *progressUI) Close() {
	if ui.hwnd != 0 {
		procDestroyWindow.Call(uintptr(ui.hwnd))
		ui.hwnd = 0
		<-ui.done
	}
}

func msgBox(title, text string) {
	t, _ := syscall.UTF16PtrFromString(title)
	b, _ := syscall.UTF16PtrFromString(text)
	procMessageBoxW.Call(0, uintptr(unsafe.Pointer(b)), uintptr(unsafe.Pointer(t)), MB_OK|MB_ICONERROR)
}

func utf16PtrToString(p *uint16) string {
	if p == nil {
		return ""
	}
	var s []uint16
	for {
		ch := *(*uint16)(unsafe.Pointer(uintptr(unsafe.Pointer(p)) + uintptr(len(s)*2)))
		if ch == 0 {
			break
		}
		s = append(s, ch)
	}
	return syscall.UTF16ToString(s)
}

func itoa(n int) string {
	if n == 0 {
		return "0"
	}
	neg := n < 0
	if neg {
		n = -n
	}
	var b [16]byte
	i := len(b)
	for n > 0 {
		i--
		b[i] = byte('0' + n%10)
		n /= 10
	}
	if neg {
		i--
		b[i] = '-'
	}
	return string(b[i:])
}
