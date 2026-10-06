(function () {
  "use strict";

  var TROPHY = '<svg viewBox="0 0 64 64" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M20 10h24v14a12 12 0 0 1-24 0z"/><path d="M20 14h-8v6a8 8 0 0 0 8 8M44 14h8v6a8 8 0 0 1-8 8"/><path d="M32 36v10M22 54h20M26 46h12v8H26z"/></svg>';
  var MONTHS = ["января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"];

  function esc(s) {
    return String(s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }

  function dateRu(iso) {
    var p = String(iso).split("-");
    if (p.length !== 3) return esc(iso);
    return Number(p[2]) + " " + MONTHS[Number(p[1]) - 1] + " " + p[0];
  }

  function art(kind) {
    if (kind === "chest") {
      return '<div class="u-art u-art--chest"><img src="assets/images/updates/chest-card.webp" alt="Пиратский сундук" loading="lazy" decoding="async" width="844" height="888"></div>';
    }
    if (kind === "cases") {
      return '<div class="u-art u-art--cases">'
        + '<img class="c2" src="assets/images/cases/steam.png" alt="Кейс III" loading="lazy" width="256" height="256">'
        + '<img class="c3" src="assets/images/cases/smeltery.png" alt="Кейс II" loading="lazy" width="256" height="256">'
        + '<img class="c1" src="assets/images/cases/flora.png" alt="Кейс IV" loading="lazy" width="256" height="256">'
        + '<span class="medal m1">1</span><span class="medal m2">2</span><span class="medal m3">3</span></div>';
    }
    if (kind === "keeper") {
      return '<div class="u-art u-art--keeper"><img src="assets/images/updates/beekeeper-idle.webp" alt="Пчеловод на спавне, анимация ожидания" loading="lazy" decoding="async" width="540" height="502"></div>';
    }
    if (kind === "keepnet") {
      return '<div class="u-art u-art--keepnet"><img src="assets/images/updates/keepnet-turn.webp" alt="Садок для рыбы" loading="lazy" decoding="async" width="460" height="430"></div>';
    }
    if (kind === "atlas") {
      return '<div class="u-art u-art--atlas"><img src="assets/images/updates/atlas-fish.webp" alt="Значки видов рыбы в атласе" loading="lazy" decoding="async" width="450" height="318"></div>';
    }
    if (kind === "booster") {
      return '<div class="u-art u-art--booster"><img src="assets/images/updates/booster.webp" alt="Бустер скупщика" loading="lazy" decoding="async" width="256" height="256"></div>';
    }
    return '<div class="u-art u-art--themes" aria-hidden="true">'
      + '<div class="mock mock--dark"><i></i><b></b><u></u><span><em></em><em></em><em></em></span></div>'
      + '<div class="mock mock--light"><i></i><b></b><u></u><span><em></em><em></em><em></em></span></div></div>';
  }

  function feature(f) {
    var points = (f.points || []).map(function (p) { return "<li>" + esc(p) + "</li>"; }).join("");
    var link = f.link ? '<a class="btn btn-secondary u-link" href="' + esc(f.link.href) + '">' + esc(f.link.label) + "</a>" : "";
    return '<article class="u-feature u-ac-' + esc(f.accent || "aqua") + ' reveal">'
      + '<div class="u-copy"><span class="u-tag">' + esc(f.tag) + "</span>"
      + "<h3>" + esc(f.title) + (f.titleAccent ? "<em>" + esc(f.titleAccent) + "</em>" : "") + "</h3>"
      + "<p>" + esc(f.text) + "</p>"
      + (points ? '<ul class="u-points">' + points + "</ul>" : "") + link + "</div>"
      + art(f.art) + "</article>";
  }

  function item(i) {
    var pic = i.img
      ? '<img src="' + esc(i.img) + '" alt="" loading="lazy" decoding="async" width="96" height="96">'
      : '<span class="u-item-icon">' + TROPHY + "</span>";
    return '<div class="u-item reveal">' + pic + "<b>" + esc(i.name) + "</b><span>" + esc(i.note) + "</span></div>";
  }

  function list(title, items) {
    if (!items || !items.length) return "";
    return '<div class="u-box reveal"><h3>' + esc(title) + "</h3><ul>" + items.map(function (i) { return "<li>" + esc(i) + "</li>"; }).join("") + "</ul></div>";
  }

  function bubbles() {
    var out = "";
    for (var n = 0; n < 14; n++) {
      var size = 6 + Math.round(Math.random() * 16);
      out += '<span style="left:' + Math.round(Math.random() * 100) + "%;width:" + size + "px;height:" + size
        + "px;animation-duration:" + (7 + Math.random() * 9).toFixed(1) + "s;animation-delay:-" + (Math.random() * 12).toFixed(1) + 's"></span>';
    }
    return out;
  }

  function render(u) {
    var hero = document.getElementById("u-hero");
    hero.innerHTML = '<div class="u-bubbles" aria-hidden="true">' + bubbles() + "</div>"
      + '<div class="container u-hero-inner"><div class="u-kicker">' + esc(u.title) + "</div><h1>" + esc(u.name) + "</h1>"
      + '<span class="u-date">Доступно с ' + dateRu(u.date) + "</span>"
      + '<p class="u-tagline">' + esc(u.tagline) + "</p></div>"
      + '<img class="u-hero-chest" src="' + esc(u.heroImg || "assets/images/updates/chest-hero.webp") + '" alt="" width="' + (u.heroImg ? 540 : 1084) + '" height="' + (u.heroImg ? 502 : 1006) + '" fetchpriority="high">'
      + '<a class="u-scroll" href="#u-main" aria-label="Листать вниз"><svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9l6 6 6-6"/></svg></a>';
    document.title = u.title + " · AquaTech";

    var html = '<section class="u-section" id="u-main"><div class="container"><h2 class="u-h2 reveal">Главные изменения</h2>'
      + (u.features || []).map(feature).join("") + "</div></section>";

    if (u.items && u.items.length) {
      html += '<section class="u-section" id="u-items"><div class="container"><h2 class="u-h2 reveal">Что добавилось</h2><div class="u-items">'
        + u.items.map(item).join("") + "</div></div></section>";
    }

    if (u.economy && u.economy.rows) {
      html += '<section class="u-section" id="u-economy"><div class="container"><h2 class="u-h2 reveal">' + esc(u.economy.title) + '</h2><div class="u-stats">'
        + u.economy.rows.map(function (r) {
          return '<div class="u-stat reveal"><span>' + esc(r.source) + "</span><b>" + esc(r.big) + "</b><p>" + esc(r.reward) + "</p><small>" + esc(r.rhythm) + "</small></div>";
        }).join("") + "</div></div></section>";
    }

    html += '<section class="u-section" id="u-fixes"><div class="container"><div class="u-cols">'
      + list("Исправления", u.fixes) + list("Дальше (в планах, без дат)", u.next) + "</div></div></section>";
    if (u.closing) html += '<section class="u-closing"><div class="container"><p>' + esc(u.closing) + "</p></div></section>";

    document.getElementById("u-body").innerHTML = html;
    observeReveals();
  }

  function observeReveals() {
    var nodes = document.querySelectorAll(".reveal");
    if (!("IntersectionObserver" in window) || window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      nodes.forEach(function (n) { n.classList.add("in"); });
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (e.isIntersecting) { e.target.classList.add("in"); io.unobserve(e.target); }
      });
    }, { rootMargin: "0px 0px -8% 0px", threshold: 0.08 });
    nodes.forEach(function (n) { io.observe(n); });
  }

  function renderArchive(all, currentId) {
    var others = all.filter(function (u) { return u.id !== currentId; });
    var wrap = document.getElementById("u-archive-wrap");
    var box = document.getElementById("u-archive");
    wrap.hidden = false;
    if (!others.length) {
      box.innerHTML = '<p class="u-first">Это первое обновление в архиве. Следующие появятся здесь.</p>';
      return;
    }
    box.innerHTML = others.map(function (u) {
      return '<a class="u-card" href="updates.html?u=' + encodeURIComponent(u.id) + '"><b>' + esc(u.title) + " " + esc(u.name) + "</b><small>" + dateRu(u.date) + "</small><span>" + esc(u.tagline) + "</span></a>";
    }).join("");
  }

  function fail() {
    document.getElementById("u-hero").innerHTML = '<div class="container u-hero-inner"><div class="u-kicker">Обновления</div><h1>Не удалось загрузить</h1><p class="u-tagline">Обновите страницу чуть позже.</p></div>';
  }

  var data = window.AQUATECH_UPDATES;
  var all = ((data && data.updates) || []).slice().sort(function (a, b) { return a.date < b.date ? 1 : -1; });
  if (!all.length) { fail(); return; }
  var wanted = new URLSearchParams(location.search).get("u");
  var current = all.filter(function (u) { return u.id === wanted; })[0] || all[0];
  render(current);
  renderArchive(all, current.id);
})();
