import test from "node:test";
import assert from "node:assert/strict";
import { createRequire } from "node:module";

const require = createRequire(import.meta.url);
const T = require("../../docs/assets/js/tournament.js");

test("escapeHtml neutralizes markup and quotes", () => {
  assert.equal(T.escapeHtml(`<img src=x onerror="a('b')">&`), "&lt;img src=x onerror=&quot;a(&#39;b&#39;)&quot;&gt;&amp;");
});

test("formatWeight uses a decimal comma and two digits", () => {
  assert.equal(T.formatWeight(12.3), "12,30 кг");
  assert.equal(T.formatWeight(0.456), "0,46 кг");
});

test("formatCountdown handles days, hours and negatives", () => {
  assert.equal(T.formatCountdown(0), "00:00:00");
  assert.equal(T.formatCountdown(-5000), "00:00:00");
  assert.equal(T.formatCountdown(3_661_000), "01:01:01");
  assert.equal(T.formatCountdown(86_400_000 + 3_661_000), "1д 01:01:01");
});

test("statusView picks the right target for each state", () => {
  const now = Date.parse("2026-09-26T12:00:00.000Z");
  const active = { status: "active", ends_at: "2026-09-27T21:00:00.000Z", next_starts_at: "2026-10-02T21:00:00.000Z" };
  const a = T.statusView(active, 0, now);
  assert.equal(a.label, "До конца турнира");
  assert.equal(a.remainingMs, Date.parse(active.ends_at) - now);

  const done = { ...active, status: "finalized" };
  const d = T.statusView(done, 60_000, now);
  assert.equal(d.label, "До следующего турнира");
  assert.equal(d.remainingMs, Date.parse(done.next_starts_at) - (now + 60_000));

  assert.equal(T.statusView(null, 0, now).label, "Турнир ещё не проводился");
  assert.equal(T.statusView({ ...done, next_starts_at: null }, 0, now).remainingMs, null);
});

test("boardRowsHtml escapes nicks and marks the prize places", () => {
  const html = T.boardRowsHtml([
    { uuid: "1", nick: `<b>x</b>`, weight: 15.5, fish: `Ту"нец` },
    { uuid: "2", nick: "b", weight: 9, fish: "Окунь" },
    { uuid: "3", nick: "c", weight: 8, fish: "Карп" },
    { uuid: "4", nick: "d", weight: 7, fish: "Ерш" },
  ]);
  assert.equal(html.includes("<b>x</b>"), false);
  assert.ok(html.includes("&lt;b&gt;x&lt;/b&gt;"));
  assert.ok(html.includes("2500"));
  assert.ok(html.includes("Кейса IV"));
  assert.ok(html.includes("500"));
  assert.equal((html.match(/class="t-row/g) || []).length, 4);
  assert.equal(T.boardRowsHtml([]).includes("Пока никто не поймал рыбу"), true);
});

test("heroHtml and hallHtml handle empty data", () => {
  assert.ok(T.heroHtml(undefined, false).includes("Ждём первую рыбу"));
  assert.ok(T.heroHtml({ nick: "a<b", weight: 3, fish: "f" }, true).includes("a&lt;b"));
  assert.ok(T.hallHtml(null).includes("Архив появится"));
  assert.ok(T.hallHtml({ weeks: [], record: null, wins: [] }).includes("Архив появится"));
});
