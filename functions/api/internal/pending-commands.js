import { bad, json, readJson } from "../../_lib/http.js";

function auth(context) {
  const serverKey = context.request.headers.get("X-AquaTech-Server-Key") || "";
  const expectedKey = context.env.SERVER_SYNC_KEY || "";
  if (!expectedKey || serverKey !== expectedKey) {
    return false;
  }
  return Boolean(context.env.DB);
}

async function ensureTable(db) {
  await db
    .prepare(
      `CREATE TABLE IF NOT EXISTS pending_commands (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nick TEXT NOT NULL,
        kind TEXT NOT NULL,
        payload TEXT NOT NULL DEFAULT '',
        provider TEXT NOT NULL DEFAULT '',
        provider_payment_id TEXT NOT NULL DEFAULT '',
        status TEXT NOT NULL DEFAULT 'queued',
        created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now')),
        claimed_at TEXT
      )`
    )
    .run();
  await db
    .prepare(
      `CREATE UNIQUE INDEX IF NOT EXISTS pending_commands_provider_id
       ON pending_commands(provider, provider_payment_id)
       WHERE provider_payment_id != ''`
    )
    .run();
  await db
    .prepare(
      `CREATE INDEX IF NOT EXISTS pending_commands_nick_status
       ON pending_commands(nick, status)`
    )
    .run();
}

export async function enqueueCommand(db, { nick, kind, payload, provider, providerPaymentId }) {
  await ensureTable(db);
  const n = String(nick || "").trim();
  const k = String(kind || "").trim();
  const p = String(payload || "");
  if (!n || !k) return { ok: false, error: "nick/kind" };
  const providerName = String(provider || "");
  const pid = String(providerPaymentId || "");
  if (pid) {
    const existing = await db
      .prepare(
        "SELECT id, status FROM pending_commands WHERE provider = ? AND provider_payment_id = ?"
      )
      .bind(providerName, pid)
      .first();
    if (existing) {
      return { ok: true, id: existing.id, duplicate: true };
    }
  }
  const res = await db
    .prepare(
      `INSERT INTO pending_commands (nick, kind, payload, provider, provider_payment_id)
       VALUES (?, ?, ?, ?, ?)`
    )
    .bind(n, k, p, providerName, pid)
    .run();
  return { ok: true, id: res.meta.last_row_id };
}

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!auth(context)) return bad("Неверный ключ сервера", 403);
  await ensureTable(env.DB);
  const url = new URL(request.url);
  const nick = String(url.searchParams.get("nick") || "").trim();
  if (!nick) return bad("Укажите nick");
  const rows = await env.DB.prepare(
    `SELECT id, nick, kind, payload, status FROM pending_commands
     WHERE nick = ? COLLATE NOCASE AND status = 'queued'
     ORDER BY id ASC LIMIT 20`
  )
    .bind(nick)
    .all();
  return json({ ok: true, commands: rows.results || [] });
}

export async function onRequestPost(context) {
  const { env } = context;
  if (!auth(context)) return bad("Неверный ключ сервера", 403);
  await ensureTable(env.DB);
  const body = await readJson(context.request);
  if (!body || !body.op) return bad("Укажите op");
  const op = String(body.op);

  if (op === "enqueue") {
    const result = await enqueueCommand(env.DB, {
      nick: body.nick,
      kind: body.kind,
      payload: body.payload,
      provider: body.provider,
      providerPaymentId: body.provider_payment_id,
    });
    if (!result.ok) return bad(result.error || "enqueue");
    return json(result);
  }

  if (op === "ack" || op === "fail") {
    const id = Math.floor(Number(body.id || 0));
    if (!id) return bad("id");
    const status = op === "ack" ? "done" : "failed";
    const updated = await env.DB.prepare(
      `UPDATE pending_commands
       SET status = ?, claimed_at = strftime('%Y-%m-%dT%H:%M:%fZ','now')
       WHERE id = ? AND status = 'queued'`
    )
      .bind(status, id)
      .run();
    if (!updated.meta.changes) return bad("Команда уже закрыта", 409);
    return json({ ok: true, status });
  }

  return bad("Неизвестный op");
}
