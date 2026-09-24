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

  mat2 rot(float a) {
    float c = cos(a);
    float s = sin(a);
    return mat2(c, -s, s, c);
  }

  float smin(float a, float b, float k) {
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
  }

  float sdSphere(vec3 p, float r) {
    return length(p) - r;
  }

  float sdBox(vec3 p, vec3 b, float r) {
    vec3 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0) - r;
  }

  float scene(vec3 p) {
    float t = uTime * 0.22;

    vec3 a = p;
    a.xz *= rot(t * 0.35 + uScroll * 1.2);
    a.xy *= rot(0.42);
    float core = sdBox(a, vec3(0.46, 0.32, 0.22), 0.16);

    vec3 b = p - vec3(sin(t * 0.7) * 0.62, cos(t * 0.53) * 0.40, 0.26);
    b.xz *= rot(-t * 0.5);
    b.xy *= rot(0.9);
    float shard = sdBox(b, vec3(0.22, 0.11, 0.09), 0.07);

    vec3 c = p - vec3(cos(t * 0.42) * 0.70, sin(t * 0.61) * 0.32, -0.22);
    c.xy *= rot(t * 0.33);
    float bead = sdSphere(c, 0.21 + 0.02 * sin(t * 1.3));

    vec3 d = p - vec3(-0.40, 0.52, 0.46);
    d.xz *= rot(t * 0.28 + 1.1);
    float drop = sdSphere(d, 0.16);

    float shape = smin(core, shard, 0.22);
    shape = smin(shape, bead, 0.18);
    shape = smin(shape, drop, 0.14);
    return shape;
  }

  vec3 normalAt(vec3 p) {
    vec2 e = vec2(0.0016, 0.0);
    return normalize(vec3(
      scene(p + e.xyy) - scene(p - e.xyy),
      scene(p + e.yxy) - scene(p - e.yxy),
      scene(p + e.yyx) - scene(p - e.yyx)));
  }

  float softShadow(vec3 ro, vec3 rd) {
    float res = 1.0;
    float t = 0.06;
    for (int i = 0; i < 18; i++) {
      float h = scene(ro + rd * t);
      res = min(res, 9.0 * h / t);
      t += clamp(h, 0.03, 0.24);
      if (res < 0.02 || t > 3.2) break;
    }
    return clamp(res, 0.0, 1.0);
  }

  void main() {
    vec2 frag = gl_FragCoord.xy;
    vec2 uv = frag / uRes.xy;
    float aspect = uRes.x / max(uRes.y, 1.0);
    vec2 p = vec2((uv.x - 0.5) * aspect, uv.y - 0.5);

    float travel = uScroll * 2.4;
    float orbit = (uMouse.x - 0.5) * 0.7 + travel * 0.5;
    float lift = (uMouse.y - 0.5) * 0.4 - uScroll * 0.8;
    float shift = aspect > 1.35 ? 0.52 : 0.0;

    vec3 ro = vec3(sin(orbit) * 1.55 - shift, 0.52 + lift, 3.35 + cos(orbit) * 0.85 - travel * 0.35);
    vec3 target = vec3(-shift, -0.04 + uScroll * 0.55, 0.0);

    vec3 forward = normalize(target - ro);
    vec3 right = normalize(cross(forward, vec3(0.0, 1.0, 0.0)));
    vec3 up = cross(right, forward);
    vec3 rd = normalize(forward * 1.9 + right * p.x + up * p.y);

    float hit = 0.0;
    float depth = 0.0;
    for (int i = 0; i < 72; i++) {
      vec3 pos = ro + rd * depth;
      float d = scene(pos);
      if (d < 0.0012) { hit = 1.0; break; }
      depth += d * 0.68;
      if (depth > 7.0) break;
    }

    vec3 lightDir = normalize(vec3(-0.55, 0.78, 0.42));
    vec3 roseDir = normalize(vec3(0.72, 0.18, -0.55));

    vec3 lightTop = vec3(1.0, 0.980, 0.988);
    vec3 lightBottom = vec3(0.973, 0.925, 0.941);
    vec3 lightBase = mix(lightTop, lightBottom, smoothstep(-0.2, 0.55, p.y - uScroll * 0.2));

    vec3 darkTop = vec3(0.055, 0.031, 0.086);
    vec3 darkBottom = vec3(0.012, 0.055, 0.086);
    vec3 darkBase = mix(darkTop, darkBottom, smoothstep(-0.2, 0.55, p.y));

    vec3 bg = mix(lightBase, darkBase, uTheme);

    vec3 glowA = mix(vec3(0.973, 0.741, 0.831), vec3(0.180, 0.831, 0.878), uTheme);
    vec3 glowB = mix(vec3(0.827, 0.686, 0.965), vec3(0.486, 0.290, 0.902), uTheme);

    bg += glowA * exp(-4.4 * dot(p - vec2(0.72, 0.20), p - vec2(0.72, 0.20))) * mix(0.34, 0.40, uTheme);
    bg += glowB * exp(-5.0 * dot(p - vec2(-0.62, -0.24), p - vec2(-0.62, -0.24))) * mix(0.26, 0.32, uTheme);

    vec2 foot = p - vec2(shift * 0.62, -0.46 - uScroll * 0.12);
    bg *= 1.0 - exp(-7.0 * dot(foot, foot)) * mix(0.16, 0.42, uTheme);

    vec3 col = bg;

    if (hit > 0.5) {
      vec3 pos = ro + rd * depth;
      vec3 n = normalAt(pos);
      float fres = pow(1.0 - clamp(dot(n, -rd), 0.0, 1.0), 2.2);

      float diff = clamp(dot(n, lightDir), 0.0, 1.0);
      float sh = softShadow(pos + n * 0.02, lightDir);
      float spec = pow(clamp(dot(reflect(rd, n), lightDir), 0.0, 1.0), 64.0);
      float spec2 = pow(clamp(dot(reflect(rd, n), roseDir), 0.0, 1.0), 22.0);
      float rim = pow(clamp(dot(n, roseDir), 0.0, 1.0), 1.8);

      vec3 rose = mix(vec3(0.937, 0.478, 0.671), vec3(0.180, 0.831, 0.878), uTheme);
      vec3 violet = mix(vec3(0.639, 0.494, 0.918), vec3(0.478, 0.310, 0.886), uTheme);
      vec3 glassLight = mix(vec3(1.0, 0.988, 0.996), vec3(0.749, 0.937, 1.0), uTheme);

      vec3 body = mix(rose, violet, clamp(0.30 + 0.45 * n.y, 0.0, 1.0));
      body = mix(body, glassLight, 0.18 + 0.30 * diff);

      col = body * (0.72 + 0.28 * sh);
      col = mix(col, glassLight, fres * 0.62);
      col += glassLight * spec * 0.90 * sh;
      col += violet * spec2 * 0.30;
      col += rose * rim * 0.22;
    }

    float grain = fract(sin(dot(frag + uTime, vec2(12.9898, 78.233))) * 43758.5453) - 0.5;
    col += grain * 0.014;

    float vignette = smoothstep(0.46, 1.16, length(p));
    col *= 1.0 - vignette * 0.22;

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
  };

  const state = {
    visible: true,
    mouseX: 0.62,
    mouseY: 0.7,
    targetX: 0.62,
    targetY: 0.7,
    theme: document.documentElement.getAttribute("data-theme") === "dark" ? 1 : 0,
    scroll: 0,
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
    const maxScroll = Math.max(1, document.documentElement.scrollHeight - window.innerHeight);
    state.scroll += (Math.min(1, Math.max(0, window.scrollY / maxScroll)) - state.scroll) * 0.12;
    gl.uniform1f(uni.time, (now - start) / 1000);
    gl.uniform2f(uni.mouse, state.mouseX, state.mouseY);
    gl.uniform1f(uni.theme, state.theme);
    gl.uniform1f(uni.scroll, state.scroll);
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
