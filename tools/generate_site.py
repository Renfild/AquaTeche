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
  <link href="https://fonts.googleapis.com/css2?family=Figtree:wght@400;500;600;700&family=Syne:wght@600;700;800&display=swap" rel="stylesheet" />
  <link rel="stylesheet" href="assets/css/site.css?v=20260810c" />
</head>
<body data-page="{page}">
  <a class="skip-link" href="#main">К содержимому</a>
  <div id="site-header"></div>
  <main id="main">
  {body}
  </main>
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
    <div class="hero-bg" aria-hidden="true">
      <div class="hero-caustic"></div>
      <div class="hero-horizon"></div>
      <div class="hero-wave"></div>
      <div class="hero-spark"></div>
    </div>
    <div class="container hero-inner">
      <p class="eyebrow reveal" data-site="hero_eyebrow">Minecraft 1.20.1 · океанский skyblock</p>
      <h1 class="reveal" style="--d:.06s" data-site="hero_title">AquaTech</h1>
      <p class="hero-lead reveal" style="--d:.12s" data-site="hero_lead">Спавн на плоту. Двенадцать удочек StarCatcher, авторыбалка, кейсы и индустриальные моды. Скачай лаунчер и заходи.</p>
      <div class="hero-actions reveal" style="--d:.18s">
        <a class="btn btn-primary" data-download href="#">Скачать лаунчер</a>
        <a class="btn btn-secondary" href="register.html">Регистрация</a>
        <a class="btn btn-ghost" href="store.html">Магазин</a>
      </div>
      <button class="ip-box reveal" style="--d:.24s" type="button" data-copy-ip>
        <div>
          <small>IP СЕРВЕРА</small>
          <strong>katherine-hydro.tun.ply.gg:31279</strong>
        </div>
        <span class="copy">Скопировать</span>
      </button>
    </div>
  </section>

  <section class="section">
    <div class="container">
      <div class="section-head reveal">
        <div>
          <h2 data-site="features_title">На сервере</h2>
          <p data-site="features_lead">Один мир-океан. Рыбалка, кейсы, прогрессия.</p>
        </div>
        <a class="btn btn-secondary" href="start.html">Как зайти</a>
      </div>
      <div class="grid-3">
        <a class="tile reveal" href="rods.html" style="--d:.05s">
          <div class="feature-art rods" aria-hidden="true"></div>
          <span class="tag">Удочки</span>
          <h3>StarCatcher</h3>
          <p>Бамбук в начале, дальше руды и индустриальный лут из пулов AquaTech.</p>
        </a>
        <a class="tile reveal" href="cases.html" style="--d:.1s">
          <div class="feature-art cases" aria-hidden="true"></div>
          <span class="tag gold">Кейсы</span>
          <h3>Награды в игре</h3>
          <p>Кейсы крутятся на сервере (F4). На сайте только состав.</p>
        </a>
        <a class="tile reveal" href="top.html" style="--d:.15s">
          <div class="feature-art tech" aria-hidden="true"></div>
          <span class="tag">Игроки</span>
          <h3>Топы</h3>
          <p>Рейтинг по лайкам и монетам. Профиль можно оформить после входа.</p>
        </a>
      </div>
    </div>
  </section>

  <section class="section section-tight">
    <div class="container grid-2">
      <div class="reveal">
        <div class="section-head">
          <div>
            <h2>Новости</h2>
            <p>Что менялось в лаунчере и на сервере.</p>
          </div>
        </div>
        <div class="news-list" id="home-news" data-news-home>
          <p class="muted-line">Загрузка новостей…</p>
        </div>
      </div>
      <aside class="join-panel reveal" style="--d:.08s">
        <span class="tag">Сервер</span>
        <h3 data-site="join_title">AquaTech Ocean</h3>
        <p data-site="join_body">Океанский skyblock, плот 4×4. Заходи по IP ниже.</p>
        <button class="ip-box ip-box-block" type="button" data-copy-ip>
          <div>
            <small>ПОДКЛЮЧЕНИЕ</small>
            <strong>katherine-hydro.tun.ply.gg:31279</strong>
          </div>
          <span class="copy">Скопировать</span>
        </button>
        <div class="join-actions">
          <a class="btn btn-aqua" href="start.html">Начать игру</a>
          <a class="btn btn-secondary" href="players.html">Игроки</a>
        </div>
      </aside>
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
      <p data-site="news_page_lead">Что нового на сервере и в лаунчере.</p>
    </div>
  </section>
  <section class="section" style="padding-top:0">
    <div class="container news-list" id="news-root" data-news-page>
      <p class="muted-line">Загрузка…</p>
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
            <h3 style="margin:0">Тексты сайта</h3>
            <p style="margin:.35rem 0 0">Главная, join-блок, футер, lead новостей</p>
          </div>
          <button class="btn btn-secondary" type="button" id="admin-save-copy">Сохранить тексты</button>
        </div>
        <div class="admin-copy-grid" id="admin-copy"></div>
      </div>
      <div class="card" style="grid-column:1/-1">
        <div class="section-head" style="margin-bottom:1rem">
          <div>
            <h3 style="margin:0">Новости</h3>
            <p style="margin:.35rem 0 0">Публикации на главной и /news</p>
          </div>
        </div>
        <form class="admin-news-form" id="admin-news-form">
          <div class="field"><label>Заголовок</label><input name="title" required maxlength="160" placeholder="Лаунчер 2.9.21" /></div>
          <div class="field"><label>Дата</label><input name="published_at" type="date" required /></div>
          <div class="field" style="grid-column:1/-1"><label>Текст</label><textarea name="body" rows="3" required maxlength="4000" placeholder="Что изменилось…"></textarea></div>
          <label class="admin-check" style="align-self:end"><input type="checkbox" name="published" checked /> Опубликовано</label>
          <button class="btn btn-primary" type="submit">Добавить</button>
        </form>
        <div id="admin-news" class="admin-table-wrap" style="margin-top:1rem"></div>
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
