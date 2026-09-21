/* AquaTech motion layer: Lenis smooth scroll + GSAP choreography.
   Loads only when the vendor libs are present; every effect is skipped for
   users who ask for reduced motion. */
(() => {
  "use strict";

  const root = document.documentElement;
  const prefersReduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const gsapLib = window.gsap;
  const ScrollTriggerLib = window.ScrollTrigger;
  if (!gsapLib || !ScrollTriggerLib) return;

  const gsap = gsapLib;
  gsap.registerPlugin(ScrollTriggerLib);
  root.classList.add("motion-ready");

  const ease = "expo.out";
  const EASE_OUT = gsap.parseEase(ease);

  /* ---------- 1. Smooth scroll, synced to one rAF loop ---------- */
  let lenis = null;
  if (window.Lenis && !prefersReduced) {
    lenis = new window.Lenis({
      duration: 1.05,
      smoothWheel: true,
      touchMultiplier: 1.5,
      wheelMultiplier: 1,
    });
    lenis.on("scroll", ScrollTriggerLib.update);
    gsap.ticker.add((time) => lenis.raf(time * 1000));
    gsap.ticker.lagSmoothing(0);

    document.addEventListener("click", (event) => {
      const link = event.target.closest('a[href^="#"]');
      if (!link) return;
      const hash = link.getAttribute("href");
      if (!hash || hash === "#") return;
      const target = document.querySelector(hash);
      if (!target) return;
      event.preventDefault();
      lenis.scrollTo(target, { offset: -84 });
    });
  }

  /* ---------- 2. Header state on scroll ---------- */
  const header = document.querySelector(".site-header");
  if (header) {
    ScrollTriggerLib.create({
      start: 12,
      end: "max",
      onToggle: (self) => header.classList.toggle("is-scrolled", self.isActive),
    });
  }

  /* ---------- 3. Kinetic type ---------- */
  function splitChars(el) {
    if (el.dataset.splitDone === "1") return [];
    const text = el.textContent || "";
    el.textContent = "";
    const chars = [];
    [...text].forEach((ch) => {
      if (ch === " ") {
        el.appendChild(document.createTextNode(" "));
        return;
      }
      const span = document.createElement("span");
      span.className = "k-char";
      span.textContent = ch;
      el.appendChild(span);
      chars.push(span);
    });
    el.dataset.splitDone = "1";
    return chars;
  }

  function splitWords(el) {
    if (el.dataset.splitDone === "1") return [];
    const words = (el.textContent || "").split(/(\s+)/);
    el.textContent = "";
    const spans = [];
    words.forEach((word) => {
      if (/^\s+$/.test(word)) {
        el.appendChild(document.createTextNode(word));
        return;
      }
      const span = document.createElement("span");
      span.className = "k-word";
      span.textContent = word;
      el.appendChild(span);
      spans.push(span);
    });
    el.dataset.splitDone = "1";
    return spans;
  }

  const heroTitle = document.querySelector("[data-kinetic='chars']");
  const heroWords = [...document.querySelectorAll("[data-kinetic='words']")];

  if (prefersReduced) {
    /* keep markup untouched for reduced motion */
  } else if (heroTitle) {
    const chars = splitChars(heroTitle);
    const words = heroWords.flatMap((el) => splitWords(el));
    const timeline = gsap.timeline({ defaults: { ease } });

    timeline
      .from(".hero-brand-crest", { scale: 0.6, opacity: 0, duration: 0.9 })
      .from(chars, {
        yPercent: 118,
        rotate: () => gsap.utils.random(-14, 14),
        opacity: 0,
        duration: 0.85,
        stagger: { each: 0.045, from: "start" },
      }, "-=0.55")
      .from(".hero-kicker", { y: 12, opacity: 0, duration: 0.6 }, "-=0.7")
      .from(words, { yPercent: 60, opacity: 0, duration: 0.7, stagger: 0.03 }, "-=0.55")
      .from(".hero-actions .btn", { y: 18, opacity: 0, duration: 0.6, stagger: 0.08 }, "-=0.45")
      .from(".hero-meta", { y: 12, opacity: 0, duration: 0.5 }, "-=0.35")
      .from(".hero-showcase-card", { y: 34, opacity: 0, duration: 0.9 }, "-=0.85");
  }

  /* ---------- 4. Reveal vocabulary ---------- */
  const revealTargets = gsap.utils.toArray("[data-reveal], .reveal");
  if (!prefersReduced && revealTargets.length) {
    revealTargets.forEach((el) => {
      const delay = Number(el.dataset.delay || 0);
      gsap.from(el, {
        y: 26,
        opacity: 0,
        duration: 0.9,
        ease,
        delay,
        scrollTrigger: { trigger: el, start: "top 88%", once: true },
      });
    });
  }

  /* ---------- 5. Parallax for decorative layers ---------- */
  if (!prefersReduced) {
    gsap.utils.toArray("[data-parallax]").forEach((el) => {
      const strength = Number(el.dataset.parallax || 14);
      gsap.to(el, {
        yPercent: -strength,
        ease: "none",
        scrollTrigger: { trigger: el, start: "top bottom", end: "bottom top", scrub: true },
      });
    });
  }

  /* ---------- 6. Magnetic buttons ---------- */
  if (!prefersReduced && window.matchMedia("(hover: hover)").matches) {
    document.querySelectorAll("[data-magnetic]").forEach((el) => {
      const strength = Number(el.dataset.magnetic || 8);
      let raf = 0;
      let tx = 0;
      let ty = 0;
      let cx = 0;
      let cy = 0;
      const render = () => {
        cx += (tx - cx) * 0.18;
        cy += (ty - cy) * 0.18;
        el.style.transform = `translate3d(${cx.toFixed(2)}px, ${cy.toFixed(2)}px, 0)`;
        if (Math.abs(tx - cx) > 0.1 || Math.abs(ty - cy) > 0.1) {
          raf = requestAnimationFrame(render);
        } else {
          raf = 0;
          if (tx === 0 && ty === 0) el.style.transform = "";
        }
      };
      const kick = () => {
        if (!raf) raf = requestAnimationFrame(render);
      };
      el.addEventListener("pointermove", (event) => {
        const rect = el.getBoundingClientRect();
        tx = ((event.clientX - rect.left) / rect.width - 0.5) * strength * 2;
        ty = ((event.clientY - rect.top) / rect.height - 0.5) * strength * 2;
        kick();
      });
      el.addEventListener("pointerleave", () => {
        tx = 0;
        ty = 0;
        kick();
      });
    });
  }

  /* ---------- 7. Section rail progress ---------- */
  const progressBar = document.querySelector("[data-scroll-progress]");
  if (progressBar) {
    gsap.to(progressBar, {
      scaleX: 1,
      ease: "none",
      scrollTrigger: { start: 0, end: "max", scrub: 0.3 },
    });
  }

  window.__aquaMotion = { lenis, ease: EASE_OUT, gsap };
})();
