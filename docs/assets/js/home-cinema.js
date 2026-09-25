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

  const splitWords = (el) => {
    const units = [];
    Array.from(el.childNodes).forEach((node) => {
      if (node.nodeType === 3) {
        const words = node.textContent.split(/\s+/).filter(Boolean);
        if (!words.length) {
          node.remove();
          return;
        }
        const frag = document.createDocumentFragment();
        words.forEach((word) => {
          const outer = document.createElement("span");
          outer.className = "head-split";
          const inner = document.createElement("span");
          inner.className = "head-split-in";
          inner.textContent = word;
          outer.appendChild(inner);
          frag.appendChild(outer);
          frag.appendChild(document.createTextNode(" "));
          units.push(inner);
        });
        node.replaceWith(frag);
      } else if (node.nodeType === 1) {
        const inner = document.createElement("span");
        inner.className = "head-split-in";
        inner.textContent = node.textContent;
        node.textContent = "";
        node.appendChild(inner);
        node.classList.add("head-split");
        units.push(inner);
      }
    });
    return units;
  };

  const setupHeadlines = (gsap, ScrollTrigger) => {
    document.querySelectorAll(".section-head h2, .section-head-split h2, .bento-section h2, .story-title").forEach((el) => {
      if (el.dataset.split === "1") return;
      el.dataset.split = "1";
      const units = splitWords(el);
      if (!units.length) return;
      gsap.from(units, {
        yPercent: 108,
        duration: 0.9,
        ease: "expo.out",
        stagger: 0.045,
        scrollTrigger: { trigger: el, start: "top 88%", once: true },
      });
    });
  };

  const setupProgress = () => {
    const bar = document.querySelector("[data-scroll-progress]");
    if (!bar) return;
    let queued = false;
    const update = () => {
      queued = false;
      const max = Math.max(1, document.documentElement.scrollHeight - window.innerHeight);
      const ratio = Math.min(1, Math.max(0, window.scrollY / max));
      bar.style.scale = ratio + " 1";
    };
    const request = () => {
      if (queued) return;
      queued = true;
      requestAnimationFrame(update);
    };
    window.addEventListener("scroll", request, { passive: true });
    window.addEventListener("resize", request, { passive: true });
    update();
  };

  const setupRodGallery = () => {
    const gallery = document.querySelector("[data-rod-gallery]");
    if (!gallery) return;
    const track = gallery.querySelector(".rod-ladder-track");
    const items = track ? Array.from(track.children) : [];
    const caption = document.querySelector("[data-rod-caption]");
    const counter = document.querySelector("[data-rod-count]");
    if (!track || !items.length) return;

    let active = -1;
    const setActive = (index) => {
      if (index === active || index < 0 || index >= items.length) return;
      active = index;
      items.forEach((item, i) => item.classList.toggle("is-active", i === index));
      if (caption) {
        const raw = items[index].getAttribute("title") || "";
        const parts = raw.split("·").map((s) => s.trim()).filter(Boolean);
        caption.textContent = parts.slice(0, 2).join(" · ");
      }
      if (counter) {
        counter.textContent = String(index + 1).padStart(2, "0") + " / " + String(items.length).padStart(2, "0");
      }
    };

    const sync = () => {
      const box = track.getBoundingClientRect();
      const center = box.left + box.width / 2;
      let best = 0;
      let bestDist = Infinity;
      items.forEach((item, i) => {
        const r = item.getBoundingClientRect();
        const d = Math.abs(r.left + r.width / 2 - center);
        if (d < bestDist) {
          bestDist = d;
          best = i;
        }
      });
      setActive(best);
    };

    setActive(0);
    items.forEach((item, i) => {
      item.addEventListener("click", () => {
        item.scrollIntoView({ behavior: "smooth", inline: "center", block: "nearest" });
        setActive(i);
      }, { passive: true });
    });

    let queued = false;
    track.addEventListener("scroll", () => {
      if (queued) return;
      queued = true;
      requestAnimationFrame(() => {
        queued = false;
        sync();
      });
    }, { passive: true });

    let drag = null;
    track.addEventListener("pointerdown", (event) => {
      if (event.pointerType === "mouse") {
        drag = { x: event.clientX, left: track.scrollLeft, moved: false };
        track.setPointerCapture(event.pointerId);
      }
    });
    track.addEventListener("pointermove", (event) => {
      if (!drag) return;
      const dx = event.clientX - drag.x;
      if (Math.abs(dx) > 4) drag.moved = true;
      track.scrollLeft = drag.left - dx;
    });
    const endDrag = () => {
      drag = null;
    };
    track.addEventListener("pointerup", endDrag);
    track.addEventListener("pointercancel", endDrag);
    track.addEventListener("pointerleave", endDrag);

    sync();
  };

  const setupScroll = (gsap, ScrollTrigger) => {
    gsap.registerPlugin(ScrollTrigger);

    setupMarquee();
    setupHero(gsap);
    setupProgress();
    setupHeadlines(gsap, ScrollTrigger);
    setupRodGallery();

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
            const next = Math.max(0, Math.min(panels.length - 1, Math.round(self.progress * (panels.length - 1))));
            if (next !== current) {
              current = next;
              activate(next);
            }
          },
          onLeaveBack: () => {
            current = 0;
            activate(0);
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
      setupProgress();
      setupRodGallery();
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
