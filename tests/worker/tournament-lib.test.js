import test from "node:test";
import assert from "node:assert/strict";
import { validateSnapshot, groupHistory, TOURNAMENT_TOP_LIMIT } from "../../functions/_lib/tournament.js";

const UUID_A = "2102d9ae-dcb0-3598-8846-c4e76d4134fd";
const UUID_B = "3b5e083d-8b48-33d6-8f3c-85ab1ebb9b84";

function snapshot(overrides = {}) {
  return {
    week: 202639,
    status: "active",
    ends_at: "2026-09-27T21:00:00.000Z",
    next_starts_at: "2026-10-02T21:00:00.000Z",
    top: [{ uuid: UUID_A, nick: "xietoru", weight: 12.34, fish: "Тунец" }],
    ...overrides,
  };
}

test("accepts a valid snapshot and normalizes it", () => {
  const r = validateSnapshot(snapshot({ top: [{ uuid: UUID_A.toUpperCase(), nick: "  xietoru ", weight: "12.34", fish: "Тунец" }] }));
  assert.equal(r.ok, true);
  assert.deepEqual(r.value.top, [{ uuid: UUID_A, nick: "xietoru", weight: 12.34, fish: "Тунец" }]);
  assert.equal(r.value.week, 202639);
  assert.equal(r.value.endsAt, "2026-09-27T21:00:00.000Z");
  assert.equal(r.value.nextStartsAt, "2026-10-02T21:00:00.000Z");
});

test("accepts an empty top and a missing next_starts_at", () => {
  const r = validateSnapshot(snapshot({ top: [], next_starts_at: undefined }));
  assert.equal(r.ok, true);
  assert.deepEqual(r.value.top, []);
  assert.equal(r.value.nextStartsAt, null);
});

test("rejects bad week, status and dates", () => {
  assert.equal(validateSnapshot(snapshot({ week: 202600 })).ok, false);
  assert.equal(validateSnapshot(snapshot({ week: 202654 })).ok, false);
  assert.equal(validateSnapshot(snapshot({ week: "abc" })).ok, false);
  assert.equal(validateSnapshot(snapshot({ status: "done" })).ok, false);
  assert.equal(validateSnapshot(snapshot({ ends_at: "not-a-date" })).ok, false);
  assert.equal(validateSnapshot(snapshot({ ends_at: undefined })).ok, false);
  assert.equal(validateSnapshot(snapshot({ next_starts_at: "garbage" })).ok, false);
  assert.equal(validateSnapshot(null).ok, false);
});

test("rejects a top longer than the limit", () => {
  const top = Array.from({ length: TOURNAMENT_TOP_LIMIT + 1 }, (_, i) => ({
    uuid: `00000000-0000-4000-8000-${String(i).padStart(12, "0")}`,
    nick: `p${i}`,
    weight: 1 + i,
    fish: "",
  }));
  assert.equal(validateSnapshot(snapshot({ top })).ok, false);
});

test("rejects duplicate uuids, bad uuids, bad nicks and bad weights", () => {
  const dup = [
    { uuid: UUID_A, nick: "a", weight: 1, fish: "" },
    { uuid: UUID_A.toUpperCase(), nick: "b", weight: 2, fish: "" },
  ];
  assert.equal(validateSnapshot(snapshot({ top: dup })).ok, false);
  assert.equal(validateSnapshot(snapshot({ top: [{ uuid: "nope", nick: "a", weight: 1 }] })).ok, false);
  assert.equal(validateSnapshot(snapshot({ top: [{ uuid: UUID_A, nick: "   ", weight: 1 }] })).ok, false);
  assert.equal(validateSnapshot(snapshot({ top: [{ uuid: UUID_A, nick: "x".repeat(33), weight: 1 }] })).ok, false);
  assert.equal(validateSnapshot(snapshot({ top: [{ uuid: UUID_A, nick: "a", weight: 0 }] })).ok, false);
  assert.equal(validateSnapshot(snapshot({ top: [{ uuid: UUID_A, nick: "a", weight: -3 }] })).ok, false);
  assert.equal(validateSnapshot(snapshot({ top: [{ uuid: UUID_A, nick: "a", weight: 1001 }] })).ok, false);
  assert.equal(validateSnapshot(snapshot({ top: [{ uuid: UUID_A, nick: "a", weight: NaN }] })).ok, false);
});

test("stores hostile nicks verbatim (escaping is the view's job)", () => {
  const nick = `<img src=x onerror="alert(1)">`;
  const r = validateSnapshot(snapshot({ top: [{ uuid: UUID_A, nick, weight: 5, fish: "" }] }));
  assert.equal(r.ok, true);
  assert.equal(r.value.top[0].nick, nick);
});

test("groupHistory keeps a podium of three and skips empty weeks", () => {
  const weeks = [
    { week: 202639, ends_at: "2026-09-27T21:00:00.000Z" },
    { week: 202638, ends_at: "2026-09-20T21:00:00.000Z" },
    { week: 202637, ends_at: "2026-09-13T21:00:00.000Z" },
  ];
  const entries = [
    { week: 202639, uuid: UUID_A, nick: "a", weight: 15, fish: "Тунец" },
    { week: 202639, uuid: UUID_B, nick: "b", weight: 9, fish: "Окунь" },
    { week: 202637, uuid: UUID_A, nick: "a", weight: 10, fish: "Щука" },
    { week: 202637, uuid: UUID_B, nick: "b", weight: 8, fish: "Карп" },
    { week: 202637, uuid: "11111111-1111-4111-8111-111111111111", nick: "c", weight: 7, fish: "" },
    { week: 202637, uuid: "22222222-2222-4222-8222-222222222222", nick: "d", weight: 6, fish: "" },
  ];
  const out = groupHistory(weeks, entries);
  assert.deepEqual(out.map((w) => w.week), [202639, 202637]);
  assert.equal(out[1].podium.length, 3);
  assert.deepEqual(out[0].podium[0], { place: 1, uuid: UUID_A, nick: "a", weight: 15, fish: "Тунец" });
  assert.equal(out[1].podium[2].place, 3);
});
