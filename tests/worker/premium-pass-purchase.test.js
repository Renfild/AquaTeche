import test from "node:test";
import assert from "node:assert/strict";
import { onRequestPost as purchase, onRequestCallback as lavaCallback } from "../../functions/api/purchase.js";
import { onRequestPost as syncPlayer } from "../../functions/api/sync/player.js";
import { markPremiumPassOwned } from "../../functions/_lib/premium-pass.js";
import { createTestDb } from "./test-d1.js";

const migrations = ["0001_init", "0003_sync_lp_privileges", "0005_skills_and_stats", "0006_customization_and_likes", "0007_pending_commands", "0012_pass_premium_owners"]
  .map((m) => new URL(`../../migrations/${m}.sql`, import.meta.url));

async function makeEnv(nick = "Steve", balance = 1000) {
  const db = createTestDb(migrations);
  await db.prepare("INSERT INTO users (nick, password_hash, password_salt) VALUES (?, 'x', '')").bind(nick).run();
  await db.prepare("INSERT INTO sessions (id, user_id, expires_at) VALUES ('sid', 1, '2099-01-01T00:00:00.000Z')").run();
  await db.prepare("CREATE TABLE IF NOT EXISTS rub_balances (nick TEXT PRIMARY KEY, balance INTEGER NOT NULL DEFAULT 0, updated_at TEXT)").run();
  await db.prepare("INSERT INTO rub_balances (nick, balance) VALUES (?, ?)").bind(nick, balance).run();
  return { DB: db, PURCHASES_ENABLED: "true", SERVER_SYNC_KEY: "k" };
}

function buy(env, body = { slug: "pass_premium", method: "balance" }) {
  return purchase({
    env,
    request: new Request("https://aquateche.store/api/purchase", {
      method: "POST",
      headers: { "content-type": "application/json", cookie: "at_session=sid" },
      body: JSON.stringify(body),
    }),
  });
}

const balance = async (env, nick = "Steve") => (await env.DB.prepare("SELECT balance FROM rub_balances WHERE nick = ?").bind(nick).first()).balance;
const queuedPasses = async (env) =>
  (await env.DB.prepare("SELECT COUNT(*) AS n FROM pending_commands WHERE kind = 'pass_premium' AND status = 'queued'").first()).n;

test("balance purchase of the pass charges 299 and queues one delivery", async () => {
  const env = await makeEnv();
  const res = await buy(env);
  assert.equal(res.status, 200);
  assert.equal(await balance(env), 1000 - 299);
  assert.equal(await queuedPasses(env), 1);
});

test("a second purchase is refused with 409 and costs nothing", async () => {
  const env = await makeEnv();
  await buy(env);
  const again = await buy(env);
  assert.equal(again.status, 409);
  assert.equal(await balance(env), 1000 - 299);
  assert.equal(await queuedPasses(env), 1);
});

test("a pass bought in the game (server sync) blocks the purchase before charging", async () => {
  const env = await makeEnv();
  await markPremiumPassOwned(env.DB, "steve", "game");
  const res = await buy(env);
  assert.equal(res.status, 409);
  assert.equal(await balance(env), 1000);
  assert.equal(await queuedPasses(env), 0);
});

test("a sync from the server with pass_premium blocks the card route too", async () => {
  const env = await makeEnv();
  await syncPlayer({
    env,
    request: new Request("https://aquateche.store/api/sync/player", {
      method: "POST",
      headers: { "content-type": "application/json", "X-AquaTech-Server-Key": "k" },
      body: JSON.stringify({ nick: "Steve", pass_premium: true }),
    }),
  });
  const res = await buy(env, { slug: "pass_premium", method: "sbp" });
  assert.equal(res.status, 409);
});

test("two parallel balance purchases: one delivery, the loser gets the money back", async () => {
  const env = await makeEnv();
  const [a, b] = await Promise.all([buy(env), buy(env)]);
  assert.deepEqual([a.status, b.status].sort(), [200, 409]);
  assert.equal(await queuedPasses(env), 1);
  assert.equal(await balance(env), 1000 - 299);
});

test("other products are not affected by the pass rules", async () => {
  const env = await makeEnv();
  await markPremiumPassOwned(env.DB, "Steve", "game");
  const res = await buy(env, { slug: "coins_10k", method: "balance" });
  assert.equal(res.status, 200);
});

async function seedInvoice(env, id, nick = "Steve", amount = 299) {
  await env.DB.prepare(
    "CREATE TABLE IF NOT EXISTS lava_invoices (id TEXT PRIMARY KEY, nick TEXT NOT NULL, amount INTEGER NOT NULL, kind TEXT NOT NULL, payload TEXT NOT NULL DEFAULT '', status TEXT NOT NULL DEFAULT 'pending', created_at TEXT)"
  ).run();
  await env.DB.prepare("INSERT INTO lava_invoices (id, nick, amount, kind, payload) VALUES (?, ?, ?, 'pass_premium', '')").bind(id, nick, amount).run();
}

function webhook(env, id, amount = 299) {
  return lavaCallback({
    env,
    request: new Request("https://aquateche.store/api/purchase/callback", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify({ eventType: "payment.success", contractId: id, currency: "RUB", amount }),
    }),
  });
}

test("webhook for a paid pass queues delivery once even when Lava repeats the call", async () => {
  const env = await makeEnv("Steve", 0);
  await seedInvoice(env, "inv1");
  const first = await (await webhook(env, "inv1")).json();
  assert.ok(first.queued);
  const second = await (await webhook(env, "inv1")).json();
  assert.equal(second.duplicate, true);
  assert.equal(await queuedPasses(env), 1);
  assert.equal(await balance(env), 0);
});

test("webhook for a pass the player already owns refunds the amount to the site balance", async () => {
  const env = await makeEnv("Steve", 0);
  await markPremiumPassOwned(env.DB, "Steve", "game");
  await seedInvoice(env, "inv2");
  const res = await (await webhook(env, "inv2")).json();
  assert.equal(res.refunded, true);
  assert.equal(await queuedPasses(env), 0);
  assert.equal(await balance(env), 299);
});
