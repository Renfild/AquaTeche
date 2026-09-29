import test from "node:test";
import assert from "node:assert/strict";
import { onRequestPost as syncPost } from "../../functions/api/sync/tournament.js";
import { onRequestGet as getTournament } from "../../functions/api/tournament.js";
import { createTestDb, MIGRATION_0011 } from "./test-d1.js";

export const KEY = "test-key";
export const UUID_A = "2102d9ae-dcb0-3598-8846-c4e76d4134fd";
export const UUID_B = "3b5e083d-8b48-33d6-8f3c-85ab1ebb9b84";

export function makeEnv() {
  return { DB: createTestDb([MIGRATION_0011]), SERVER_SYNC_KEY: KEY };
}

export function snapshot(overrides = {}) {
  return {
    week: 202639,
    status: "active",
    ends_at: "2026-09-27T21:00:00.000Z",
    next_starts_at: "2026-10-02T21:00:00.000Z",
    top: [
      { uuid: UUID_A, nick: "xietoru", weight: 12.34, fish: "Тунец" },
      { uuid: UUID_B, nick: "renfild", weight: 15.5, fish: "Окунь" },
    ],
    ...overrides,
  };
}

export async function post(env, body, key = KEY) {
  const request = new Request("https://aquateche.store/api/sync/tournament", {
    method: "POST",
    headers: { "content-type": "application/json", "X-AquaTech-Server-Key": key },
    body: JSON.stringify(body),
  });
  return syncPost({ request, env });
}

test("sync: 503 without DB, 403 with a wrong key", async () => {
  const noDb = await post({ SERVER_SYNC_KEY: KEY }, snapshot());
  assert.equal(noDb.status, 503);
  const wrong = await post(makeEnv(), snapshot(), "nope");
  assert.equal(wrong.status, 403);
});

test("sync: 400 on an invalid snapshot", async () => {
  const res = await post(makeEnv(), snapshot({ top: [{ uuid: UUID_A, nick: "a", weight: -1 }] }));
  assert.equal(res.status, 400);
});

test("sync: stores the snapshot and is idempotent", async () => {
  const env = makeEnv();
  const first = await (await post(env, snapshot())).json();
  assert.deepEqual(first, { ok: true, week: 202639, status: "active", entries: 2 });
  await post(env, snapshot());
  const rows = await env.DB.prepare("SELECT COUNT(*) AS n FROM tournament_entries WHERE week = ?").bind(202639).first();
  assert.equal(rows.n, 2);
});

test("sync: a new snapshot fully replaces the week's entries", async () => {
  const env = makeEnv();
  await post(env, snapshot());
  await post(env, snapshot({ top: [{ uuid: UUID_A, nick: "xietoru", weight: 20, fish: "Тунец" }] }));
  const rows = await env.DB.prepare("SELECT uuid, weight FROM tournament_entries WHERE week = ?").bind(202639).all();
  assert.deepEqual(rows.results, [{ uuid: UUID_A, weight: 20 }]);
});

test("sync: a finalized week ignores later snapshots", async () => {
  const env = makeEnv();
  await post(env, snapshot({ status: "finalized" }));
  const res = await (await post(env, snapshot({ status: "active", top: [] }))).json();
  assert.equal(res.ignored, true);
  const week = await env.DB.prepare("SELECT status FROM tournament_weeks WHERE week = ?").bind(202639).first();
  assert.equal(week.status, "finalized");
  const rows = await env.DB.prepare("SELECT COUNT(*) AS n FROM tournament_entries WHERE week = ?").bind(202639).first();
  assert.equal(rows.n, 2);
});

async function get(env, query = "") {
  const request = new Request(`https://aquateche.store/api/tournament${query}`);
  return getTournament({ request, env });
}

test("get: 503 without DB and null current when nothing is stored", async () => {
  assert.equal((await get({})).status, 503);
  const body = await (await get(makeEnv())).json();
  assert.equal(body.ok, true);
  assert.equal(body.current, null);
  assert.ok(Number.isFinite(Date.parse(body.server_time)));
  assert.equal("history" in body, false);
});

test("get: current week comes back sorted by weight then nick", async () => {
  const env = makeEnv();
  await post(env, snapshot());
  const body = await (await get(env)).json();
  assert.equal(body.current.week, 202639);
  assert.equal(body.current.status, "active");
  assert.equal(body.current.ends_at, "2026-09-27T21:00:00.000Z");
  assert.equal(body.current.next_starts_at, "2026-10-02T21:00:00.000Z");
  assert.deepEqual(body.current.top.map((e) => e.nick), ["renfild", "xietoru"]);
});

test("get: equal weights are ordered by nick", async () => {
  const env = makeEnv();
  await post(
    env,
    snapshot({
      top: [
        { uuid: UUID_A, nick: "zeta", weight: 10, fish: "" },
        { uuid: UUID_B, nick: "alpha", weight: 10, fish: "" },
      ],
    })
  );
  const body = await (await get(env)).json();
  assert.deepEqual(body.current.top.map((e) => e.nick), ["alpha", "zeta"]);
});

test("get: an empty finalized week is served as current but not archived", async () => {
  const env = makeEnv();
  await post(env, snapshot({ status: "finalized", top: [] }));
  const body = await (await get(env, "?history=12")).json();
  assert.equal(body.current.status, "finalized");
  assert.deepEqual(body.current.top, []);
  assert.deepEqual(body.history.weeks, []);
  assert.equal(body.history.record, null);
  assert.deepEqual(body.history.wins, []);
});

test("get: history returns podiums, the all-time record and win counts", async () => {
  const env = makeEnv();
  const at = (week) => ({ week, status: "finalized", ends_at: `2026-0${week - 202630}-01T00:00:00.000Z` });
  await post(env, { ...snapshot(at(202637)), top: [
    { uuid: UUID_A, nick: "xietoru", weight: 10, fish: "Щука" },
    { uuid: UUID_B, nick: "renfild", weight: 8, fish: "Карп" },
  ] });
  await post(env, { ...snapshot(at(202638)), top: [
    { uuid: UUID_B, nick: "renfild", weight: 12, fish: "Окунь" },
    { uuid: UUID_A, nick: "xietoru", weight: 9, fish: "Плотва" },
  ] });
  await post(env, { ...snapshot(at(202639)), top: [
    { uuid: UUID_A, nick: "xietoru", weight: 15, fish: "Тунец" },
  ] });
  await post(env, snapshot({ week: 202640, status: "active" }));

  const body = await (await get(env, "?history=2")).json();
  assert.equal(body.current.week, 202640);
  assert.deepEqual(body.history.weeks.map((w) => w.week), [202639, 202638]);
  assert.equal(body.history.weeks[1].podium[0].nick, "renfild");
  assert.deepEqual(
    { nick: body.history.record.nick, weight: body.history.record.weight, week: body.history.record.week },
    { nick: "xietoru", weight: 15, week: 202639 }
  );
  assert.deepEqual(body.history.wins.map((w) => [w.nick, w.wins]), [["xietoru", 2], ["renfild", 1]]);
});

test("get: history parameter is clamped and ignored when invalid", async () => {
  const env = makeEnv();
  await post(env, snapshot({ status: "finalized" }));
  assert.equal("history" in (await (await get(env, "?history=abc")).json()), false);
  assert.equal("history" in (await (await get(env, "?history=0")).json()), false);
  const big = await (await get(env, "?history=9999")).json();
  assert.equal(big.history.weeks.length, 1);
});
