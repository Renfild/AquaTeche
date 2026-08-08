#!/usr/bin/env python3
"""Generate AquaTech multi-page portal under docs/."""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"

SHELL = """<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>{title}</title>
  <meta name="description" content="{desc}" />
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@500;700;800&family=Sora:wght@400;500;600;700&display=swap" rel="stylesheet" />
  <link rel="stylesheet" href="assets/css/site.css" />
</head>
<body data-page="{page}">
  <div id="site-header"></div>
  {body}
  <div id="site-footer"></div>
  <script src="assets/js/site.js"></script>
</body>
</html>
"""

PAGES: dict[str, tuple[str, str, str]] = {}

PAGES["index.html"] = (
    "home",
    "AquaTech — океанский Minecraft 1.20.1",
    "Официальный сайт AquaTech: лаунчер, удочки StarCatcher, кейсы, донат и профили игроков.",
    """
  <section class="hero">
    <div class="hero-bg" aria-hidden="true"></div>
    <div class="container hero-inner">
      <div class="eyebrow reveal">Океанский модпак · 1.20.1 Forge</div>
      <h1 class="reveal reveal-delay-1">AquaTech — <span>глубины и неизведанность</span></h1>
      <p class="hero-lead reveal reveal-delay-2">Спавн на плоту посреди океана, 12 удочек StarCatcher, авторыбалка, кейсы и индустриальный прогресс. Скачай лаунчер — сборка приедет сама.</p>
      <div class="hero-actions reveal reveal-delay-3">
        <a class="btn btn-primary" data-download href="#">Скачать лаунчер · ~5 МБ</a>
        <a class="btn btn-secondary" href="register.html">Регистрация</a>
        <a class="btn btn-ghost" href="store.html">Магазин</a>
      </div>
      <div class="ip-box reveal reveal-delay-3" data-copy-ip>
        <div>
          <small>IP СЕРВЕРА</small>
          <strong>katherine-hydro.tun.ply.gg:31279</strong>
        </div>
        <div class="copy">СКОПИРОВАТЬ</div>
      </div>
    </div>
  </section>

  <section class="section">
    <div class="container">
      <div class="section-head reveal">
        <div>
          <h2>Что тебя ждёт</h2>
          <p>Сайт, лаунчер и сервер — один проект с прогрессией и сообществом.</p>
        </div>
        <a class="btn btn-secondary" href="start.html">Как начать</a>
      </div>
      <div class="grid-3">
        <a class="card card-link reveal" href="rods.html">
          <div class="feature-art rods"></div>
          <span class="tag">Прогрессия</span>
          <h3>12 удочек StarCatcher</h3>
          <p>От бамбука до легендарных удилищ с ресурсными пулами AquaTech.</p>
        </a>
        <a class="card card-link reveal reveal-delay-1" href="cases.html">
          <div class="feature-art cases"></div>
          <span class="tag gold">Награды</span>
          <h3>Кейсы и привилегии</h3>
          <p>Крути кейсы, копи монеты и открывай VIP–Ultimate на сервере.</p>
        </a>
        <a class="card card-link reveal reveal-delay-2" href="top.html">
          <div class="feature-art tech"></div>
          <span class="tag">Сообщество</span>
          <h3>Топы и профили</h3>
          <p>Онлайн, монеты, лайки — смотри лидеров и оформляй свой профиль.</p>
        </a>
      </div>
    </div>
  </section>

  <section class="section" style="padding-top:0">
    <div class="container grid-2">
      <div class="reveal">
        <div class="section-head">
          <div>
            <h2>Новости</h2>
            <p>Обновления лаунчера, сборки и сервера.</p>
          </div>
        </div>
        <div class="news-list">
          <a class="news-item" href="news.html">
            <time>8 августа 2026</time>
            <h3>Лаунчер 2.9.8 и CDN сборки</h3>
            <p>Маленький bootstrap ~5 МБ, обновления модов через CDN.</p>
          </a>
          <a class="news-item" href="news.html">
            <time>Июль 2026</time>
            <h3>StarCatcher loot-таблицы</h3>
            <p>Полная прогрессия ресурсных удочек и авторыбалка.</p>
          </a>
        </div>
      </div>
      <div class="card reveal">
        <span class="tag">Сервер</span>
        <h3>AquaTech Ocean Skyblock</h3>
        <p style="margin:.6rem 0 1rem;color:var(--muted)">Один основной мир: океан, плот 4×4, индустриальные моды и кастомный UI.</p>
        <div class="ip-box" data-copy-ip style="width:100%;justify-content:space-between">
          <div>
            <small>ПОДКЛЮЧЕНИЕ</small>
            <strong>katherine-hydro.tun.ply.gg:31279</strong>
          </div>
          <div class="copy">COPY</div>
        </div>
        <div style="margin-top:1rem;display:flex;gap:.6rem;flex-wrap:wrap">
          <a class="btn btn-aqua" href="start.html">Начать игру</a>
          <a class="btn btn-secondary" href="players.html">Игроки</a>
        </div>
      </div>
    </div>
  </section>
""",
)

PAGES["start.html"] = (
    "start",
    "Начать игру — AquaTech",
    "Скачай лаунчер AquaTech для Windows и зайди на сервер.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Старт</div>
      <h1>Начни приключение на AquaTech</h1>
      <p>Лаунчер сам скачает сборку. Тебе нужен только Windows и ~5 МБ на первый файл.</p>
      <div class="banner"></div>
    </div>
  </section>
  <section class="section" style="padding-top:1rem">
    <div class="container grid-2">
      <div class="card">
        <h3>1. Скачай лаунчер</h3>
        <p style="color:var(--muted);margin:.6rem 0 1rem">AquaTech.exe — маленький bootstrap. Полный лаунчер установится в %LOCALAPPDATA%\\AquaTech.</p>
        <a class="btn btn-primary" data-download href="#">Скачать для Windows</a>
      </div>
      <div class="card">
        <h3>2. Войди и играй</h3>
        <ul class="perk-list">
          <li>Укажи ник в лаунчере</li>
          <li>Дождись синхронизации модов</li>
          <li>Жми «Играть» — авто-вход на сервер</li>
          <li>IP: katherine-hydro.tun.ply.gg:31279</li>
        </ul>
        <div style="margin-top:1rem;display:flex;gap:.6rem;flex-wrap:wrap">
          <a class="btn btn-secondary" href="register.html">Создать профиль на сайте</a>
          <button class="btn btn-ghost" type="button" data-copy-ip>Скопировать IP</button>
        </div>
      </div>
    </div>
  </section>
""",
)

PAGES["store.html"] = (
    "store",
    "Магазин — AquaTech",
    "Привилегии VIP, Premium, Deluxe и Ultimate на сервере AquaTech.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Магазин</div>
      <h1>Привилегии сервера</h1>
      <p>Поддержи проект и получи бонусы на сервере. Оплата пока в демо-режиме — заявка сохраняется на ник из профиля.</p>
    </div>
  </section>
  <section class="section" style="padding-top:0">
    <div class="container grid-3">
      <div class="card">
        <span class="tag">VIP</span>
        <h3>VIP</h3>
        <ul class="perk-list">
          <li>Префикс в чате</li>
          <li>+1 дом /sethome</li>
          <li>Цветной ник</li>
        </ul>
        <div class="price">149 ₽ <small>/ мес</small></div>
        <button class="btn btn-secondary" style="margin-top:1rem;width:100%" data-buy="VIP" type="button">Купить</button>
      </div>
      <div class="card">
        <span class="tag">Premium</span>
        <h3>Premium</h3>
        <ul class="perk-list">
          <li>Всё из VIP</li>
          <li>Кейс в день</li>
          <li>Приоритет входа</li>
        </ul>
        <div class="price">299 ₽ <small>/ мес</small></div>
        <button class="btn btn-secondary" style="margin-top:1rem;width:100%" data-buy="Premium" type="button">Купить</button>
      </div>
      <div class="card">
        <span class="tag gold">Deluxe</span>
        <h3>Deluxe</h3>
        <ul class="perk-list">
          <li>Всё из Premium</li>
          <li>Рамка профиля</li>
          <li>Бонус к улову</li>
        </ul>
        <div class="price">599 ₽ <small>/ мес</small></div>
        <button class="btn btn-primary" style="margin-top:1rem;width:100%" data-buy="Deluxe" type="button">Купить</button>
      </div>
    </div>
    <div class="container" style="margin-top:1rem">
      <div class="card" style="display:flex;flex-wrap:wrap;gap:1rem;align-items:center;justify-content:space-between">
        <div>
          <span class="tag gold">Ultimate</span>
          <h3 style="margin:.4rem 0">Ultimate</h3>
          <p style="margin:0;color:var(--muted)">Максимум привилегий, уникальный бейдж профиля и поддержка разработки AquaTech.</p>
        </div>
        <div style="text-align:right">
          <div class="price" style="margin:0">1199 ₽ <small>/ мес</small></div>
          <button class="btn btn-aqua" style="margin-top:.8rem" data-buy="Ultimate" type="button">Купить Ultimate</button>
        </div>
      </div>
    </div>
  </section>
""",
)

PAGES["cases.html"] = (
    "cases",
    "Кейсы — AquaTech",
    "Открывай кейсы AquaTech и получай награды.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Награды</div>
      <h1>Кейсы AquaTech</h1>
      <p>Демо-рулетка на сайте. На сервере кейсы выдаются через игровые механики и донат.</p>
    </div>
  </section>
  <section class="section" style="padding-top:0">
    <div class="container grid-3">
      <div class="card">
        <h3>Океанский кейс</h3>
        <p style="color:var(--muted)">Базовые награды и монеты.</p>
        <button class="btn btn-secondary" style="margin-top:1rem;width:100%" data-open-case="Океанский кейс" type="button">Открыть</button>
      </div>
      <div class="card">
        <h3>Кейс рыбака</h3>
        <p style="color:var(--muted)">Бусты удочек и ресурсы.</p>
        <button class="btn btn-secondary" style="margin-top:1rem;width:100%" data-open-case="Кейс рыбака" type="button">Открыть</button>
      </div>
      <div class="card">
        <h3>Глубинный кейс</h3>
        <p style="color:var(--muted)">Редкие привилегии и рамки.</p>
        <button class="btn btn-primary" style="margin-top:1rem;width:100%" data-open-case="Глубинный кейс" type="button">Открыть</button>
      </div>
    </div>
  </section>
  <div class="modal" id="case-modal">
    <div class="modal-card">
      <h3>Результат</h3>
      <p id="case-result" style="color:var(--muted)">…</p>
      <button class="btn btn-secondary" style="margin-top:1rem" data-close-modal type="button">Закрыть</button>
    </div>
  </div>
""",
)

RODS = [
    ("T1", "Bamboo Rod", "Старт: булыжник, земля, саженцы, медь."),
    ("T2", "Humble Rod", "Медь, олово, железо, уголь, титан."),
    ("T3", "Good Old Rod", "Железо, редстоун, лазурит, редкие руды IU."),
    ("T4", "Fine Rod", "Золото, алмазы, продвинутые руды."),
    ("T5", "Excellent Rod", "Плотные руды и индустриальный лут."),
    ("T6+", "Легендарные", "Верхние тиры StarCatcher — максимум глубин."),
]

rod_cards = "\n".join(
    f"""      <div class="card reveal">
        <div class="rod-tier">{t}</div>
        <h3 style="margin:.35rem 0">{n}</h3>
        <p style="margin:0;color:var(--muted)">{d}</p>
      </div>"""
    for t, n, d in RODS
)

PAGES["rods.html"] = (
    "rods",
    "Удочки StarCatcher — AquaTech",
    "Прогрессия 12 удочек StarCatcher на сервере AquaTech.",
    f"""
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">StarCatcher</div>
      <h1>Удочки и лут</h1>
      <p>Ресурсные удочки отменяют ванильный улов и крутят пулы AquaTech. Полная таблица — в документации репозитория.</p>
      <div class="banner" style="background:radial-gradient(circle at 30% 40%, rgba(245,197,66,.3), transparent 35%), linear-gradient(145deg,#164e63,#0f766e,#083344)"></div>
    </div>
  </section>
  <section class="section" style="padding-top:1rem">
    <div class="container rod-grid">
{rod_cards}
    </div>
  </section>
""",
)

PAGES["top.html"] = (
    "top",
    "Топы игроков — AquaTech",
    "Рейтинги игроков AquaTech по онлайну, монетам и лайкам.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Рейтинги</div>
      <h1>Топы игроков</h1>
      <p>Демо-данные сайта. Позже подключим живой API сервера.</p>
      <div class="banner"></div>
      <div class="tabs">
        <button class="tab active" type="button" data-top-tab="playtime">По онлайну</button>
        <button class="tab" type="button" data-top-tab="coins">По монетам</button>
        <button class="tab" type="button" data-top-tab="likes">По лайкам</button>
      </div>
      <div class="top-list" id="top-root"></div>
    </div>
  </section>
""",
)

PAGES["news.html"] = (
    "news",
    "Новости — AquaTech",
    "Новости и обновления проекта AquaTech.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Блог</div>
      <h1>Новости и обновления</h1>
      <p>Лаунчер, сборка, сайт и сервер.</p>
    </div>
  </section>
  <section class="section" style="padding-top:0">
    <div class="container news-list">
      <article class="news-item">
        <time>8 августа 2026</time>
        <h3>Портал AquaTech и лаунчер 2.9.8</h3>
        <p>Сайт пересобран как полноценный портал: профили, топы, магазин, удочки. Лаунчер качает сборку с CDN.</p>
      </article>
      <article class="news-item">
        <time>Август 2026</time>
        <h3>Playit только для игры</h3>
        <p>IP туннеля — только для входа на сервер. Обновления модов больше не идут через Playit.</p>
      </article>
      <article class="news-item">
        <time>Июль 2026</time>
        <h3>Авторыбалка и StarCatcher</h3>
        <p>Совместимость aquatech_ui с прогрессией удочек и кастомными пулами лута.</p>
      </article>
    </div>
  </section>
""",
)

PAGES["profile.html"] = (
    "profile",
    "Профиль игрока — AquaTech",
    "Профиль игрока AquaTech: статистика, лайки и кастомизация.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Профиль</div>
      <h1>Страница игрока</h1>
      <p>Кастомизируй обложку и свечение. Лайки и просмотры считаются локально в браузере (демо).</p>
    </div>
  </section>
  <section class="section" style="padding-top:0">
    <div class="container profile-layout">
      <div id="profile-root"></div>
      <div class="card">
        <h3>Быстрые ссылки</h3>
        <ul class="perk-list">
          <li><a href="top.html">Топы игроков</a></li>
          <li><a href="players.html">Поиск игроков</a></li>
          <li><a href="store.html">Магазин привилегий</a></li>
          <li><a href="start.html">Скачать лаунчер</a></li>
        </ul>
      </div>
    </div>
  </section>
""",
)

PAGES["login.html"] = (
    "login",
    "Вход — AquaTech",
    "Вход в профиль AquaTech.",
    """
  <section class="page-hero">
    <div class="container auth-shell">
      <div class="eyebrow">Аккаунт</div>
      <h1>Вход</h1>
      <p>Демо-авторизация в браузере. Ник станет твоим профилем на сайте.</p>
      <form class="card form" id="login-form" style="margin-top:1.25rem">
        <div class="field"><label>Ник</label><input name="nick" maxlength="16" placeholder="Steve" required /></div>
        <div class="field"><label>Пароль</label><input name="password" type="password" placeholder="••••" /></div>
        <button class="btn btn-primary" type="submit">Войти</button>
        <a class="btn btn-ghost" href="register.html">Нет аккаунта? Регистрация</a>
      </form>
    </div>
  </section>
""",
)

PAGES["register.html"] = (
    "register",
    "Регистрация — AquaTech",
    "Создай профиль на сайте AquaTech.",
    """
  <section class="page-hero">
    <div class="container auth-shell">
      <div class="eyebrow">Аккаунт</div>
      <h1>Регистрация</h1>
      <p>Создай профиль, чтобы лайкать игроков и оформлять страницу.</p>
      <form class="card form" id="register-form" style="margin-top:1.25rem">
        <div class="field"><label>Ник</label><input name="nick" maxlength="16" placeholder="Steve" required /></div>
        <div class="field"><label>Пароль</label><input name="password" type="password" placeholder="минимум 4 символа" required /></div>
        <button class="btn btn-primary" type="submit">Создать аккаунт</button>
        <a class="btn btn-ghost" href="login.html">Уже есть аккаунт? Войти</a>
      </form>
    </div>
  </section>
""",
)

PAGES["players.html"] = (
    "players",
    "Поиск игроков — AquaTech",
    "Найди игрока AquaTech по нику.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Сообщество</div>
      <h1>Поиск игроков</h1>
      <p>Открой профиль любого игрока из демо-списка.</p>
      <div class="field" style="margin-top:1.25rem;max-width:420px">
        <label>Ник</label>
        <input id="player-search" placeholder="Начни вводить…" />
      </div>
      <div class="top-list" id="player-results" style="margin-top:1.25rem"></div>
    </div>
  </section>
""",
)

PAGES["rules.html"] = (
    "rules",
    "Правила — AquaTech",
    "Правила сообщества AquaTech.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Сообщество</div>
      <h1>Правила</h1>
      <div class="card" style="margin-top:1.25rem">
        <ol class="perk-list" style="list-style:decimal;padding-left:1.2rem">
          <li>Не читерь и не используй дюпы.</li>
          <li>Не мешай другим на спавне и в чужих базах.</li>
          <li>Не оскорбляй игроков в чате.</li>
          <li>Реклама других серверов запрещена.</li>
          <li>Администрация может откатить гриф и выдать мут/бан.</li>
        </ol>
      </div>
    </div>
  </section>
""",
)


def main() -> None:
    for name, (page, title, desc, body) in PAGES.items():
        html = SHELL.format(page=page, title=title, desc=desc, body=body)
        (DOCS / name).write_text(html, encoding="utf-8")
        print("wrote", name)
    # Keep root index in sync with docs home for non-Pages previews
    (ROOT / "index.html").write_text((DOCS / "index.html").read_text(encoding="utf-8"), encoding="utf-8")
    print("synced root index.html")


if __name__ == "__main__":
    main()
