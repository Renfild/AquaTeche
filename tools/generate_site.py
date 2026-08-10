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
  <link rel="stylesheet" href="assets/css/site.css?v=20260810g" />
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

def loot_rows(rows: list[tuple[str, str, str]]) -> str:
    body = "\n".join(
        f"            <tr><td>{chance}</td><td>{item}</td><td>{qty}</td></tr>"
        for chance, item, qty in rows
    )
    return f"""        <div class="loot-table-wrap">
          <table class="loot-table">
            <thead><tr><th>Шанс</th><th>Предмет</th><th>Кол-во</th></tr></thead>
            <tbody>
{body}
            </tbody>
          </table>
        </div>"""


def rod_loot_block(
    tier: str,
    name: str,
    blurb: str,
    tables: list[tuple[str, list[tuple[str, str, str]]]],
) -> str:
    parts = [
        f"""      <article class="loot-block" id="{tier.lower()}">
        <header class="loot-head">
          <div class="rod-tier">{tier}</div>
          <h2>{name}</h2>
          <p>{blurb}</p>
        </header>"""
    ]
    for title, rows in tables:
        parts.append(f'        <h3 class="loot-sub">{title}</h3>')
        parts.append(loot_rows(rows))
    parts.append("      </article>")
    return "\n".join(parts)


ROD_LOOT_BLOCKS = "\n".join(
    [
        rod_loot_block(
            "T1",
            "Бамбуковая / ванильная",
            "Сначала один гарантированный дроп по весам, потом доп. пул (из прошедших — 1–2 стака).",
            [
                (
                    "Гарантия (один из)",
                    [
                        ("18%", "Булыжник", "2–4"),
                        ("16%", "Земля", "2–4"),
                        ("14%", "Глина", "2–4"),
                        ("10%", "Саженец дуба", "1–2"),
                        ("10%", "Резиновый саженец", "1"),
                        ("10%", "Гравий", "2–3"),
                        ("10%", "Песок", "2–3"),
                        ("12%", "Саженец берёзы", "1"),
                    ],
                ),
                (
                    "Доп. пул",
                    [
                        ("50%", "Булыжник", "1–3"),
                        ("45%", "Земля", "1–3"),
                        ("45%", "Глина", "1–3"),
                        ("35%", "Саженец дуба", "1"),
                        ("25%", "Саженец берёзы", "1"),
                        ("35%", "Резиновый саженец", "1"),
                        ("30%", "Сырой латекс", "1–2"),
                        ("25%", "Необработанный торф", "1"),
                        ("40%", "Гравий", "1–2"),
                        ("35%", "Песок", "1–2"),
                        ("40%", "Медная руда", "1"),
                        ("30%", "Оловянная руда", "1"),
                        ("22%", "Титановая руда", "1"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T2",
            "Скромная",
            "Из прошедших шансов берут 1–3 стака.",
            [
                (
                    "Пул",
                    [
                        ("45%", "Булыжник", "1–2"),
                        ("35%", "Глина", "1–2"),
                        ("65%", "Медная руда", "1–2"),
                        ("50%", "Оловянная руда", "1–2"),
                        ("45%", "Железная руда", "1–2"),
                        ("40%", "Угольная руда", "1–2"),
                        ("45%", "Титановая руда", "1–2"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T3",
            "Старая добрая",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("55%", "Железная руда", "1–2"),
                        ("50%", "Редстоуновая руда", "1–2"),
                        ("45%", "Лазуритовая руда", "1–2"),
                        ("45%", "Оловянная руда", "1–2"),
                        ("40%", "Стронций", "1"),
                        ("40%", "Иттрий", "1"),
                        ("35%", "Таллий", "1"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T4",
            "Натуралиста",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("55%", "Шпинель", "1–2"),
                        ("50%", "Барий", "1–2"),
                        ("45%", "Оловянная руда", "1–2"),
                        ("40%", "Железная руда", "1–2"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T5",
            "Слизневая",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("55%", "Шпинель", "1–2"),
                        ("50%", "Барий", "1–2"),
                        ("45%", "Полоний", "1–2"),
                        ("40%", "Железная руда", "1–2"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T6",
            "Ледяная",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("50%", "Алюминий", "1–2"),
                        ("45%", "Серебро", "1"),
                        ("45%", "Цинк", "1"),
                        ("40%", "Железная руда", "1–2"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T7",
            "Ловец Звёзд",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("50%", "Золотая руда", "1–2"),
                        ("45%", "Лазурит", "2–5"),
                        ("35%", "Лазуритовая руда", "1–2"),
                        ("45%", "Вольфрам", "1"),
                        ("45%", "Хром", "1"),
                        ("40%", "Сапфир", "1"),
                        ("40%", "Топаз", "1"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T8",
            "Лазурный кристалл",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("55%", "Лазурит", "3–7"),
                        ("40%", "Лазуритовая руда", "1–2"),
                        ("55%", "Аметист", "2–4"),
                        ("50%", "Сапфир", "1–2"),
                        ("50%", "Топаз", "1–2"),
                        ("40%", "Руда сапфира", "1"),
                        ("40%", "Кристалл", "1"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T9",
            "Акулий клык",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("50%", "Титан", "1"),
                        ("45%", "Кобальт", "1"),
                        ("45%", "Марганец", "1"),
                        ("45%", "Никель", "1"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T10",
            "Обсидиановая",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("55%", "Алмаз", "1"),
                        ("55%", "Обсидиан", "1–2"),
                        ("45%", "Титан", "1"),
                        ("40%", "Нержавеющая сталь", "1"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T11",
            "Светящаяся ягода",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("55%", "Осколок призмарина", "2–4"),
                        ("50%", "Кристаллы призмарина", "1–2"),
                        ("40%", "Платина", "1"),
                        ("20%", "Сердце моря", "1"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T12",
            "Магматическая",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("55%", "Кварц", "2–4"),
                        ("50%", "Дроблёный уран", "1–2"),
                        ("45%", "Незеритовый лом", "1"),
                        ("40%", "Инконель", "1"),
                    ],
                ),
            ],
        ),
        rod_loot_block(
            "T13",
            "Альфа",
            "1–3 стака из прошедших.",
            [
                (
                    "Пул",
                    [
                        ("50%", "Иридий", "1"),
                        ("50%", "Осмий", "1"),
                        ("40%", "Полоний", "1"),
                        ("40%", "Осмиридий", "1"),
                        ("30%", "Адамантиевая руда", "1"),
                        ("20%", "Звезда Незера", "1"),
                    ],
                ),
            ],
        ),
    ]
)

PAGES["rods.html"] = (
    "rods",
    "Удочки и лут · AquaTech",
    "Шансы дропа ресурсных удочек StarCatcher на AquaTech.",
    f"""
  <section class="page-hero">
    <div class="container">
      <div class="eyebrow">StarCatcher</div>
      <h1>Удочки и лут</h1>
      <p>Ванильный улов выключен. Ресурсные удочки крутят пулы AquaTech; множители ×2…×64 умножают количество.</p>
      <div class="banner" style="background:radial-gradient(circle at 30% 40%, rgba(245,197,66,.3), transparent 35%), linear-gradient(145deg,#164e63,#0f766e,#083344)"></div>
    </div>
  </section>

  <section class="section section-tight">
    <div class="container">
      <h2 class="loot-section-title">Как считается улов</h2>
      <ul class="loot-rules">
        <li>Каждый предмет в пуле сначала кидает свой шанс.</li>
        <li>Из успешно прошедших случайно оставляют 1–3 стака (у T1 доп. пула — 1–2).</li>
        <li>Кол-во в стаке — диапазон из таблицы; множитель удочки его умножает.</li>
        <li>Avaritia / inferno / crystal_* из улова вырезаны.</li>
        <li>Костяная и небесная — только рыба StarCatcher, без ресурсного пула.</li>
      </ul>
      <nav class="loot-jump" aria-label="Тиры удочек">
        <a href="#t1">T1</a><a href="#t2">T2</a><a href="#t3">T3</a><a href="#t4">T4</a>
        <a href="#t5">T5</a><a href="#t6">T6</a><a href="#t7">T7</a><a href="#t8">T8</a>
        <a href="#t9">T9</a><a href="#t10">T10</a><a href="#t11">T11</a><a href="#t12">T12</a>
        <a href="#t13">T13</a><a href="#fish">Рыба</a><a href="#treasure">Treasure</a>
      </nav>
    </div>
  </section>

  <section class="section" style="padding-top:0">
    <div class="container loot-stack">
{ROD_LOOT_BLOCKS}

      <article class="loot-block" id="fish">
        <header class="loot-head">
          <div class="rod-tier">Рыба</div>
          <h2>Костяная и небесная</h2>
          <p>Ресурсный пул AquaTech не крутится — улов как у StarCatcher по умолчанию (рыба).</p>
        </header>
        <div class="loot-table-wrap">
          <table class="loot-table">
            <thead><tr><th>Удочка</th><th>Поведение</th></tr></thead>
            <tbody>
              <tr><td>Костяная</td><td>Рыба StarCatcher</td></tr>
              <tr><td>Небесная</td><td>Рыба StarCatcher</td></tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="loot-block" id="treasure">
        <header class="loot-head">
          <div class="rod-tier">Бонус</div>
          <h2>Treasure поверх улова</h2>
          <p>Взвешенный бросок: perfect reel (quality ≥ 90) — 35%; навык rare loot / шторм Horizon; полная луна — 18%.</p>
        </header>
{loot_rows([
    ("35%", "Осколок призмарина", "1–2"),
    ("20%", "Кристаллы призмарина", "1–2"),
    ("15%", "Золотая руда", "1"),
    ("12%", "Изумруд", "1"),
    ("10%", "Алмаз", "1"),
    ("5%", "Раковина наутилуса", "1"),
    ("3%", "Сердце моря", "1"),
])}
      </article>
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
