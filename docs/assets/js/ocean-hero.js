/* AquaTech ocean hero: raw WebGL2 caustics behind the homepage hero.
   No libraries. Renders at a reduced internal resolution, pauses when the hero
   leaves the viewport, and silently steps aside when WebGL is unavailable or
   the visitor asked for reduced motion. */
(() => {
  "use strict";

  const prefersReduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (prefersReduced) return;

  /* On pages without a canvas in the markup, mount one into the page hero.
     Kept to the hero block so long content pages never pay for the shader. */
  function resolveCanvas() {
    const existing = document.getElementById("ocean-hero");
    if (existing) return existing;
    const host = document.querySelector(".page-hero, .hero, .start-hero, .hero-card");
    if (!host) return null;
    const canvas = document.createElement("canvas");
    canvas.className = "hero-canvas";
    canvas.id = "ocean-hero";
    canvas.setAttribute("aria-hidden", "true");
    host.prepend(canvas);
    return canvas;
  }

  const canvas = resolveCanvas();
  if (!canvas) return;

  const gl = canvas.getContext("webgl2", {
    alpha: true,
    antialias: false,
    depth: false,
    stencil: false,
    powerPreference: "low-power",
    premultipliedAlpha: false,
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

  uniform vec2  uRes;
  uniform float uTime;
  uniform vec2  uMouse;

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
      p *= 2.03;
      amp *= 0.5;
    }
    return value;
  }

  // Layered caustics: bright filaments where two drifting gradient fields cancel.
  float caustics(vec2 uv, float t) {
    float a = fbm(uv * 2.4 + vec2(t * 0.05, -t * 0.035));
    float b = fbm(uv * 3.1 - vec2(t * 0.04, t * 0.06));
    float c = fbm(uv * 5.7 + vec2(-t * 0.09, t * 0.02));
    float ridges = 1.0 - abs(a - b) * 1.6;
    ridges = pow(max(ridges, 0.0), 3.4);
    float fine = pow(max(1.0 - abs(c - b) * 2.1, 0.0), 6.0);
    return ridges * 0.85 + fine * 0.45;
  }

  void main() {
    vec2 uv = gl_FragCoord.xy / uRes.xy;
    float aspect = uRes.x / uRes.y;
    vec2 p = vec2(uv.x * aspect, uv.y);

    // Cursor drag: pulls the field toward the pointer with a soft falloff.
    vec2 mouse = vec2(uMouse.x * aspect, 1.0 - uMouse.y);
    float pull = smoothstep(0.5, 0.0, distance(p, mouse)) * 0.04;

    float t = uTime * 0.28;
    float surface = caustics(p * 2.3 - vec2(0.0, t * 0.06) + pull, uTime);

    // Depth gradient: ink at the top, slightly lighter shelf toward the middle.
    float depth = smoothstep(0.12, 0.95, uv.y);
    vec3 deep  = vec3(0.006, 0.028, 0.050);
    vec3 shelf = vec3(0.020, 0.082, 0.135);
    vec3 teal  = vec3(0.176, 0.831, 0.878);
    vec3 colour = mix(deep, shelf, depth);

    // Caustic filaments, brighter near the light source (top-left).
    float lightFall = smoothstep(1.9, 0.15, distance(p, vec2(0.25, 1.0)));
    colour += teal * surface * (0.06 + lightFall * 0.11);

    // Vignette keeps the middle readable for text.
    float vignette = smoothstep(1.25, 0.3, distance(uv, vec2(0.45, 0.6)));
    colour *= 0.5 + vignette * 0.6;

    // Bottom fade into the page background so the section boundary disappears.
    float fade = smoothstep(0.0, 0.34, uv.y);
    colour = mix(vec3(0.004, 0.016, 0.028), colour, fade);

    float alpha = mix(0.97, 0.72, fade) * (0.82 + surface * 0.16);
    fragColor = vec4(colour, alpha);
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
  };

  const state = {
    visible: true,
    mouseX: 0.5,
    mouseY: 0.5,
    targetX: 0.5,
    targetY: 0.5,
    raf: 0,
    width: 0,
    height: 0,
  };

  function resize() {
    const scale = Math.min(window.devicePixelRatio || 1, 1.5) * 0.6;
    const width = canvas.clientWidth || window.innerWidth;
    const height = canvas.clientHeight || window.innerHeight;
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
    state.mouseX += (state.targetX - state.mouseX) * 0.06;
    state.mouseY += (state.targetY - state.mouseY) * 0.06;
    gl.uniform1f(uni.time, (now - start) / 1000);
    gl.uniform2f(uni.mouse, state.mouseX, state.mouseY);
    gl.drawArrays(gl.TRIANGLES, 0, 3);
    if (!canvas.classList.contains("is-on")) canvas.classList.add("is-on");
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

  const observer = new IntersectionObserver(
    (entries) => {
      state.visible = entries.some((entry) => entry.isIntersecting);
      if (state.visible) kick();
    },
    { threshold: 0.01 }
  );
  observer.observe(canvas);

  document.addEventListener("visibilitychange", () => {
    if (!document.hidden) kick();
  });

  window.addEventListener("resize", () => {
    resize();
    kick();
  }, { passive: true });

  kick();
})();
