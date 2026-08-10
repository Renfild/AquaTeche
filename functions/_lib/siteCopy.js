import { ensureSettings, getSetting, setSetting } from "./settings.js";

export const SITE_COPY_KEYS = [
  "hero_eyebrow",
  "hero_title",
  "hero_lead",
  "features_title",
  "features_lead",
  "join_title",
  "join_body",
  "footer_blurb",
  "news_page_lead",
];

export const SITE_COPY_DEFAULTS = {
  hero_eyebrow: "Minecraft 1.20.1 · океанский skyblock",
  hero_title: "AquaTech",
  hero_lead:
    "Спавн на плоту. Двенадцать удочек StarCatcher, авторыбалка, кейсы и индустриальные моды. Скачай лаунчер и заходи.",
  features_title: "На сервере",
  features_lead: "Один мир-океан. Рыбалка, кейсы, прогрессия.",
  join_title: "AquaTech Ocean",
  join_body: "Океанский skyblock, плот 4×4. Заходи по IP ниже.",
  footer_blurb: "Океанский сервер. Скачай лаунчер и заходи.",
  news_page_lead: "Что нового на сервере и в лаунчере.",
};

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
    const max =
      key.endsWith("_lead") || key.endsWith("_body") || key === "footer_blurb" ? 800 : 160;
    await setSetting(db, key, val.slice(0, max));
  }
  return getSiteCopy(db);
}
