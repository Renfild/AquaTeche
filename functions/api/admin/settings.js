import { bad, json, readJson } from "../../_lib/http.js";
import { requireAdmin } from "../../_lib/auth.js";
import { getSetting, purchasesEnabled, setSetting } from "../../_lib/settings.js";
import { getSiteCopy, patchSiteCopy } from "../../_lib/siteCopy.js";

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);
  return json({
    ok: true,
    settings: {
      purchases_enabled: await purchasesEnabled(env),
      catalog_from_db: (await getSetting(env.DB, "catalog_from_db", "0")) === "1",
    },
    copy: await getSiteCopy(env.DB),
  });
}

export async function onRequestPatch(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);
  const admin = await requireAdmin(env.DB, request, env);
  if (!admin) return bad("Нет доступа", 403);
  const body = await readJson(request);
  if (!body || typeof body !== "object") return bad("Нужен JSON");

  if ("purchases_enabled" in body) {
    await setSetting(env.DB, "purchases_enabled", body.purchases_enabled ? "true" : "false");
  }
  if ("catalog_from_db" in body) {
    await setSetting(env.DB, "catalog_from_db", body.catalog_from_db ? "1" : "0");
  }
  if (body.copy && typeof body.copy === "object") {
    await patchSiteCopy(env.DB, body.copy);
  }

  return json({
    ok: true,
    settings: {
      purchases_enabled: await purchasesEnabled(env),
      catalog_from_db: (await getSetting(env.DB, "catalog_from_db", "0")) === "1",
    },
    copy: await getSiteCopy(env.DB),
  });
}
