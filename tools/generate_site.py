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
  <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@500;700;800&family=Sora:wght@400;500;600;700;800&display=swap" rel="stylesheet" />
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

PAGES: dict[str, tuple[str, str, str, str]] = {}

PAGES["index.html"] = (
    "home",
    "AquaTech · океанский Minecraft 1.20.1",
    "Океанский Minecraft: удочки StarCatcher, кейсы, сервер AquaTech.",
    """
  <section class="hero">
    <div class="hero-bg" aria-hidden="true"></div>
    <div class="container hero-inner">
      <div class="eyebrow">Minecraft 1.20.1</div>
      <h1>AquaTech</h1>
      <p class="hero-lead">Спавн на плоту в океане. Двенадцать удочек StarCatcher, авторыбалка, кейсы и индустриальные моды. Скачай лаунчер и заходи.</p>
      <div class="hero-actions">
        <a class="btn btn-primary" data-download href="#">Скачать лаунчер</a>
        <a class="btn btn-secondary" href="register.html">Регистрация</a>
        <a class="btn btn-ghost" href="store.html">Магазин</a>
      </div>
      <div class="ip-box" data-copy-ip>
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
      <div class="section-head">
        <div>
          <h2>На сервере</h2>
          <p>Один мир-океан. Рыбалка, кейсы, прогрессия.</p>
        </div>
        <a class="btn btn-secondary" href="start.html">Как зайти</a>
      </div>
      <div class="grid-3">
        <a class="card card-link" href="rods.html">
          <div class="feature-art rods"></div>
          <span class="tag">Удочки</span>
          <h3>StarCatcher</h3>
          <p>Бамбук в начале, дальше руды и индустриальный лут из пулов AquaTech.</p>
        </a>
        <a class="card card-link" href="cases.html">
          <div class="feature-art cases"></div>
          <span class="tag gold">Кейсы</span>
          <h3>Награды в игре</h3>
          <p>Кейсы крутятся на сервере (F4). На сайте только состав.</p>
        </a>
        <a class="card card-link" href="top.html">
          <div class="feature-art tech"></div>
          <span class="tag">Игроки</span>
          <h3>Топы</h3>
          <p>Рейтинг по лайкам и монетам. Профиль можно оформить после входа.</p>
        </a>
      </div>
    </div>
  </section>

  <section class="section" style="padding-top:0">
    <div class="container grid-2">
      <div>
        <div class="section-head">
          <div>
            <h2>Новости</h2>
            <p>Что менялось в лаунчере и на сервере.</p>
          </div>
        </div>
        <div class="news-list">
          <a class="news-item" href="news.html">
            <time>8 августа 2026</time>
            <h3>Лаунчер 2.9.16</h3>
            <p>Экран входа, тёмный UI, клики со звуком. Регистрация на сайте, в лаунчере — только авторизация.</p>
          </a>
          <a class="news-item" href="news.html">
            <time>Июль 2026</time>
            <h3>Лут StarCatcher</h3>
            <p>Таблицы улова для всех тиров удочек, в том числе с авторыбалкой.</p>
          </a>
        </div>
      </div>
      <div class="card">
        <span class="tag">Сервер</span>
        <h3>AquaTech Ocean</h3>
        <p style="margin:.6rem 0 1rem;color:var(--muted)">Океанский skyblock, плот 4×4. Заходи по IP ниже.</p>
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
    "Начать игру · AquaTech",
    "Скачай лаунчер AquaTech и зайди на сервер.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Старт</div>
      <h1>Как зайти</h1>
      <p>Нужен Windows. Скачай лаунчер, впиши ник и жми «Играть».</p>
      <div class="banner"></div>
    </div>
  </section>
  <section class="section" style="padding-top:1rem">
    <div class="container grid-2">
      <div class="card">
        <h3>1. Лаунчер</h3>
        <p style="color:var(--muted);margin:.6rem 0 1rem">Скачай и запусти. Дальше всё поставится само.</p>
        <a class="btn btn-primary" data-download href="#">Скачать для Windows</a>
      </div>
      <div class="card">
        <h3>2. Игра</h3>
        <ul class="perk-list">
          <li>Впиши ник в лаунчере</li>
          <li>Дождись загрузки</li>
          <li>Жми «Играть»</li>
          <li>IP вручную: katherine-hydro.tun.ply.gg:31279</li>
        </ul>
        <div style="margin-top:1rem;display:flex;gap:.6rem;flex-wrap:wrap">
          <a class="btn btn-secondary" href="register.html">Профиль на сайте</a>
          <button class="btn btn-ghost" type="button" data-copy-ip>Скопировать IP</button>
        </div>
      </div>
    </div>
  </section>
""",
)

PAGES["store.html"] = (
    "store",
    "Магазин · AquaTech",
    "Привилегии VIP–Ultimate на сервере AquaTech. Покупка на сайте пока выключена.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Магазин</div>
      <h1>Привилегии</h1>
      <p>Состав рангов и цены. Купить на сайте пока нельзя.</p>
      <div class="notice-banner inline">Покупки выключены. Оплату подключим позже.</div>
    </div>
  </section>
  <section class="section" style="padding-top:0">
    <div class="container catalog-grid" id="store-root"></div>
  </section>
""",
)

PAGES["cases.html"] = (
    "cases",
    "Кейсы · AquaTech",
    "Состав кейсов AquaTech. Открывать их нужно в игре.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Кейсы</div>
      <h1>Что внутри</h1>
      <p>Сайт только показывает состав. Открывай кейсы в игре (F4).</p>
      <div class="notice-banner inline">На сайте кейсы не открываются.</div>
    </div>
  </section>
  <section class="section" style="padding-top:0">
    <div class="container catalog-grid" id="cases-root"></div>
  </section>
""",
)

RODS = [
    ("T1", "Bamboo Rod", "Старт: булыжник, земля, саженцы, медь."),
    ("T2", "Humble Rod", "Медь, олово, железо, уголь, титан."),
    ("T3", "Good Old Rod", "Железо, редстоун, лазурит, редкие руды IU."),
    ("T4", "Fine Rod", "Золото, алмазы, продвинутые руды."),
    ("T5", "Excellent Rod", "Плотные руды и индустриальный лут."),
    ("T6+", "Легендарные", "Верхние тиры StarCatcher с самым жирным пулом."),
]

rod_cards = "\n".join(
    f"""      <div class="card">
        <div class="rod-tier">{t}</div>
        <h3 style="margin:.35rem 0">{n}</h3>
        <p style="margin:0;color:var(--muted)">{d}</p>
      </div>"""
    for t, n, d in RODS
)

PAGES["rods.html"] = (
    "rods",
    "Удочки StarCatcher · AquaTech",
    "Прогрессия удочек StarCatcher на AquaTech.",
    f"""
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">StarCatcher</div>
      <h1>Удочки и лут</h1>
      <p>Ванильный улов выключен. Каждая удочка крутит свой пул ресурсов AquaTech.</p>
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
    "Топы игроков · AquaTech",
    "Рейтинги игроков AquaTech.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Рейтинги</div>
      <h1>Топы</h1>
      <p>Кто сколько наиграл, кто накопил монет, кого лайкнули.</p>
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
    "Новости · AquaTech",
    "Новости AquaTech.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Блог</div>
      <h1>Новости</h1>
      <p>Что нового на сервере и в лаунчере.</p>
    </div>
  </section>
  <section class="section" style="padding-top:0">
    <div class="container news-list">
      <article class="news-item">
        <time>8 августа 2026</time>
        <h3>Лаунчер 2.9.16</h3>
        <p>Полноэкранный вход, палитра v2, анимации кнопок и мягкие звуки клика.</p>
      </article>
      <article class="news-item">
        <time>Август 2026</time>
        <h3>Подключение к серверу</h3>
        <p>Заходи по IP с сайта. Отдельный туннель для модов больше не нужен.</p>
      </article>
      <article class="news-item">
        <time>Июль 2026</time>
        <h3>Авторыбалка + StarCatcher</h3>
        <p>Удочки с кастомным лутом и авторыбалкой на сервере.</p>
      </article>
    </div>
  </section>
""",
)

PAGES["profile.html"] = (
    "profile",
    "Профиль игрока · AquaTech",
    "Профиль игрока AquaTech.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Профиль</div>
      <h1>Игрок</h1>
      <p>Статы, био, тема оформления. Свой профиль правится после входа.</p>
    </div>
  </section>
  <section class="section" style="padding-top:0">
    <div class="container profile-layout">
      <div id="profile-root"></div>
      <div class="card">
        <h3>Ссылки</h3>
        <ul class="perk-list">
          <li><a href="top.html">Топы</a></li>
          <li><a href="players.html">Поиск</a></li>
          <li><a href="store.html">Магазин</a></li>
          <li><a href="start.html">Лаунчер</a></li>
        </ul>
      </div>
    </div>
  </section>
""",
)

PAGES["login.html"] = (
    "login",
    "Вход · AquaTech",
    "Вход в профиль AquaTech.",
    """
  <section class="page-hero">
    <div class="container auth-shell">
      <div class="eyebrow">Аккаунт</div>
      <h1>Вход</h1>
      <p>Ник Minecraft: латиница, цифры, _.</p>
      <form class="card form" id="login-form" style="margin-top:1.25rem">
        <div class="field"><label>Ник</label><input name="nick" maxlength="16" placeholder="Steve" required pattern="[A-Za-z0-9_]{3,16}" /></div>
        <div class="field"><label>Пароль</label><input name="password" type="password" placeholder="••••" required /></div>
        <button class="btn btn-primary" type="submit">Войти</button>
        <a class="btn btn-ghost" href="register.html">Нет аккаунта? Регистрация</a>
      </form>
    </div>
  </section>
""",
)

PAGES["register.html"] = (
    "register",
    "Регистрация · AquaTech",
    "Регистрация на сайте AquaTech.",
    """
  <section class="page-hero">
    <div class="container auth-shell">
      <div class="eyebrow">Аккаунт</div>
      <h1>Регистрация</h1>
      <p>Ник 3–16 символов (A–Z, 0–9, _). Пароль от 4.</p>
      <form class="card form" id="register-form" style="margin-top:1.25rem">
        <div class="field"><label>Ник</label><input name="nick" maxlength="16" placeholder="Steve" required pattern="[A-Za-z0-9_]{3,16}" /></div>
        <div class="field"><label>Пароль</label><input name="password" type="password" placeholder="минимум 4 символа" required minlength="4" /></div>
        <button class="btn btn-primary" type="submit">Создать аккаунт</button>
        <a class="btn btn-ghost" href="login.html">Уже есть аккаунт? Войти</a>
      </form>
    </div>
  </section>
""",
)

PAGES["players.html"] = (
    "players",
    "Поиск игроков · AquaTech",
    "Поиск игроков AquaTech.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Игроки</div>
      <h1>Поиск</h1>
      <p>Введи ник, открой профиль.</p>
      <div class="field" style="margin-top:1.25rem;max-width:420px">
        <label>Ник</label>
        <input id="player-search" placeholder="Ник…" />
      </div>
      <div class="top-list" id="player-results" style="margin-top:1.25rem"></div>
    </div>
  </section>
""",
)

PAGES["rules.html"] = (
    "rules",
    "Правила · AquaTech",
    "Правила AquaTech.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Правила</div>
      <h1>На сервере</h1>
      <div class="card" style="margin-top:1.25rem">
        <ol class="perk-list" style="list-style:decimal;padding-left:1.2rem">
          <li>Читы и дюпы запрещены.</li>
          <li>Не мешай на спавне и в чужих базах.</li>
          <li>Без оскорблений в чате.</li>
          <li>Чужие сервера в чате не рекламируй.</li>
          <li>Админы могут откатить гриф и выдать мут/бан.</li>
        </ol>
      </div>
    </div>
  </section>
""",
)

PAGES["admin.html"] = (
    "admin",
    "Админка · AquaTech",
    "Панель администратора AquaTech.",
    """
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">Служебное</div>
      <h1>Админка</h1>
      <p id="admin-gate">Проверяем доступ…</p>
    </div>
  </section>
  <section class="section" style="padding-top:0" id="admin-root" hidden>
    <div class="container admin-layout">
      <div class="card">
        <h3>Настройки</h3>
        <label class="admin-check">
          <input type="checkbox" id="admin-purchases" />
          Покупки на сайте включены
        </label>
        <button class="btn btn-secondary" type="button" id="admin-save-settings" style="margin-top:1rem">Сохранить</button>
      </div>
      <div class="card" style="grid-column:1/-1">
        <div class="section-head" style="margin-bottom:1rem">
          <div>
            <h3 style="margin:0">Игроки</h3>
            <p style="margin:.35rem 0 0">Поиск и правка привилегии / статов</p>
          </div>
          <div class="field" style="margin:0;min-width:220px">
            <label>Ник</label>
            <input id="admin-user-q" placeholder="поиск…" />
          </div>
        </div>
        <div id="admin-users" class="admin-table-wrap"></div>
      </div>
      <div class="card" style="grid-column:1/-1">
        <div class="section-head" style="margin-bottom:1rem">
          <div>
            <h3 style="margin:0">Каталог</h3>
            <p style="margin:.35rem 0 0">Магазин и кейсы</p>
          </div>
          <button class="btn btn-ghost" type="button" id="admin-short-copy">Короткие тексты по умолчанию</button>
        </div>
        <div id="admin-catalog" class="admin-table-wrap"></div>
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
    (ROOT / "index.html").write_text((DOCS / "index.html").read_text(encoding="utf-8"), encoding="utf-8")
    print("synced root index.html")


if __name__ == "__main__":
    main()
