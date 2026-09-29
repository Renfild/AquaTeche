export const TOURNAMENT_TOP_LIMIT = 10;

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function fail(error) {
  return { ok: false, error };
}

/** null = absent, undefined = present but invalid, string = normalized ISO date. */
function isoOrNull(value) {
  if (value === undefined || value === null || value === "") return null;
  const ms = Date.parse(String(value));
  return Number.isFinite(ms) ? new Date(ms).toISOString() : undefined;
}

export function validateSnapshot(body) {
  if (!body || typeof body !== "object") return fail("Пустое тело запроса");

  const week = Number(body.week);
  const year = Math.floor(week / 100);
  const isoWeek = week % 100;
  if (!Number.isInteger(week) || year < 2000 || year > 2100 || isoWeek < 1 || isoWeek > 53) {
    return fail("Некорректная неделя");
  }
  if (body.status !== "active" && body.status !== "finalized") return fail("Некорректный статус");

  const endsAt = isoOrNull(body.ends_at);
  if (!endsAt) return fail("Некорректный ends_at");
  const nextStartsAt = isoOrNull(body.next_starts_at);
  if (nextStartsAt === undefined) return fail("Некорректный next_starts_at");

  if (!Array.isArray(body.top)) return fail("top должен быть массивом");
  if (body.top.length > TOURNAMENT_TOP_LIMIT) return fail("Слишком длинный top");

  const seen = new Set();
  const top = [];
  for (const raw of body.top) {
    if (!raw || typeof raw !== "object") return fail("Некорректная запись top");
    const uuid = String(raw.uuid || "").toLowerCase();
    if (!UUID_RE.test(uuid)) return fail("Некорректный uuid");
    if (seen.has(uuid)) return fail("Повторяющийся uuid");
    seen.add(uuid);
    const nick = String(raw.nick ?? "").trim();
    if (nick.length < 1 || nick.length > 32) return fail("Некорректный ник");
    const weight = Number(raw.weight);
    if (!Number.isFinite(weight) || weight <= 0 || weight > 1000) return fail("Некорректный вес");
    top.push({ uuid, nick, weight, fish: String(raw.fish ?? "").slice(0, 64) });
  }

  return { ok: true, value: { week, status: body.status, endsAt, nextStartsAt, top } };
}

export function groupHistory(weeks, entries) {
  const byWeek = new Map();
  for (const entry of entries) {
    if (!byWeek.has(entry.week)) byWeek.set(entry.week, []);
    byWeek.get(entry.week).push(entry);
  }
  const out = [];
  for (const w of weeks) {
    const rows = (byWeek.get(w.week) || []).slice(0, 3);
    if (rows.length === 0) continue;
    out.push({
      week: w.week,
      ends_at: w.ends_at,
      podium: rows.map((r, i) => ({ place: i + 1, uuid: r.uuid, nick: r.nick, weight: r.weight, fish: r.fish })),
    });
  }
  return out;
}
