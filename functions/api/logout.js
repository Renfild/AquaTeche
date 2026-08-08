import { json } from "../_lib/http.js";
import { clearSessionCookie, getSessionId } from "../_lib/auth.js";

export async function onRequestPost(context) {
  const { request, env } = context;
  const sid = getSessionId(request);
  if (sid && env.DB) {
    await env.DB.prepare("DELETE FROM sessions WHERE id = ?").bind(sid).run();
  }
  return json({ ok: true }, 200, { "set-cookie": clearSessionCookie() });
}
