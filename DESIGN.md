# DESIGN.md — AquaTech Portal

> Тихий океан под глянцевым стеклом: бледно-лавандовое полотно, чёрный заголовок во всю страницу, маджента как единственный акцент.

## 1. Visual Theme & Atmosphere

**Style**: Editorial Glass — светлый минимализм с лавандовым 3D-рендером, вынесенным из референса aicm (@nateherkai, видео 7684323553537232142).

**Keywords**: editorial, pale lavender, ink black, magenta accent, serif italic accent, glass shards, huge whitespace, thin borders, pill geometry.

**Tone**: спокойно-дорого, дизайнерский, без игровой пестроты — NOT неон, не «список карточек с тенью», не чёрный фон.

**Feel**: как разворот журнала, на который случайно лег блёклый океанский свет.

**Interaction Tier**: главная `index.html` — **L3 (immersive)**; остальные 18 страниц + 404 — **L2 (smooth)**; игровые embed-оверлеи — без изменений, тёмные.

**Dependencies**: существующие вендоры `lenis.min.js`, `gsap.min.js`, `ScrollTrigger.min.js` + собственный WebGL2-шейдер. Новых зависимостей нет.

## 2. Color Palette & Roles

```css
:root {
  /* Backgrounds */
  --bg: #f7f5fb;            /* 247,245,251 — лавандовое полотно */
  --bg-2: #efebf7;
  --bg-3: #e7e1f4;
  --surface: #ffffff;
  --surface-2: #fbfaff;
  --surface-hover: #f2effa;

  /* Borders */
  --line: rgba(24, 16, 40, 0.1);
  --line-strong: rgba(24, 16, 40, 0.18);

  /* Text */
  --text: #0b0710;
  --muted: #5b5566;
  --label-secondary: #3f3a48;

  /* Accent */
  --accent: #e6007e;        /* 230,0,126 — микро-метки, ссылки, активное */
  --accent-2: #7c3aed;      /* 124,58,237 — фиолетовый hover/свечение */
  --accent-ink: #ffffff;
  --cta: #0b0710;           /* чернильная пилюля — главный CTA */
  --cta-hover: #2a1240;

  /* RGB helpers */
  --bg-rgb: 247, 245, 251;
  --accent-rgb: 230, 0, 126;
  --violet-rgb: 124, 58, 237;
  --ink-rgb: 11, 7, 16;

  /* Semantic */
  --success: #0f8a5f;
  --error: #cf2440;
  --warning: #b4761b;

  /* Depth */
  --shadow-sm: 0 2px 8px rgba(11, 7, 16, 0.04);
  --shadow: 0 18px 44px rgba(11, 7, 16, 0.08);
  --shadow-lg: 0 40px 90px rgba(60, 30, 110, 0.14);
  --serif: Georgia, "Times New Roman", Times, serif;
}
```

**Color Rules:**
- Любой цвет только через переменную; hex в компонентах запрещён.
- Маджента — только микро-метки, ссылки, активные состояния, точечные свечения. Никогда не заливать ею поверхности.
- На одном экране максимум один акцентный цвет + чернильный CTA.
- Тёмная тема живёт под `html[data-theme="dark"]` и сохраняет океанскую палитру (глубокий синий + бирюза), чтобы переключатель был осмысленным, а не инверсией.

## 3. Typography Rules

Локальные шрифты проекта: `Ubuntu` (300/400/500/700) и `Ubuntu Mono`. Внешний Google Fonts **не подключается** — это сервер Minecraft-комьюнити с лаунчером, лишний CDN-запрос и зависимость от сети недопустимы. Референсный serif-курсив воспроизводится системным стеком Georgia/Didot.

| Role | Font | Size | Weight | Line Height | Letter Spacing |
|------|------|------|--------|-------------|----------------|
| Hero H1 | Ubuntu | `clamp(2.9rem, 7.6vw, 7rem)` | 500 | 0.92 | -0.045em |
| Section H2 | Ubuntu | `clamp(2rem, 4.4vw, 3.4rem)` | 500 | 1.0 | -0.035em |
| Serif accent | Georgia italic | `1.15em` от базового | 400 | 1.15 | -0.01em |
| H3 | Ubuntu | 1.18rem | 700 | 1.25 | -0.02em |
| Body | Ubuntu | 1.0625rem | 400 | 1.6 | 0 |
| Micro-label | Ubuntu | 0.72rem | 700 | 1.2 | 0.16em, uppercase |
| Number | Ubuntu Mono | `clamp(2.6rem, 6vw, 4.6rem)` | 700 | 1 | -0.03em |

**Typography Rules:**
- Заголовки — не жирнее 700 и всегда с отрицательным трекингом.
- Микро-метки всегда uppercase + 0.16em, цвет `--accent`.
- Один курсивный serif-акцент на экран, максимум два.

**Text Decoration:** H1 — чистый чернильный без градиента и тени; микро-метки — цветом акцента; числа — моноширинные, без свечения.

**NEVER use**: Inter, Roboto, Syne, Figtree, emoji как иконки, подчёркивание ссылок, `text-shadow` на заголовках.

## 4. Component Stylings

### Buttons
```css
.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: .55rem;
  min-height: 48px; padding: .8rem 1.5rem; border-radius: 999px;
  font: 700 .95rem/1 var(--font); white-space: nowrap; cursor: pointer;
  transition: transform .2s var(--ease), background .18s ease, color .18s ease, box-shadow .2s ease;
}
.btn-primary { background: var(--cta); color: var(--accent-ink); box-shadow: var(--shadow-sm); }
.btn-primary:hover { background: var(--cta-hover); transform: translateY(-2px); box-shadow: var(--shadow); }
.btn-primary:active { transform: translateY(0) scale(.98); }
.btn-primary:disabled { opacity: .5; cursor: not-allowed; transform: none; }
.btn-secondary { background: var(--surface); color: var(--text); border: 1px solid var(--line-strong); }
.btn-secondary:hover { background: var(--surface-hover); border-color: var(--accent); }
.btn-secondary:focus-visible, .btn-primary:focus-visible { outline: 2px solid var(--accent); outline-offset: 3px; }
.btn-ghost { background: transparent; color: var(--muted); }
.btn-ghost:hover { color: var(--text); background: var(--fill); }
```

### Cards
```css
.card, .bento-card, .showcase-card {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 22px;
  box-shadow: var(--shadow-sm);
  transition: transform .25s var(--ease), box-shadow .25s var(--ease), border-color .25s ease;
}
.card:hover { transform: translateY(-4px); box-shadow: var(--shadow); border-color: var(--line-strong); }
```

### Spotlight (cursor-tracking light inside a card)
```css
.spotlight::before {
  content: ""; position: absolute; inset: 0; border-radius: inherit; pointer-events: none;
  background: radial-gradient(320px circle at var(--mx, 50%) var(--my, 0%), rgba(124, 58, 237, .10), transparent 62%);
  opacity: 0; transition: opacity .25s ease;
}
.spotlight:hover::before { opacity: 1; }
```

### Navigation
```css
.site-header { background: rgba(247, 245, 251, .78); backdrop-filter: blur(12px) saturate(1.1); border-bottom: 1px solid transparent; }
.site-header.is-scrolled { background: rgba(247, 245, 251, .92); border-bottom-color: var(--line); }
.nav-desktop a { color: var(--muted); font-weight: 500; }
.nav-desktop a:hover { color: var(--text); }
.nav-desktop a.active { color: var(--text); }
.nav-cta, .header-cta { background: var(--cta); color: var(--accent-ink); border-radius: 999px; }
.nav-cta:hover { background: var(--cta-hover); color: var(--accent-ink); }
```

### Links
```css
a.bento-link, .footer-links a { color: var(--muted); }
a.bento-link:hover, .footer-links a:hover { color: var(--accent); }
```

### Tags / Badges
```css
.tag, .micro-label {
  display: inline-flex; align-items: center; gap: .4rem;
  padding: .3rem .7rem; border-radius: 999px;
  background: var(--accent-soft); color: var(--accent);
  font: 700 .72rem/1 var(--font); letter-spacing: .12em; text-transform: uppercase;
}
.badge-plain { background: var(--surface-hover); color: var(--muted); }
```

### BEFORE/AFTER pill (light/dark switch, signature)
```css
.ba-toggle {
  display: inline-flex; padding: 4px; border-radius: 999px;
  background: var(--surface); border: 1px solid var(--line); box-shadow: var(--shadow-sm);
}
.ba-toggle button {
  min-height: 36px; padding: 0 1.1rem; border: 0; border-radius: 999px;
  background: transparent; color: var(--muted); font: 700 .78rem/1 var(--font); cursor: pointer;
}
.ba-toggle button[aria-pressed="true"] { background: var(--accent); color: var(--accent-ink); }
```

### Editorial card (reference pattern: label + serif italic + 3 строки)
```css
.editorial-card {
  background: var(--surface); border: 1px solid var(--line); border-radius: 26px;
  padding: clamp(1.6rem, 3vw, 2.6rem); box-shadow: var(--shadow);
}
.editorial-card h3 { font: italic 400 clamp(1.8rem, 3.6vw, 2.9rem)/1.1 var(--serif); color: var(--text); }
.editorial-card p { color: var(--muted); max-width: 34ch; }
```

### Browser mockup
```css
.browser-frame { border: 1px solid var(--line); border-radius: 18px; overflow: hidden; background: var(--surface); box-shadow: var(--shadow-lg); }
.browser-bar { display: flex; gap: .5rem; align-items: center; padding: .8rem 1rem; border-bottom: 1px solid var(--line); background: var(--surface-2); }
.browser-dot { width: 10px; height: 10px; border-radius: 50%; background: var(--line-strong); }
.browser-frame img { display: block; width: 100%; height: auto; }
```

## 5. Layout Principles

**Container**: max `1240px`, padding `clamp(1rem, 4vw, 2rem)`. Text-heavy variant: `68ch`.

**Spacing scale**: section `clamp(4.5rem, 10vw, 9rem)`; card grid gap `1.25rem`; card padding `1.4rem`; editorial card `clamp(1.6rem, 3vw, 2.6rem)`.

**Grid**:
```css
.grid-3 { display: grid; gap: 1.25rem; grid-template-columns: 1fr; }
@media (min-width: 760px) { .grid-3 { grid-template-columns: repeat(3, 1fr); } }
.story-grid { display: grid; gap: clamp(2rem, 5vw, 5rem); grid-template-columns: 1fr; }
@media (min-width: 980px) { .story-grid { grid-template-columns: minmax(0, .85fr) minmax(0, 1.15fr); } }
.bento-grid { display: grid; gap: 1rem; grid-template-columns: 1fr; }
@media (min-width: 900px) { .bento-grid { grid-template-columns: repeat(6, 1fr); } .bento-radar { grid-column: span 3; } .bento-trends { grid-column: span 3; } .bento-market, .bento-events { grid-column: span 3; } }
```

## 6. Depth & Elevation

| Level | Treatment | Use |
|-------|-----------|-----|
| Flat | без тени, только 1px `--line` | микро-метки, поля ввода, сегменты |
| Subtle | `0 2px 8px rgba(11,7,16,.04)` | карточки, пилюли, бейджи |
| Elevated | `0 18px 44px rgba(11,7,16,.08)` | hover-карточки, editorial-card |
| Cinematic | `0 40px 90px rgba(60,30,110,.14)` | browser-frame, pinned-сцены |

Тени — только холодно-фиолетовые (микс `--ink` и `--violet`), никаких чисто чёрных дымок.

## 7. Animation & Interaction

**Motion Philosophy**: один «тяжёлый» момент на экран, остальное — короткие transform/opacity; ничего не мельтешит.

**Tier**: L3 на главной, L2 на остальных.

### Dependencies
```html
<script src="assets/js/vendor/lenis.min.js" defer></script>
<script src="assets/js/vendor/gsap.min.js" defer></script>
<script src="assets/js/vendor/ScrollTrigger.min.js" defer></script>
<script src="assets/js/glass-hero.js" defer></script>
<script src="assets/js/home-cinema.js" defer></script>
```

### WebGL-фон (единственная тяжёлая сцена)
`glass-hero.js` — raw WebGL2 fullscreen triangle: FBM-стекло с преломлением, фиолетово-маджентовые световые штрихи, курсор тянет поле. Рендер в 0.55 внутреннего разрешения, пауза через `IntersectionObserver` и `visibilitychange`, полный отказ при `prefers-reduced-motion` и без WebGL2.

### Scroll-сцены главной
1. **Маск-reveal H1** — `clip-path` снизу вверх, слова со stagger 40 мс.
2. **Marquee** — бесконечная лента `УДОЧКИ · АВТОРЫБАЛ · АУКЦИОН · КЕЙСЫ ·`, чистый CSS `translateX`.
3. **Pin-swapstory** — левая колонка закрепляется, правая меняет 3 сцены (Плот → Удочки → Заводы) по scroll-scrub; ScrollTrigger `pin` + `scrub: 0.6`.
4. **Числовая вспышка** — 13 / 426 / 7 с count-up при входе в вьюпорт.
5. **Spotlight-карточки** — `--mx/--my` через rAF-throttled `pointermove`.
6. **Магнитный CTA** — `--magnetic` сдвиг до 6px, только при `matchMedia('(hover: hover)')`.
7. **BEFORE/AFTER** — пилюля переключает светлую/тёмную тему и синхронизируется с тумблером в шапке.

### Reduced Motion
```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after { animation: none !important; transition: none !important; }
  .hero-title span, .marquee-track { transform: none !important; clip-path: none !important; }
}
```
JS: `glass-hero.js` и `home-cinema.js` выходят на старте, если `matchMedia('(prefers-reduced-motion: reduce)')` совпал.

## 8. Do's and Don'ts

### Do
- Держать белый текст только на чернильной/маджентовой заливке.
- Оставлять воздух: между секциями не меньше 4.5rem.
- Использовать 1px `--line` вместо тяжёлых обводок.
- Один акцент на экран.
- Держать интерактивные зоны ≥ 44×44 px.
- Давать `focus-visible` каждому интерактивному элементу.
- Ставить `loading="lazy"` на всё, что ниже первого экрана.

### Don't
- ❌ Заливать страницу чистым белым `#ffffff` без полутонов — будет «стерильно» (это был прошлый провал).
- ❌ Использовать фиолетово-неоновые градиенты на чёрном (SaaS-клише, запрещено anti-slop).
- ❌ Больше одного WebGL-слоя на страницу.
- ❌ `filter: blur()` на движущихся элементах и `backdrop-filter` крупнее 14px.
- ❌ `ScrollTrigger pin` больше двух на страницу.
- ❌ Глобальная замена курсора.
- ❌ Эмодзи как иконки.
- ❌ Анимировать `width`/`height` в циклах.
- ❌ Оставлять читаемость текста на фоне 3D-сцены без подложки.

## 9. Responsive Behavior

| Name | Width | Key Changes |
|------|-------|-------------|
| Desktop | > 980px | полная сетка, pin-swapstory, bento 6 колонок |
| Tablet | 700–980px | bento 2 колонки, pin отключается, H1 ниже |
| Mobile | < 700px | одна колонка, marquee остаётся, spotlight выключен, WebGL на 0.4 scale |

**Touch Targets**: минимум 44×44 px, `.ba-toggle button` — 44px по высоте.

**Collapsing Strategy**: навигация → бургер (уже реализовано), pin-сцены → вертикальный стек, spotlight → выключен, count-up → сразу финальное значение.

```css
@media (max-width: 980px) {
  .story-grid { grid-template-columns: 1fr; }
  .story-pin { position: static; }
}
@media (max-width: 700px) {
  .hero-title { font-size: clamp(2.4rem, 12vw, 3.6rem); }
  .browser-frame { border-radius: 12px; }
}
```
