import test from "node:test";
import assert from "node:assert/strict";
import {
  isPremiumPassOwned,
  markPremiumPassOwned,
  isDuplicatePremiumCommand,
  enqueuePremiumPass,
} from "../../functions/_lib/premium-pass.js";
import { enqueueCommand } from "../../functions/api/internal/pending-commands.js";
import { onRequestPost as syncPlayer } from "../../functions/api/sync/player.js";
import { createTestDb } from "./test-d1.js";

const M7 = new URL("../../migrations/0007_pending_commands.sql", import.meta.url);
const M12 = new URL("../../migrations/0012_pass_premium_owners.sql", import.meta.url);

function makeDb() {
  return createTestDb([M7, M12]);
}

async function queue(db, nick, status = "queued", kind = "pass_premium") {
  const res = await enqueueCommand(db, { nick, kind, payload: "", provider: "balance", providerPaymentId: `p_${Math.random()}` });
  if (status !== "queued") {
    await db.prepare("UPDATE pending_commands SET status = ? WHERE id = ?").bind(status, res.id).run();
  }
  return res.id;
}

test("nobody owns the pass by default", async () => {
  assert.equal(await isPremiumPassOwned(makeDb(), "Steve"), false);
});

test("a queued or delivered site purchase counts as owned, case-insensitive", async () => {
  const db = makeDb();
  await queue(db, "Steve", "queued");
  assert.equal(await isPremiumPassOwned(db, "steve"), true);
  const db2 = makeDb();
  await queue(db2, "Alex", "done");
  assert.equal(await isPremiumPassOwned(db2, "ALEX"), true);
});

test("a failed delivery does not count, other kinds do not count", async () => {
  const db = makeDb();
  await queue(db, "Steve", "failed");
  await queue(db, "Steve", "queued", "coins");
  assert.equal(await isPremiumPassOwned(db, "Steve"), false);
});

test("an in-game purchase reported by the server counts, marking twice is harmless", async () => {
  const db = makeDb();
  await markPremiumPassOwned(db, "Steve", "game");
  await markPremiumPassOwned(db, "steve", "game");
  assert.equal(await isPremiumPassOwned(db, "STEVE"), true);
  assert.equal(await isPremiumPassOwned(db, "Alex"), false);
  const rows = await db.prepare("SELECT COUNT(*) AS n FROM pass_premium_owners").first();
  assert.equal(rows.n, 1);
});

test("the later of two concurrent premium commands is the duplicate", async () => {
  const db = makeDb();
  const first = await queue(db, "Steve");
  const second = await queue(db, "steve");
  assert.equal(await isDuplicatePremiumCommand(db, "Steve", first), false);
  assert.equal(await isDuplicatePremiumCommand(db, "Steve", second), true);
});

test("a failed earlier command does not make a new one a duplicate", async () => {
  const db = makeDb();
  await queue(db, "Steve", "failed");
  const second = await queue(db, "Steve");
  assert.equal(await isDuplicatePremiumCommand(db, "Steve", second), false);
});

test("server sync with pass_premium=true marks the player as owner; false never clears it", async () => {
  const db = createTestDb(["0001_init", "0003_sync_lp_privileges", "0005_skills_and_stats", "0006_customization_and_likes"].map((m) => new URL(`../../migrations/${m}.sql`, import.meta.url)).concat([M7, M12]));
  const env = { DB: db, SERVER_SYNC_KEY: "k" };
  const post = (body) =>
    syncPlayer({
      request: new Request("https://aquateche.store/api/sync/player", {
        method: "POST",
        headers: { "content-type": "application/json", "X-AquaTech-Server-Key": "k" },
        body: JSON.stringify(body),
      }),
      env,
    });
  assert.equal((await post({ nick: "Steve", coins: 1 })).status, 200);
  assert.equal(await isPremiumPassOwned(db, "Steve"), false);
  assert.equal((await post({ nick: "Steve", coins: 1, pass_premium: true })).status, 200);
  assert.equal(await isPremiumPassOwned(db, "Steve"), true);
  assert.equal((await post({ nick: "Steve", coins: 1, pass_premium: false })).status, 200);
  assert.equal(await isPremiumPassOwned(db, "Steve"), true);
});

test("enqueuePremiumPass queues the first purchase", async () => {
  const db = makeDb();
  const res = await enqueuePremiumPass(db, { nick: "Steve", provider: "balance", providerPaymentId: "a" });
  assert.equal(res.ok, true);
  const row = await db.prepare("SELECT kind, status FROM pending_commands WHERE id = ?").bind(res.id).first();
  assert.deepEqual(row, { kind: "pass_premium", status: "queued" });
});

test("enqueuePremiumPass refuses an owner without queueing anything", async () => {
  const db = makeDb();
  await markPremiumPassOwned(db, "Steve", "game");
  const res = await enqueuePremiumPass(db, { nick: "steve", provider: "balance", providerPaymentId: "a" });
  assert.deepEqual(res, { ok: false, reason: "owned" });
  const n = await db.prepare("SELECT COUNT(*) AS n FROM pending_commands").first();
  assert.equal(n.n, 0);
});

test("enqueuePremiumPass: the second purchase is refused and does not stay queued", async () => {
  const db = makeDb();
  await enqueuePremiumPass(db, { nick: "Steve", provider: "balance", providerPaymentId: "a" });
  const second = await enqueuePremiumPass(db, { nick: "Steve", provider: "lava_webhook", providerPaymentId: "b" });
  assert.deepEqual(second, { ok: false, reason: "owned" });
  const live = await db.prepare("SELECT COUNT(*) AS n FROM pending_commands WHERE status = 'queued'").first();
  assert.equal(live.n, 1);
});

test("enqueuePremiumPass: two parallel purchases deliver exactly once", async () => {
  const db = makeDb();
  const results = await Promise.all([
    enqueuePremiumPass(db, { nick: "Steve", provider: "balance", providerPaymentId: "a" }),
    enqueuePremiumPass(db, { nick: "Steve", provider: "balance", providerPaymentId: "b" }),
    enqueuePremiumPass(db, { nick: "steve", provider: "lava_webhook", providerPaymentId: "c" }),
  ]);
  assert.equal(results.filter((r) => r.ok).length, 1);
  assert.ok(results.filter((r) => !r.ok).every((r) => r.reason === "owned"));
  const live = await db.prepare("SELECT COUNT(*) AS n FROM pending_commands WHERE status = 'queued'").first();
  assert.equal(live.n, 1);
});

test("enqueuePremiumPass: a repeated webhook with the same payment id is not delivered twice", async () => {
  const db = makeDb();
  const first = await enqueuePremiumPass(db, { nick: "Steve", provider: "lava_webhook", providerPaymentId: "same" });
  assert.equal(first.ok, true);
  const again = await enqueuePremiumPass(db, { nick: "Steve", provider: "lava_webhook", providerPaymentId: "same" });
  assert.deepEqual(again, { ok: false, reason: "owned" });
});
