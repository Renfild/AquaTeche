package main

import (
	_ "embed"
	"math"
	"runtime"
	"syscall"
	"time"
	"unsafe"
)

//go:embed assets/syne-800.ttf
var fontSyne []byte

var (
	user32   = syscall.NewLazyDLL("user32.dll")
	kernel32 = syscall.NewLazyDLL("kernel32.dll")
	gdi32    = syscall.NewLazyDLL("gdi32.dll")
	dwmapi   = syscall.NewLazyDLL("dwmapi.dll")
	msimg32  = syscall.NewLazyDLL("msimg32.dll")

	procRegisterClassExW       = user32.NewProc("RegisterClassExW")
	procCreateWindowExW        = user32.NewProc("CreateWindowExW")
	procShowWindow             = user32.NewProc("ShowWindow")
	procUpdateWindow           = user32.NewProc("UpdateWindow")
	procDestroyWindow          = user32.NewProc("DestroyWindow")
	procDefWindowProcW         = user32.NewProc("DefWindowProcW")
	procGetMessageW            = user32.NewProc("GetMessageW")
	procTranslateMessage       = user32.NewProc("TranslateMessage")
	procDispatchMessageW       = user32.NewProc("DispatchMessageW")
	procPostQuitMessage        = user32.NewProc("PostQuitMessage")
	procSendMessageW           = user32.NewProc("SendMessageW")
	procPostMessageW           = user32.NewProc("PostMessageW")
	procMessageBoxW            = user32.NewProc("MessageBoxW")
	procLoadCursorW            = user32.NewProc("LoadCursorW")
	procInvalidateRect         = user32.NewProc("InvalidateRect")
	procBeginPaint             = user32.NewProc("BeginPaint")
	procEndPaint               = user32.NewProc("EndPaint")
	procGetClientRect          = user32.NewProc("GetClientRect")
	procGetWindowRect          = user32.NewProc("GetWindowRect")
	procGetSystemMetrics       = user32.NewProc("GetSystemMetrics")
	procSetTimer               = user32.NewProc("SetTimer")
	procGetDC                  = user32.NewProc("GetDC")
	procReleaseDC              = user32.NewProc("ReleaseDC")
	procGetModuleHandleW       = kernel32.NewProc("GetModuleHandleW")
	procCreateSolidBrush       = gdi32.NewProc("CreateSolidBrush")
	procDeleteObject           = gdi32.NewProc("DeleteObject")
	procSetBkMode              = gdi32.NewProc("SetBkMode")
	procSetTextColor           = gdi32.NewProc("SetTextColor")
	procTextOutW               = gdi32.NewProc("TextOutW")
	procCreateFontW            = gdi32.NewProc("CreateFontW")
	procSelectObject           = gdi32.NewProc("SelectObject")
	procGetStockObject         = gdi32.NewProc("GetStockObject")
	procRoundRect              = gdi32.NewProc("RoundRect")
	procCreateRoundRectRgn     = gdi32.NewProc("CreateRoundRectRgn")
	procSelectClipRgn          = gdi32.NewProc("SelectClipRgn")
	procAddFontMemResourceE    = gdi32.NewProc("AddFontMemResourceEx")
	procGetTextExtentPt32W     = gdi32.NewProc("GetTextExtentPoint32W")
	procCreateCompatibleDC     = gdi32.NewProc("CreateCompatibleDC")
	procCreateCompatibleBitmap = gdi32.NewProc("CreateCompatibleBitmap")
	procCreateDIBSection       = gdi32.NewProc("CreateDIBSection")
	procBitBlt                 = gdi32.NewProc("BitBlt")
	procGradientFill           = msimg32.NewProc("GradientFill")
	procDwmSetWindowAttribute  = dwmapi.NewProc("DwmSetWindowAttribute")
)

const (
	WS_POPUP     = 0x80000000
	WS_VISIBLE   = 0x10000000
	SW_SHOW      = 5
	WM_DESTROY   = 0x0002
	WM_USER      = 0x0400
	WM_PAINT     = 0x000F
	WM_TIMER     = 0x0113
	WM_ERASEBKGD = 0x0014
	WM_NCHITTEST = 0x0084
	WM_LBTN      = 0x0201
	WM_CLOSE     = 0x0010
	WM_APP_CLOSE = WM_USER + 3
	WM_SETSTATUS = WM_USER + 1
	WM_SETPRG    = WM_USER + 2
	IDC_ARROW    = 32512
	MB_OK        = 0x00000000
	MB_ICONERROR = 0x00000010
	TRANSPARENT  = 1
	HTCAPTION    = 2
	HTCLIENT     = 1
	NULL_PEN     = 8
	FW_BOLD      = 700
	CLEARTYPE_Q  = 5
	SRCCOPY      = 0x00CC0020
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

type bitmapinfoheader struct {
	Size, Width, Height          int32
	Planes, BitCount             uint16
	Compression, SizeImage       int32
	XPelsPerMeter, YPelsPerMeter int32
	ClrUsed, ClrImportant        int32
}

type bitmapinfo struct {
	Header bitmapinfoheader
	Colors [1]uint32
}

type trivertex struct {
	X, Y                    int32
	Red, Green, Blue, Alpha uint16
}

type gradientRect struct{ UpperLeft, LowerRight uint32 }

// Layout (client size).
const (
	winW = 440
	winH = 230
	padX = 28
	barY = 150
	barH = 12
)

type progressUI struct {
	hwnd     syscall.Handle
	status   string
	target   int     // percent requested by main
	shown    float64 // percent the bar currently displays (eased)
	done     chan struct{}
	fTitle   syscall.Handle
	fBody    syscall.Handle
	fSmall   syscall.Handle
	fPct     syscall.Handle
	fClose   syscall.Handle
	bgDC     uintptr // baked liquid-glass backdrop
	frameDC  uintptr // double buffer: backdrop + dynamic, one BitBlt to screen
	frameBmp uintptr
}

func newProgressUI() *progressUI {
	ui := &progressUI{status: "AquaTech", done: make(chan struct{})}
	ready := make(chan struct{})
	go func() {
		runtime.LockOSThread()
		ui.run(ready)
	}()
	<-ready
	return ui
}

func (ui *progressUI) run(ready chan struct{}) {
	runtime.LockOSThread()

	className, _ := syscall.UTF16PtrFromString("AquaTechBootstrapWnd")
	title, _ := syscall.UTF16PtrFromString("AquaTech")
	hInstance, _, _ := procGetModuleHandleW.Call(0)
	hCursor, _, _ := procLoadCursorW.Call(0, uintptr(IDC_ARROW))

	wndProc := syscall.NewCallback(ui.wndProc)
	wc := wndClassEx{
		Size:      uint32(unsafe.Sizeof(wndClassEx{})),
		WndProc:   wndProc,
		Instance:  syscall.Handle(hInstance),
		Cursor:    syscall.Handle(hCursor),
		ClassName: className,
	}
	procRegisterClassExW.Call(uintptr(unsafe.Pointer(&wc)))

	// Center on the primary screen, slightly above middle.
	sw, _, _ := procGetSystemMetrics.Call(0)
	sh, _, _ := procGetSystemMetrics.Call(1)
	x := (int32(sw) - winW) / 2
	y := (int32(sh) - winH) * 2 / 5

	hwnd, _, _ := procCreateWindowExW.Call(
		0,
		uintptr(unsafe.Pointer(className)),
		uintptr(unsafe.Pointer(title)),
		uintptr(WS_POPUP|WS_VISIBLE),
		uintptr(uint32(x)),
		uintptr(uint32(y)),
		uintptr(winW),
		uintptr(winH),
		0, 0, hInstance, 0,
	)
	ui.hwnd = syscall.Handle(hwnd)
	ui.loadFonts()
	ui.applyRound()
	procShowWindow.Call(hwnd, SW_SHOW)
	procUpdateWindow.Call(hwnd)
	procSetTimer.Call(hwnd, 1, 30, 0) // drives easing + shimmer
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

// applyRound: rounded corners (Win11) + dark titlebar. The liquid-glass
// backdrop is baked per-frame into an opaque DIB (GDI text over real acrylic
// leaves ghost frames behind).
func (ui *progressUI) applyRound() {
	hwnd := uintptr(ui.hwnd)
	pref := int32(2) // DWMWCP_ROUND
	procDwmSetWindowAttribute.Call(hwnd, 33, uintptr(unsafe.Pointer(&pref)), 4)
	dark := int32(1)
	procDwmSetWindowAttribute.Call(hwnd, 20, uintptr(unsafe.Pointer(&dark)), 4)
}

func (ui *progressUI) loadFonts() {
	n := uint32(0)
	procAddFontMemResourceE.Call(
		uintptr(unsafe.Pointer(&fontSyne[0])),
		uintptr(len(fontSyne)),
		0,
		uintptr(unsafe.Pointer(&n)),
	)
	ui.fTitle = ui.createFont(-26, "Syne", FW_BOLD)
	ui.fBody = ui.createFont(-13, "Segoe UI", 400)
	ui.fSmall = ui.createFont(-11, "Segoe UI", 400)
	ui.fPct = ui.createFont(-15, "Segoe UI", FW_BOLD)
	ui.fClose = ui.createFont(-12, "Segoe UI Symbol", 400)
}

func (ui *progressUI) createFont(height int32, face string, weight int32) syscall.Handle {
	f, _ := syscall.UTF16PtrFromString(face)
	h, _, _ := procCreateFontW.Call(
		uintptr(height), 0, 0, 0,
		uintptr(weight), 0, 0, 0,
		1 /*DEFAULT_CHARSET*/, 0, 0, CLEARTYPE_Q, 0,
		uintptr(unsafe.Pointer(f)),
	)
	return syscall.Handle(h)
}

func (ui *progressUI) inCloseBox(cx, cy int32) bool {
	return cx > winW-44 && cx < winW-14 && cy > 6 && cy < 34
}

// ensureBackdrop bakes the liquid-glass backdrop once: vertical navy gradient
// with soft aqua/teal glows. Opaque, so every repaint fully overwrites the frame.
func (ui *progressUI) ensureBackdrop() {
	if ui.bgDC != 0 {
		return
	}
	hdcScreen, _, _ := procGetDC.Call(0)
	defer procReleaseDC.Call(0, hdcScreen)
	memdc, _, _ := procCreateCompatibleDC.Call(hdcScreen)

	bmi := bitmapinfo{}
	bmi.Header.Size = int32(unsafe.Sizeof(bmi.Header))
	bmi.Header.Width = winW
	bmi.Header.Height = -winH // top-down
	bmi.Header.Planes = 1
	bmi.Header.BitCount = 32

	var bits uintptr
	bmp, _, _ := procCreateDIBSection.Call(memdc, uintptr(unsafe.Pointer(&bmi)), 0, uintptr(unsafe.Pointer(&bits)), 0, 0)
	if bmp == 0 || bits == 0 {
		return
	}
	procSelectObject.Call(memdc, bmp)

	pix := unsafe.Slice((*uint32)(unsafe.Pointer(bits)), int(winW*winH))
	for y := int32(0); y < winH; y++ {
		t := float64(y) / float64(winH)
		r0 := 16.0 - 8.0*t
		g0 := 26.0 - 13.0*t
		b0 := 42.0 - 24.0*t
		for x := int32(0); x < winW; x++ {
			r, g, b := r0, g0, b0
			dx := float64(x - 60)
			dy := float64(y + 40)
			f := 1 - math.Sqrt(dx*dx+dy*dy)/340
			if f > 0 {
				f *= f * 0.30
				r += 45 * f
				g += 210 * f
				b += 230 * f
			}
			dx = float64(x - 410)
			dy = float64(y - 260)
			f = 1 - math.Sqrt(dx*dx+dy*dy)/380
			if f > 0 {
				f *= f * 0.30
				r += 10 * f
				g += 120 * f
				b += 160 * f
			}
			if r > 255 {
				r = 255
			}
			if g > 255 {
				g = 255
			}
			if b > 255 {
				b = 255
			}
			pix[int(y*winW+x)] = 0xFF000000 | uint32(b)<<16 | uint32(g)<<8 | uint32(r)
		}
	}
	ui.bgDC = memdc
	_ = bmp // kept alive by the selected memDC
}

// ensureFrame prepares the double buffer (backdrop-sized 32bpp bitmap).
func (ui *progressUI) ensureFrame() {
	if ui.frameDC != 0 {
		return
	}
	ui.ensureBackdrop()
	if ui.bgDC == 0 {
		return
	}
	fdc, _, _ := procCreateCompatibleDC.Call(ui.bgDC)
	fbmp, _, _ := procCreateCompatibleBitmap.Call(ui.bgDC, uintptr(winW), uintptr(winH))
	procSelectObject.Call(fdc, fbmp)
	ui.frameDC, ui.frameBmp = fdc, fbmp
}

func (ui *progressUI) wndProc(hwnd syscall.Handle, msg uint32, wParam, lParam uintptr) uintptr {
	switch msg {
	case WM_SETSTATUS:
		if lParam != 0 {
			ui.status = utf16PtrToString((*uint16)(unsafe.Pointer(lParam)))
		}
		procInvalidateRect.Call(uintptr(hwnd), 0, 0)
		return 0
	case WM_SETPRG:
		ui.target = int(wParam)
		if ui.target < 0 {
			ui.target = 0
		}
		if ui.target > 100 {
			ui.target = 100
		}
		return 0
	case WM_TIMER:
		// Ease the bar toward the target and advance the shimmer.
		ui.shown += (float64(ui.target) - ui.shown) * 0.18
		if math.Abs(ui.shown-float64(ui.target)) < 0.3 {
			ui.shown = float64(ui.target)
		}
		procInvalidateRect.Call(uintptr(hwnd), 0, 0)
		return 0
	case WM_ERASEBKGD:
		return 1
	case WM_NCHITTEST:
		var wr rect
		procGetWindowRect.Call(uintptr(hwnd), uintptr(unsafe.Pointer(&wr)))
		cx := int32(int16(lParam&0xFFFF)) - wr.Left
		cy := int32(int16((lParam>>16)&0xFFFF)) - wr.Top
		if ui.inCloseBox(cx, cy) {
			return HTCLIENT
		}
		return HTCAPTION
	case WM_LBTN:
		cx := int32(int16(lParam & 0xFFFF))
		cy := int32(int16((lParam >> 16) & 0xFFFF))
		if ui.inCloseBox(cx, cy) {
			procPostMessageW.Call(uintptr(hwnd), WM_CLOSE, 0, 0)
		}
		return 0
	case WM_PAINT:
		ui.paint(hwnd)
		return 0
	case WM_APP_CLOSE:
		procShowWindow.Call(uintptr(hwnd), 0)
		procDestroyWindow.Call(uintptr(hwnd))
		return 0
	case WM_CLOSE:
		procShowWindow.Call(uintptr(hwnd), 0)
		procDestroyWindow.Call(uintptr(hwnd))
		return 0
	case WM_DESTROY:
		procPostQuitMessage.Call(0)
		return 0
	}
	ret, _, _ := procDefWindowProcW.Call(uintptr(hwnd), uintptr(msg), wParam, lParam)
	return ret
}

func (ui *progressUI) paint(hwnd syscall.Handle) {
	ui.ensureFrame()
	if ui.frameDC == 0 {
		return
	}
	var ps paintStruct
	hdc, _, _ := procBeginPaint.Call(uintptr(hwnd), uintptr(unsafe.Pointer(&ps)))
	defer procEndPaint.Call(uintptr(hwnd), uintptr(unsafe.Pointer(&ps)))

	// 1) Backdrop in one blit.
	procBitBlt.Call(ui.frameDC, 0, 0, uintptr(winW), uintptr(winH), uintptr(ui.bgDC), 0, 0, SRCCOPY)

	procSetBkMode.Call(ui.frameDC, TRANSPARENT)

	// 2) Close glyph (top-right).
	procSelectObject.Call(ui.frameDC, uintptr(ui.fClose))
	procSetTextColor.Call(ui.frameDC, 0x00A39AB0)
	cross, _ := syscall.UTF16FromString("✕")
	procTextOutW.Call(ui.frameDC, winW-27, 12, uintptr(unsafe.Pointer(&cross[0])), uintptr(len(cross)-1))

	// 3) Wordmark + subtitle.
	procSelectObject.Call(ui.frameDC, uintptr(ui.fTitle))
	procSetTextColor.Call(ui.frameDC, 0x00FCF8FF)
	brand, _ := syscall.UTF16FromString("AquaTech")
	procTextOutW.Call(ui.frameDC, padX, 22, uintptr(unsafe.Pointer(&brand[0])), uintptr(len(brand)-1))
	procSelectObject.Call(ui.frameDC, uintptr(ui.fSmall))
	procSetTextColor.Call(ui.frameDC, 0x00A39AB0)
	sub, _ := syscall.UTF16FromString("УСТАНОВКА И ОБНОВЛЕНИЕ")
	procTextOutW.Call(ui.frameDC, padX+1, 60, uintptr(unsafe.Pointer(&sub[0])), uintptr(len(sub)-1))

	// 4) Status line (left) + big percent (right), same baseline.
	shownPct := ui.shown
	if shownPct < 0 {
		shownPct = 0
	}
	if shownPct > 100 {
		shownPct = 100
	}

	procSelectObject.Call(ui.frameDC, uintptr(ui.fBody))
	procSetTextColor.Call(ui.frameDC, 0x00EEE2D6)
	status, _ := syscall.UTF16FromString(ui.status)
	procTextOutW.Call(ui.frameDC, padX, 104, uintptr(unsafe.Pointer(&status[0])), uintptr(len(status)-1))

	procSelectObject.Call(ui.frameDC, uintptr(ui.fPct))
	procSetTextColor.Call(ui.frameDC, 0x00FFE15C)
	pctText := itoa(int(shownPct)) + "%"
	pct, _ := syscall.UTF16FromString(pctText)
	var size int64
	procGetTextExtentPt32W.Call(ui.frameDC, uintptr(unsafe.Pointer(&pct[0])), uintptr(len(pct)-1), uintptr(unsafe.Pointer(&size)))
	pctW := int32(int16(size & 0xFFFF))
	pctH := int32(int16((size >> 16) & 0xFFFF))
	procTextOutW.Call(ui.frameDC, uintptr(winW-padX-pctW), uintptr(104+16-pctH), uintptr(unsafe.Pointer(&pct[0])), uintptr(len(pct)-1))

	// 5) Progress: rounded track + eased aqua fill.
	pen, _, _ := procGetStockObject.Call(NULL_PEN)
	procSelectObject.Call(ui.frameDC, pen)
	track, _, _ := procCreateSolidBrush.Call(0x00261A12)
	var trackRc rect
	trackRc.Left, trackRc.Top, trackRc.Right, trackRc.Bottom = padX, barY, winW-padX, barY+barH
	procRoundRect.Call(ui.frameDC, uintptr(trackRc.Left), uintptr(trackRc.Top), uintptr(trackRc.Right), uintptr(trackRc.Bottom), uintptr(barH), uintptr(barH))
	procDeleteObject.Call(track)

	barW := trackRc.Right - trackRc.Left
	fillW := int32(float64(barW) * shownPct / 100.0)
	if fillW < 12 && shownPct > 0 {
		fillW = 12
	}
	if fillW > 0 {
		rgn, _, _ := procCreateRoundRectRgn.Call(
			uintptr(trackRc.Left), uintptr(trackRc.Top),
			uintptr(trackRc.Left+fillW), uintptr(trackRc.Bottom),
			uintptr(barH), uintptr(barH),
		)
		procSelectClipRgn.Call(ui.frameDC, rgn)
		verts := []trivertex{
			{X: trackRc.Left, Y: trackRc.Top, Red: 0x5C00, Green: 0xE100, Blue: 0xFF00, Alpha: 0},
			{X: trackRc.Left + fillW, Y: trackRc.Bottom, Red: 0x2200, Green: 0xC900, Blue: 0xE800, Alpha: 0},
		}
		grect := gradientRect{UpperLeft: 0, LowerRight: 1}
		procGradientFill.Call(ui.frameDC, uintptr(unsafe.Pointer(&verts[0])), 2, uintptr(unsafe.Pointer(&grect)), 1, 0)
		procSelectClipRgn.Call(ui.frameDC, 0)
		procDeleteObject.Call(rgn)
	}

	// 6) Footer hint.
	procSelectObject.Call(ui.frameDC, uintptr(ui.fSmall))
	procSetTextColor.Call(ui.frameDC, 0x0086715C)
	hint, _ := syscall.UTF16FromString("Ocean Skyblock · Minecraft 1.20.1")
	procTextOutW.Call(ui.frameDC, padX, winH-28, uintptr(unsafe.Pointer(&hint[0])), uintptr(len(hint)-1))

	// 7) Single blit to the screen: no flicker.
	procBitBlt.Call(hdc, 0, 0, uintptr(winW), uintptr(winH), uintptr(ui.frameDC), 0, 0, SRCCOPY)
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
	procSendMessageW.Call(uintptr(ui.hwnd), WM_SETPRG, uintptr(p), 0)
}

func (ui *progressUI) Close() {
	hwnd := ui.hwnd
	if hwnd == 0 {
		return
	}
	procShowWindow.Call(uintptr(hwnd), 0)
	procPostMessageW.Call(uintptr(hwnd), WM_APP_CLOSE, 0, 0)
	select {
	case <-ui.done:
	case <-time.After(500 * time.Millisecond):
	}
	ui.hwnd = 0
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
