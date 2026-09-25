(() => {
  "use strict";

  const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (reduced) return;

  const host = document.querySelector("[data-glass-hero]");
  if (!host) return;

  const canvas = document.createElement("canvas");
  canvas.className = "glass-canvas";
  canvas.setAttribute("aria-hidden", "true");
  host.appendChild(canvas);

  const gl = canvas.getContext("webgl2", {
    alpha: false,
    antialias: false,
    depth: false,
    stencil: false,
    powerPreference: "low-power",
  });
  if (!gl) {
    canvas.remove();
    return;
  }

  const VERT = `#version 300 es
  precision highp float;
  const vec2 P[3] = vec2[3](vec2(-1.0, -1.0), vec2(3.0, -1.0), vec2(-1.0, 3.0));
  void main() {
    gl_Position = vec4(P[gl_VertexID], 0.0, 1.0);
  }`;

  const FRAG = `#version 300 es
  precision highp float;
  out vec4 fragColor;

  uniform vec2 uRes;
  uniform float uTime;
  uniform vec2 uMouse;
  uniform float uTheme;
  uniform float uScroll;
  uniform vec3 uClick;

  float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
  }

  float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(
      mix(hash(i + vec2(0.0, 0.0)), hash(i + vec2(1.0, 0.0)), u.x),
      mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x),
      u.y);
  }

  float fbm(vec2 p) {
    float value = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
      value += amp * noise(p);
      p *= 2.07;
      amp *= 0.5;
    }
    return value;
  }

  float causticField(vec2 p, float t) {
    float a = fbm(p * 1.9 + vec2(t * 0.14, -t * 0.10));
    float b = fbm(p * 2.5 - vec2(t * 0.11, t * 0.15));
    float c = fbm(p * 4.6 + vec2(-t * 0.20, t * 0.06));
    float ridges = 1.0 - abs(a - b) * 1.7;
    ridges = pow(max(ridges, 0.0), 3.2);
    float fine = pow(max(1.0 - abs(c - b) * 2.2, 0.0), 6.0);
    return ridges * 0.9 + fine * 0.5;
  }

  void main() {
    vec2 frag = gl_FragCoord.xy;
    vec2 uv = frag / uRes.xy;
    float aspect = uRes.x / max(uRes.y, 1.0);
    vec2 p = vec2(uv.x * aspect, uv.y);
    float t = uTime;

    vec2 mouse = vec2(uMouse.x * aspect, 1.0 - uMouse.y);
    float near = smoothstep(0.75, 0.0, distance(p, mouse));

    vec2 flow = p;
    flow += vec2(sin(t * 0.21), cos(t * 0.17)) * 0.05;
    flow += (mouse - p) * near * 0.10;
    flow.y += uScroll * 0.85;
    flow.x += sin(flow.y * 1.6 + t * 0.3) * 0.06;

    float ripple = 0.0;
    if (uClick.z < 1.35) {
      float age = uClick.z;
      vec2 cp = vec2(uClick.x * aspect, 1.0 - uClick.y);
      float d = distance(p, cp);
      float ring = sin(d * 26.0 - age * 9.0) * exp(-d * 3.2) * exp(-age * 2.1);
      ripple = ring;
      flow += normalize(p - cp + 0.0001) * ring * 0.05;
    }

    float field = causticField(flow, t);

    vec3 aquaLight = vec3(0.541, 0.867, 0.925);
    vec3 aquaDark = vec3(0.043, 0.145, 0.208);
    vec3 aqua = mix(aquaDark, aquaLight, uTheme);

    vec3 roseLight = vec3(0.973, 0.796, 0.871);
    vec3 roseDark = vec3(0.290, 0.114, 0.216);
    vec3 rose = mix(roseDark, roseLight, uTheme);

    vec3 lightTop = vec3(1.0, 0.984, 0.990);
    vec3 lightBottom = vec3(0.973, 0.925, 0.941);
    vec3 lightBase = mix(lightTop, lightBottom, smoothstep(-0.1, 0.9, uv.y));

    vec3 deepTop = vec3(0.043, 0.075, 0.110);
    vec3 deepBottom = vec3(0.008, 0.031, 0.051);
    vec3 deepBase = mix(deepTop, deepBottom, smoothstep(-0.1, 0.9, uv.y));

    vec3 col = mix(lightBase, deepBase, uTheme);

    vec2 g1 = p - vec2(0.72, 0.80);
    vec2 g2 = p - vec2(0.16, 0.22);
    col += rose * exp(-3.0 * dot(g1, g1)) * mix(0.34, 0.26, uTheme);
    col += aqua * exp(-3.4 * dot(g2, g2)) * mix(0.26, 0.34, uTheme);

    float veins = smoothstep(0.02, 0.42, field);
    col += aqua * veins * mix(0.34, 0.36, uTheme);
    col += vec3(1.0) * pow(max(field - 0.58, 0.0), 1.7) * mix(0.16, 0.26, uTheme);
    col += aqua * near * 0.10;
    col += vec3(1.0, 0.98, 0.99) * ripple * 0.16 * mix(0.6, 1.0, uTheme);

    float bubbles = smoothstep(0.965, 1.0, noise(vec2(p.x * 9.0, p.y * 9.0 - t * 0.35)));
    col += mix(roseLight, aquaLight, uTheme) * bubbles * 0.12;

    float depth = smoothstep(0.0, 1.0, uv.y + uScroll * 0.25);
    col *= mix(1.0, 0.86, depth * (1.0 - uTheme));

    float grain = fract(sin(dot(frag + uTime, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    col += grain * 0.012;

    float vignette = smoothstep(0.44, 1.2, length(p * vec2(0.72, 0.86)));
    col *= 1.0 - vignette * 0.20;

    fragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
  }`;

  function compile(type, source) {
    const shader = gl.createShader(type);
    gl.shaderSource(shader, source);
    gl.compileShader(shader);
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
      gl.deleteShader(shader);
      return null;
    }
    return shader;
  }

  const vs = compile(gl.VERTEX_SHADER, VERT);
  const fs = compile(gl.FRAGMENT_SHADER, FRAG);
  if (!vs || !fs) {
    canvas.remove();
    return;
  }

  const program = gl.createProgram();
  gl.attachShader(program, vs);
  gl.attachShader(program, fs);
  gl.linkProgram(program);
  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
    canvas.remove();
    return;
  }
  gl.useProgram(program);

  const uni = {
    res: gl.getUniformLocation(program, "uRes"),
    time: gl.getUniformLocation(program, "uTime"),
    mouse: gl.getUniformLocation(program, "uMouse"),
    theme: gl.getUniformLocation(program, "uTheme"),
    scroll: gl.getUniformLocation(program, "uScroll"),
    click: gl.getUniformLocation(program, "uClick"),
  };

  const state = {
    visible: true,
    mouseX: 0.62,
    mouseY: 0.7,
    targetX: 0.62,
    targetY: 0.7,
    theme: document.documentElement.getAttribute("data-theme") === "dark" ? 1 : 0,
    scroll: 0,
    clickX: 0.5,
    clickY: 0.5,
    clickAt: 0,
    raf: 0,
    width: 0,
    height: 0,
  };

  function resize() {
    const mobile = window.matchMedia("(max-width: 700px)").matches;
    const scale = (mobile ? 0.24 : 0.34) * Math.min(window.devicePixelRatio || 1, 1.5);
    const width = canvas.clientWidth || host.clientWidth || window.innerWidth;
    const height = canvas.clientHeight || host.clientHeight || window.innerHeight;
    const w = Math.max(2, Math.round(width * scale));
    const h = Math.max(2, Math.round(height * scale));
    if (w === state.width && h === state.height) return;
    state.width = w;
    state.height = h;
    canvas.width = w;
    canvas.height = h;
    gl.viewport(0, 0, w, h);
    gl.uniform2f(uni.res, w, h);
  }

  const start = performance.now();

  function frame(now) {
    state.raf = 0;
    resize();
    state.mouseX += (state.targetX - state.mouseX) * 0.05;
    state.mouseY += (state.targetY - state.mouseY) * 0.05;
    state.theme += ((document.documentElement.getAttribute("data-theme") === "dark" ? 1 : 0) - state.theme) * 0.08;
    const maxScroll = Math.max(1, document.documentElement.scrollHeight - window.innerHeight);
    state.scroll += (Math.min(1, Math.max(0, window.scrollY / maxScroll)) - state.scroll) * 0.12;
    gl.uniform1f(uni.time, (now - start) / 1000);
    gl.uniform2f(uni.mouse, state.mouseX, state.mouseY);
    gl.uniform1f(uni.theme, state.theme);
    gl.uniform1f(uni.scroll, state.scroll);
    gl.uniform3f(uni.click, state.clickX, state.clickY, state.clickAt ? (now - state.clickAt) / 1000 : 99);
    gl.drawArrays(gl.TRIANGLES, 0, 3);
    canvas.classList.add("is-on");
    if (state.visible && !document.hidden) state.raf = requestAnimationFrame(frame);
  }

  function kick() {
    if (!state.raf && state.visible && !document.hidden) {
      state.raf = requestAnimationFrame(frame);
    }
  }

  window.addEventListener("pointermove", (event) => {
    state.targetX = event.clientX / window.innerWidth;
    state.targetY = event.clientY / window.innerHeight;
  }, { passive: true });

  const ripple = (event) => {
    const rect = host.getBoundingClientRect();
    state.clickX = (event.clientX - rect.left) / Math.max(1, rect.width);
    state.clickY = (event.clientY - rect.top) / Math.max(1, rect.height);
    state.clickAt = performance.now();
    kick();
  };

  host.addEventListener("pointerdown", ripple, { passive: true });

  const observer = new IntersectionObserver((entries) => {
    state.visible = entries.some((entry) => entry.isIntersecting);
    if (state.visible) kick();
  }, { threshold: 0.01 });
  observer.observe(canvas);

  document.addEventListener("visibilitychange", () => {
    if (!document.hidden) kick();
  });

  window.addEventListener("resize", () => {
    resize();
    kick();
  }, { passive: true });

  const themeWatcher = new MutationObserver(() => kick());
  themeWatcher.observe(document.documentElement, { attributes: true, attributeFilter: ["data-theme"] });

  kick();
})();
