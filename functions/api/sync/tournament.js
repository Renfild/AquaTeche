import { bad, json, readJson } from "../../_lib/http.js";
import { validateSnapshot } from "../../_lib/tournament.js";

export async function onRequestPost(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const serverKey = request.headers.get("X-AquaTech-Server-Key") || "";
  const expectedKey = env.SERVER_SYNC_KEY || "";
  if (!expectedKey || serverKey !== expectedKey) return bad("Неверный ключ сервера", 403);

  const parsed = validateSnapshot(await readJson(request));
  if (!parsed.ok) return bad(parsed.error);
  const { week, status, endsAt, nextStartsAt, top } = parsed.value;

  const existing = await env.DB.prepare("SELECT status FROM tournament_weeks WHERE week = ?").bind(week).first();
  if (existing && existing.status === "finalized") {
    return json({ ok: true, ignored: true, reason: "week finalized" });
  }

  const statements = [
    env.DB.prepare(
      `INSERT INTO tournament_weeks (week, ends_at, next_starts_at, status) VALUES (?, ?, ?, ?)
       ON CONFLICT(week) DO UPDATE SET
         ends_at = excluded.ends_at,
         next_starts_at = excluded.next_starts_at,
         status = excluded.status,
         updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')`
    ).bind(week, endsAt, nextStartsAt, status),
    env.DB.prepare("DELETE FROM tournament_entries WHERE week = ?").bind(week),
    ...top.map((e) =>
      env.DB.prepare("INSERT INTO tournament_entries (week, uuid, nick, weight, fish) VALUES (?, ?, ?, ?, ?)").bind(
        week,
        e.uuid,
        e.nick,
        e.weight,
        e.fish
      )
    ),
  ];
  await env.DB.batch(statements);
  return json({ ok: true, week, status, entries: top.length });
}
