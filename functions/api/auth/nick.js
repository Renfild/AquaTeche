import { bad, json } from "../../_lib/http.js";
import { isUnclaimedHash, nickOk, normalizeNick } from "../../_lib/auth.js";
import { gateNickLookup } from "../../_lib/rate_limit.js";

/** Live nick check for the launcher. Only reports unclaimed server nicks. */
export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена", 503);

  const url = new URL(request.url);
  const nick = normalizeNick(url.searchParams.get("nick"));
  if (!nickOk(nick)) {
    return json({ ok: true, unclaimed: false });
  }

  const gated = await gateNickLookup(env.DB, request);
  if (!gated.ok) {
    return bad(`Слишком много запросов. Подождите ${gated.retrySec} с.`, 429);
  }

  const user = await env.DB.prepare(
    "SELECT password_hash FROM users WHERE nick = ? COLLATE NOCASE"
  )
    .bind(nick)
    .first();

  return json({
    ok: true,
    unclaimed: Boolean(user && isUnclaimedHash(user.password_hash)),
    exists: Boolean(user),
  });
}
