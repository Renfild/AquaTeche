import { bad, json } from "../_lib/http.js";
import { groupHistory, TOURNAMENT_TOP_LIMIT } from "../_lib/tournament.js";

const HISTORY_MAX = 52;

function historyLimit(raw) {
  const n = Number.parseInt(raw ?? "", 10);
  if (!Number.isFinite(n) || n <= 0) return 0;
  return Math.min(n, HISTORY_MAX);
}

async function loadHistory(db, limit) {
  const weeksRes = await db
    .prepare("SELECT week, ends_at FROM tournament_weeks WHERE status = 'finalized' ORDER BY week DESC LIMIT ?")
    .bind(limit)
    .all();
  const weeks = weeksRes.results;

  let entries = [];
  if (weeks.length > 0) {
    const marks = weeks.map(() => "?").join(",");
    const res = await db
      .prepare(
        `SELECT week, uuid, nick, weight, fish FROM tournament_entries
         WHERE week IN (${marks}) ORDER BY week DESC, weight DESC, nick ASC`
      )
      .bind(...weeks.map((w) => w.week))
      .all();
    entries = res.results;
  }

  const record = await db
    .prepare(
      `SELECT e.week, e.nick, e.weight, e.fish
       FROM tournament_entries e JOIN tournament_weeks w ON w.week = e.week
       WHERE w.status = 'finalized' ORDER BY e.weight DESC, e.week DESC LIMIT 1`
    )
    .first();

  const wins = await db
    .prepare(
      `SELECT e.uuid AS uuid, MAX(e.nick) AS nick, COUNT(*) AS wins
       FROM tournament_entries e JOIN tournament_weeks w ON w.week = e.week
       WHERE w.status = 'finalized'
         AND e.weight = (SELECT MAX(x.weight) FROM tournament_entries x WHERE x.week = e.week)
       GROUP BY e.uuid ORDER BY wins DESC, nick ASC LIMIT 5`
    )
    .all();

  return { weeks: groupHistory(weeks, entries), record: record || null, wins: wins.results };
}

export async function onRequestGet(context) {
  const { request, env } = context;
  if (!env.DB) return bad("База не подключена (D1)", 503);

  const limit = historyLimit(new URL(request.url).searchParams.get("history"));

  const week = await env.DB
    .prepare("SELECT week, status, ends_at, next_starts_at FROM tournament_weeks ORDER BY week DESC LIMIT 1")
    .first();

  let current = null;
  if (week) {
    const rows = await env.DB
      .prepare(
        "SELECT uuid, nick, weight, fish FROM tournament_entries WHERE week = ? ORDER BY weight DESC, nick ASC LIMIT ?"
      )
      .bind(week.week, TOURNAMENT_TOP_LIMIT)
      .all();
    current = { ...week, top: rows.results };
  }

  const body = { ok: true, server_time: new Date().toISOString(), current };
  if (limit > 0) body.history = await loadHistory(env.DB, limit);
  return json(body, 200, { "cache-control": "public, max-age=10" });
}
