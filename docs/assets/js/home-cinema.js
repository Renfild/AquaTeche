(() => {
  "use strict";

  if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;

  const setTheme = (theme) => {
    const next = theme === "dark" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", next);
    try {
      localStorage.setItem("aquatech_theme", next);
    } catch {}
    document.querySelectorAll("[data-theme-toggle]").forEach((btn) => {
      btn.setAttribute("aria-label", next === "dark" ? "Включить светлую тему" : "Включить тёмную тему");
    });
  };

  document.querySelectorAll("[data-ba]").forEach((btn) => {
    btn.addEventListener("click", () => {
      setTheme(btn.dataset.ba);
      document.querySelectorAll("[data-ba]").forEach((other) => {
        other.setAttribute("aria-pressed", other === btn ? "true" : "false");
      });
    });
  });

  document.querySelectorAll("[data-ba]").forEach((btn) => {
    const current = document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";
    btn.setAttribute("aria-pressed", btn.dataset.ba === current ? "true" : "false");
  });

  const armHero = () => {
    const mega = document.querySelector("[data-hero-mega]");
    if (!mega) return;
    const units = [];
    Array.from(mega.childNodes).forEach((node) => {
      if (node.nodeType === 3) {
        const words = node.textContent.split(/\s+/).filter(Boolean);
        if (!words.length) {
          node.remove();
          return;
        }
        const frag = document.createDocumentFragment();
        words.forEach((word, i) => {
          const outer = document.createElement("span");
          outer.className = "hw";
          const inner = document.createElement("span");
          inner.className = "hw-in";
          inner.textContent = word;
          outer.appendChild(inner);
          frag.appendChild(outer);
          frag.appendChild(document.createTextNode(" "));
          units.push(inner);
        });
        node.replaceWith(frag);
      } else if (node.nodeType === 1) {
        const inner = document.createElement("span");
        inner.className = "hw-in";
        inner.textContent = node.textContent;
        node.textContent = "";
        node.appendChild(inner);
        node.after(document.createTextNode(" "));
        units.push(inner);
      }
    });
    mega.dataset.armed = "1";
    return units;
  };

  const setupHero = (gsap) => {
    const units = armHero();
    const kicker = document.querySelector("[data-hero-kicker]");
    const lead = document.querySelector("[data-hero-lead]");
    const actions = document.querySelectorAll("[data-hero-actions] > *");
    const meta = document.querySelector("[data-hero-meta]");
    const soft = [kicker, lead, meta].filter(Boolean);

    if (units && units.length) {
      const tl = gsap.timeline();
      tl.fromTo(units, { yPercent: 115 }, { yPercent: 0, duration: 1.05, ease: "expo.out", stagger: 0.05, clearProps: "transform" }, 0);
      if (kicker) tl.fromTo(kicker, { y: 16, opacity: 0 }, { y: 0, opacity: 1, duration: 0.7, ease: "expo.out", clearProps: "transform" }, 0.15);
      if (lead) tl.fromTo(lead, { y: 22, opacity: 0 }, { y: 0, opacity: 1, duration: 0.8, ease: "expo.out", clearProps: "transform" }, 0.42);
      if (actions.length) tl.fromTo(actions, { y: 18, opacity: 0 }, { y: 0, opacity: 1, duration: 0.7, ease: "expo.out", stagger: 0.07, clearProps: "transform" }, 0.56);
      if (meta) tl.fromTo(meta, { opacity: 0 }, { opacity: 1, duration: 0.8, ease: "power1.out", clearProps: "transform" }, 0.7);
    }

    soft.concat(Array.from(actions)).forEach((el) => {
      if (el) el.setAttribute("data-hero-fade", "");
    });

    window.setTimeout(() => {
      document.querySelectorAll("[data-hero-fade]").forEach((el) => {
        el.style.removeProperty("opacity");
        el.style.removeProperty("transform");
      });
      document.querySelectorAll(".hero-mega .hw-in").forEach((el) => {
        el.style.removeProperty("transform");
      });
    }, 2600);
  };

  const setupMarquee = () => {
    const track = document.querySelector("[data-marquee-track]");
    if (!track) return;
    if (track.dataset.cloned === "1") return;
    Array.from(track.children).forEach((item) => {
      const clone = item.cloneNode(true);
      clone.setAttribute("aria-hidden", "true");
      track.appendChild(clone);
    });
    track.dataset.cloned = "1";
  };

  const setupSpotlight = () => {
    if (!window.matchMedia("(hover: hover)").matches) return;
    document.querySelectorAll(".spotlight").forEach((card) => {
      let queued = false;
      let x = 50;
      let y = 0;
      const apply = () => {
        queued = false;
        card.style.setProperty("--mx", x + "%");
        card.style.setProperty("--my", y + "%");
      };
      card.addEventListener("pointermove", (event) => {
        const rect = card.getBoundingClientRect();
        x = ((event.clientX - rect.left) / rect.width) * 100;
        y = ((event.clientY - rect.top) / rect.height) * 100;
        if (!queued) {
          queued = true;
          requestAnimationFrame(apply);
        }
      }, { passive: true });
    });
  };

  const setupMagnetic = () => {
    if (!window.matchMedia("(hover: hover)").matches) return;
    document.querySelectorAll("[data-magnetic]").forEach((el) => {
      const strength = Number(el.dataset.magnetic) || 5;
      el.addEventListener("pointermove", (event) => {
        const rect = el.getBoundingClientRect();
        const dx = (event.clientX - (rect.left + rect.width / 2)) / (rect.width / 2);
        const dy = (event.clientY - (rect.top + rect.height / 2)) / (rect.height / 2);
        el.style.transform = `translate(${dx * strength}px, ${dy * strength}px)`;
      }, { passive: true });
      el.addEventListener("pointerleave", () => {
        el.style.transform = "";
      });
    });
  };

  const setupScroll = (gsap, ScrollTrigger) => {
    gsap.registerPlugin(ScrollTrigger);

    setupMarquee();
    setupHero(gsap);

    document.querySelectorAll("[data-count]").forEach((el) => {
      const target = Number(el.dataset.count) || 0;
      const obj = { value: 0 };
      ScrollTrigger.create({
        trigger: el,
        start: "top 88%",
        once: true,
        onEnter: () => {
          gsap.to(obj, {
            value: target,
            duration: 1.6,
            ease: "power2.out",
            snap: { value: 1 },
            onUpdate: () => {
              el.textContent = Math.round(obj.value).toLocaleString("ru-RU");
            },
          });
        },
      });
    });

    document.querySelectorAll("[data-float]").forEach((el) => {
      gsap.from(el, {
        y: 44,
        opacity: 0,
        duration: 1,
        ease: "power3.out",
        scrollTrigger: { trigger: el, start: "top 86%", once: true },
      });
    });

    const stage = document.querySelector("[data-story-stage]");
    const panels = Array.from(document.querySelectorAll("[data-story-panel]"));
    const counter = document.querySelector("[data-story-counter]");

    if (stage && panels.length > 1) {
      const activate = (index) => {
        panels.forEach((panel, i) => panel.classList.toggle("is-active", i === index));
        if (counter) counter.textContent = String(index + 1).padStart(2, "0");
        stage.dataset.active = String(index);
      };
      activate(0);

      if (window.matchMedia("(min-width: 981px)").matches) {
        let current = 0;
        ScrollTrigger.create({
          trigger: stage,
          start: "top 22%",
          endTrigger: stage,
          end: "+=" + Math.round(window.innerHeight * 0.85 * panels.length),
          pin: true,
          pinSpacing: true,
          scrub: 0.6,
          onUpdate: (self) => {
            const next = Math.min(panels.length - 1, Math.floor(self.progress * panels.length));
            if (next !== current) {
              current = next;
              activate(next);
            }
          },
        });
      } else {
        panels.forEach((panel) => panel.classList.add("is-active"));
      }
    }

    setupSpotlight();
    setupMagnetic();

    window.addEventListener("load", () => ScrollTrigger.refresh());
  };

  const boot = () => {
    const gsap = window.gsap;
    const ScrollTrigger = window.ScrollTrigger;
    if (!gsap || !ScrollTrigger) {
      setupMarquee();
      setupSpotlight();
      setupMagnetic();
      return;
    }
    setupScroll(gsap, ScrollTrigger);
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", boot, { once: true });
  } else {
    boot();
  }
})();
