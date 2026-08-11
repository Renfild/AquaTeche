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
    "Спавн на плоту. Двенадцать удочек StarCatcher, авторыбалка, кейсы и индустриальные моды. Скачай лаунчер и заходи.",
  features_title: "На сервере",
  features_lead: "Один мир-океан. Рыбалка, кейсы, прогрессия.",
  tile_rods_tag: "Удочки",
  tile_rods_title: "StarCatcher",
  tile_rods_body: "Бамбук в начале, дальше руды и индустриальный лут из пулов AquaTech.",
  tile_cases_tag: "Кейсы",
  tile_cases_title: "Награды в игре",
  tile_cases_body: "Кейсы крутятся на сервере (F4). На сайте только состав.",
  tile_top_tag: "Игроки",
  tile_top_title: "Топы",
  tile_top_body: "Рейтинг по лайкам и монетам. Профиль можно оформить после входа.",
  home_news_title: "Новости",
  home_news_lead: "Что менялось в лаунчере и на сервере.",
  join_title: "AquaTech Ocean",
  join_body: "Океанский skyblock, плот 4×4. Заходи по IP ниже.",
  footer_blurb: "Океанский сервер. Скачай лаунчер и заходи.",
  start_eyebrow: "Старт",
  start_title: "Как зайти",
  start_lead: "Нужен Windows. Скачай лаунчер, впиши ник и жми «Играть».",
  start_step1_title: "1. Лаунчер",
  start_step1_body: "Скачай и запусти. Дальше всё поставится само.",
  start_step2_title: "2. Игра",
  start_step2_1: "Впиши ник в лаунчере",
  start_step2_2: "Дождись загрузки",
  start_step2_3: "Жми «Играть»",
  start_step2_4: "IP вручную: g-pl-3.apexnodes.xyz:21561",
  store_eyebrow: "Магазин",
  store_title: "Привилегии",
  store_lead: "Состав рангов и цены. Купить на сайте пока нельзя.",
  store_notice: "Покупки выключены. Оплату подключим позже.",
  cases_eyebrow: "Кейсы",
  cases_title: "Что внутри",
  cases_lead: "Сайт только показывает состав. Открывай кейсы в игре (F4).",
  cases_notice: "На сайте кейсы не открываются.",
  rods_eyebrow: "StarCatcher",
  rods_title: "Удочки и лут",
  rods_lead:
    "Ванильный улов выключен. Ресурсные удочки крутят пулы AquaTech; множители ×2…×64 умножают количество.",
  rods_rules_title: "Как считается улов",
  rods_rule_1: "Каждый предмет в пуле сначала кидает свой шанс.",
  rods_rule_2: "Из успешно прошедших случайно оставляют 1–3 стака (у T1 доп. пула — 1–2).",
  rods_rule_3: "Кол-во в стаке — диапазон из таблицы; множитель удочки его умножает.",
  rods_rule_4: "Костяная и небесная — только рыба StarCatcher, без ресурсного пула.",
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
