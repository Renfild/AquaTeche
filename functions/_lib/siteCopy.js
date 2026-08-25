import { ensureSettings, getSetting, setSetting } from "./settings.js";

export const SITE_COPY_KEYS = [
  "hero_eyebrow",
  "hero_title",
  "hero_lead",
  "features_title",
  "features_lead",
  "tile_rods_tag",
  "tile_rods_title",
  "tile_rods_body",
  "tile_cases_tag",
  "tile_cases_title",
  "tile_cases_body",
  "tile_top_tag",
  "tile_top_title",
  "tile_top_body",
  "home_news_title",
  "home_news_lead",
  "join_title",
  "join_body",
  "footer_blurb",
  "start_eyebrow",
  "start_title",
  "start_lead",
  "start_step1_title",
  "start_step1_body",
  "start_step2_title",
  "start_step2_1",
  "start_step2_2",
  "start_step2_3",
  "start_step2_4",
  "store_eyebrow",
  "store_title",
  "store_lead",
  "store_notice",
  "cases_eyebrow",
  "cases_title",
  "cases_lead",
  "cases_notice",
  "rods_eyebrow",
  "rods_title",
  "rods_lead",
  "rods_rules_title",
  "rods_rule_1",
  "rods_rule_2",
  "rods_rule_3",
  "rods_rule_4",
  "top_eyebrow",
  "top_title",
  "top_lead",
  "news_eyebrow",
  "news_title",
  "news_page_lead",
  "profile_eyebrow",
  "profile_title",
  "profile_lead",
  "login_eyebrow",
  "login_title",
  "login_lead",
  "register_eyebrow",
  "register_title",
  "register_lead",
  "players_eyebrow",
  "players_title",
  "players_lead",
  "rules_eyebrow",
  "rules_title",
  "rules_1",
  "rules_2",
  "rules_3",
  "rules_4",
  "rules_5",
];

export const SITE_COPY_DEFAULTS = {
  hero_eyebrow: "Minecraft 1.20.1 · океанский skyblock",
  hero_title: "AquaTech",
  hero_lead:
    "Спавн на плоту посреди бесконечного океана. Удочки AquaTech добывают руды из воды, авторыбалка, кейсы и индустриальные моды.",
  features_title: "Особенности сервера",
  features_lead: "Один бесконечный океан: рыбалка вместо шахт, кейсы и живые топы игроков.",
  tile_rods_tag: "Удочки",
  tile_rods_title: "Удочки AquaTech",
  tile_rods_body: "12 уровней с авторыбалкой: от бамбуковой до звёздного титана. Добыча руд, сплавов и механизмов прямо из воды.",
  tile_cases_tag: "Кейсы",
  tile_cases_title: "Кейсы и лут",
  tile_cases_body: "Материалы прогрессии, редкие руды и удочки высоких тиров. Открытие внутри игры через меню F4.",
  tile_top_tag: "Игроки",
  tile_top_title: "Таблица лидеров",
  tile_top_body: "Рейтинг по пойманной рыбе, балансу монет и лайкам профиля с обновлением в реальном времени.",
  home_news_title: "Новости",
  home_news_lead: "Обновления лаунчера, квестов и сервера.",
  join_title: "AquaTech Ocean",
  join_body: "Выживание на плоту: удочки AquaTech добывают руды из воды, авторыбалка автоматизирует улов, а промышленная цепочка ведёт к квантовому эндгейму.",
  footer_blurb: "Океанский сервер. Скачай лаунчер и заходи.",
  start_eyebrow: "Быстрый старт",
  start_title: "Как начать играть на AquaTech",
  start_lead: "Лаунчер автоматически скачает сборку Minecraft 1.20.1 с модами, настроит Java 17 и выделит память под ваш компьютер.",
  start_step1_title: "1. Регистрация",
  start_step1_body: "Создайте профиль игрока для синхронизации прогресса.",
  start_step2_title: "2. Лаунчер",
  start_step2_1: "Скачайте лаунчер для Windows",
  start_step2_2: "Авторизуйтесь под своим ником",
  start_step2_3: "Нажмите «Играть»",
  start_step2_4: "Клиент обновится автоматически",
  store_eyebrow: "Магазин",
  store_title: "Привилегии",
  store_lead: "Состав рангов и цены. Купить на сайте пока нельзя.",
  store_notice: "Покупки выключены. Оплату подключим позже.",
  cases_eyebrow: "Кейсы",
  cases_title: "Что внутри",
  cases_lead: "Сайт только показывает состав. Открывай кейсы в игре (F4).",
  cases_notice: "На сайте кейсы не открываются.",
  rods_eyebrow: "AquaTech",
  rods_title: "Удочки и лут",
  rods_lead:
    "Ванильный улов выключен. Ресурсные удочки крутят пулы AquaTech; множители ×2…×64 умножают количество.",
  rods_rules_title: "Как считается улов",
  rods_rule_1: "Каждый предмет в пуле сначала кидает свой шанс.",
  rods_rule_2: "Из успешно прошедших случайно оставляют 1–3 стака (у T1 доп. пула — 1–2).",
  rods_rule_3: "Кол-во в стаке — диапазон из таблицы; множитель удочки его умножает.",
  rods_rule_4: "Костяная и небесная ловят только рыбу, без ресурсного пула.",
  top_eyebrow: "Рейтинги",
  top_title: "Топы",
  top_lead: "Кто сколько наиграл, кто накопил монет, кого лайкнули.",
  news_eyebrow: "Блог",
  news_title: "Новости",
  news_page_lead: "Что нового на сервере и в лаунчере.",
  profile_eyebrow: "Профиль",
  profile_title: "Игрок",
  profile_lead: "Статы, био, тема оформления. Свой профиль правится после входа.",
  login_eyebrow: "Аккаунт",
  login_title: "Вход",
  login_lead: "Ник Minecraft: латиница, цифры, _.",
  register_eyebrow: "Аккаунт",
  register_title: "Регистрация",
  register_lead: "Ник 3–16 символов (A–Z, 0–9, _). Пароль от 4.",
  players_eyebrow: "Игроки",
  players_title: "Поиск",
  players_lead: "Введи ник, открой профиль.",
  rules_eyebrow: "Правила",
  rules_title: "На сервере",
  rules_1: "Читы и дюпы запрещены.",
  rules_2: "Не мешай на спавне и в чужих базах.",
  rules_3: "Без оскорблений в чате.",
  rules_4: "Чужие сервера в чате не рекламируй.",
  rules_5: "Админы могут откатить гриф и выдать мут/бан.",
};

function copyMaxLen(key) {
  if (
    key.endsWith("_lead") ||
    key.endsWith("_body") ||
    key.includes("_rule_") ||
    key.startsWith("rules_") ||
    key.endsWith("_notice") ||
    key === "footer_blurb"
  ) {
    return 800;
  }
  return 160;
}

export async function getSiteCopy(db) {
  await ensureSettings(db);
  const out = { ...SITE_COPY_DEFAULTS };
  for (const key of SITE_COPY_KEYS) {
    out[key] = await getSetting(db, key, SITE_COPY_DEFAULTS[key]);
  }
  return out;
}

export async function patchSiteCopy(db, patch) {
  if (!patch || typeof patch !== "object") return getSiteCopy(db);
  for (const key of SITE_COPY_KEYS) {
    if (!(key in patch)) continue;
    const val = String(patch[key] ?? "").trim();
    if (!val) continue;
    await setSetting(db, key, val.slice(0, copyMaxLen(key)));
  }
  return getSiteCopy(db);
}
