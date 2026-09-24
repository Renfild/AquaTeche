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
    for (int i = 0; i < 5; i++) {
      value += amp * noise(p);
      p *= 2.04;
      amp *= 0.5;
    }
    return value;
  }

  void main() {
    vec2 uv = gl_FragCoord.xy / uRes.xy;
    float aspect = uRes.x / max(uRes.y, 1.0);
    vec2 p = vec2(uv.x * aspect, uv.y);

    float t = uTime * 0.11;

    vec2 mouse = vec2(uMouse.x * aspect, 1.0 - uMouse.y);
    float pull = smoothstep(0.62, 0.0, distance(p, mouse));
    p += (mouse - p) * pull * 0.045;

    vec2 q = vec2(
      fbm(p * 1.55 + vec2(t * 0.9, -t * 0.6)),
      fbm(p * 1.55 + vec2(5.2, 1.3) - t * 0.7));
    vec2 r = vec2(
      fbm(p * 2.15 + 2.9 * q + vec2(1.7, 9.2) + 0.13 * t),
      fbm(p * 2.15 + 2.9 * q + vec2(8.3, 2.8) - 0.11 * t));
    float f = fbm(p * 2.45 + 3.2 * r);

    float depth = smoothstep(0.05, 0.95, uv.y);

    vec3 lightTop = vec3(0.988, 0.980, 0.996);
    vec3 lightBottom = vec3(0.925, 0.902, 0.969);
    vec3 lightBase = mix(lightTop, lightBottom, depth);

    vec3 darkTop = vec3(0.043, 0.020, 0.075);
    vec3 darkBottom = vec3(0.012, 0.063, 0.094);
    vec3 darkBase = mix(darkTop, darkBottom, depth);

    vec3 base = mix(lightBase, darkBase, uTheme);

    float edge = length(vec2(dFdx(f), dFdy(f)));
    float sheen = smoothstep(0.010, 0.115, edge);

    float veins = pow(smoothstep(0.62, 0.96, f), 3.0);

    vec3 violet = mix(vec3(0.486, 0.227, 0.929), vec3(0.180, 0.831, 0.878), uTheme);
    vec3 magenta = mix(vec3(0.902, 0.000, 0.494), vec3(0.184, 0.878, 0.784), uTheme);

    vec2 s1c = vec2(0.84, 0.86) + 0.05 * vec2(sin(t * 1.7), cos(t * 1.3));
    vec2 s2c = vec2(0.14, 0.22) + 0.05 * vec2(cos(t * 1.1), sin(t * 1.5));
    float s1 = exp(-8.5 * dot(p - s1c, p - s1c));
    float s2 = exp(-7.0 * dot(p - s2c, p - s2c));

    vec3 col = base;
    col += sheen * mix(vec3(0.86, 0.84, 1.0), vec3(0.42, 0.72, 0.86), uTheme) * 0.48;
    col += veins * mix(vec3(0.72, 0.68, 0.98), vec3(0.12, 0.55, 0.72), uTheme) * 0.20;
    col += violet * s1 * 0.46;
    col += magenta * s2 * 0.13;
    col += magenta * pull * 0.04;

    float grain = hash(gl_FragCoord.xy * 0.5 + uTime) - 0.5;
    col += grain * 0.016;

    float vignette = smoothstep(0.52, 1.18, length((uv - 0.5) * vec2(aspect, 1.0)));
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
  };

  const state = {
    visible: true,
    mouseX: 0.62,
    mouseY: 0.7,
    targetX: 0.62,
    targetY: 0.7,
    theme: document.documentElement.getAttribute("data-theme") === "dark" ? 1 : 0,
    raf: 0,
    width: 0,
    height: 0,
  };

  function resize() {
    const mobile = window.matchMedia("(max-width: 700px)").matches;
    const scale = (mobile ? 0.4 : 0.55) * Math.min(window.devicePixelRatio || 1, 1.5);
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
    gl.uniform1f(uni.time, (now - start) / 1000);
    gl.uniform2f(uni.mouse, state.mouseX, state.mouseY);
    gl.uniform1f(uni.theme, state.theme);
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
