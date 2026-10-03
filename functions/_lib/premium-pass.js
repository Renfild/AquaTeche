import { enqueueCommand } from "../api/internal/pending-commands.js";

// Premium season pass ownership: a site purchase (pending_commands) or an in-game one reported by the server.

async function ensureOwnersTable(db) {
  await db
    .prepare(
      `CREATE TABLE IF NOT EXISTS pass_premium_owners (
        nick TEXT PRIMARY KEY COLLATE NOCASE,
        source TEXT NOT NULL DEFAULT '',
        created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ','now'))
      )`
    )
    .run();
}

export async function markPremiumPassOwned(db, nick, source) {
  await ensureOwnersTable(db);
  await db
    .prepare("INSERT OR IGNORE INTO pass_premium_owners (nick, source) VALUES (?, ?)")
    .bind(String(nick).trim(), String(source || ""))
    .run();
}

export async function isPremiumPassOwned(db, nick) {
  await ensureOwnersTable(db);
  const n = String(nick).trim();
  const owner = await db.prepare("SELECT 1 AS ok FROM pass_premium_owners WHERE nick = ?").bind(n).first();
  if (owner) return true;
  const queued = await db
    .prepare(
      "SELECT 1 AS ok FROM pending_commands WHERE nick = ? COLLATE NOCASE AND kind = 'pass_premium' AND status != 'failed' LIMIT 1"
    )
    .bind(n)
    .first();
  return Boolean(queued);
}

/** After enqueueing: true when an earlier live premium command exists, so two parallel purchases deliver once. */
export async function isDuplicatePremiumCommand(db, nick, commandId) {
  const earlier = await db
    .prepare(
      "SELECT 1 AS ok FROM pending_commands WHERE nick = ? COLLATE NOCASE AND kind = 'pass_premium' AND status != 'failed' AND id < ? LIMIT 1"
    )
    .bind(String(nick).trim(), commandId)
    .first();
  return Boolean(earlier);
}

/** Queues the premium pass for delivery unless the player already has it; a parallel second purchase is failed and refused (reason "owned"), a repeated payment id is reason "same_payment". */
export async function enqueuePremiumPass(db, { nick, provider, providerPaymentId }) {
  if (await isPremiumPassOwned(db, nick)) return { ok: false, reason: "owned" };
  let queued;
  try {
    queued = await enqueueCommand(db, { nick, kind: "pass_premium", payload: "", provider, providerPaymentId });
  } catch (err) {
    // two requests with the same payment id raced past the lookup: the other one owns the delivery
    if (/UNIQUE/i.test(String(err?.message))) return { ok: false, reason: "same_payment" };
    throw err;
  }
  if (!queued.ok) return { ok: false, reason: "error", error: queued.error };
  if (queued.duplicate) return { ok: false, reason: "same_payment" };
  if (await isDuplicatePremiumCommand(db, nick, queued.id)) {
    await db.prepare("UPDATE pending_commands SET status = 'failed' WHERE id = ? AND status = 'queued'").bind(queued.id).run();
    return { ok: false, reason: "owned" };
  }
  return { ok: true, id: queued.id };
}
