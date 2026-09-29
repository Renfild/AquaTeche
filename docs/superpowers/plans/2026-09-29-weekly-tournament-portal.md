# Weekly Tournament Portal Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Живая таблица недельного турнира на сайте (топ-10), кейс-ключи в призах, архив прошлых недель, без потери призов у офлайн-победителей.

**Architecture:** Мод `aquatech-ui` держит состояние турнира и шлёт полный снимок (`POST /api/sync/tournament`, ключ сервера) в D1 через воркер; сайт читает `GET /api/tournament`. Чистая логика (ISO-неделя, топ-10, призы, снимок) вынесена в `TournamentLogic` без Minecraft-зависимостей и покрыта JUnit; воркер-логика покрыта `node --test` на реальном SQLite (`node:sqlite`) через D1-адаптер.

**Tech Stack:** Java 17 / Forge 1.20.1 (ForgeGradle 6) + JUnit 5, Cloudflare Worker + D1 (SQLite), ручной HTML/CSS/JS, Node 24 (`node --test`).

**Spec:** `docs/superpowers/specs/2026-09-29-weekly-tournament-portal-design.md`

## Global Constraints

- Никаких compile-зависимостей между `mods/aquatech-ui`, `mods/aqualumen-ui`, `mods/aquatech-machines`; связь только через NBT/файлы.
- Не выполнять `git commit`, `git push`, деплой и миграции без прямого приказа владельца (AGENTS.md §6). Шаги «Checkpoint» только проверяют `git status`.
- `tools/generate_site.py` не вызывать; `docs/*.html` — ручной HTML; `hub.html`, `manifest.json`, `cases.json` руками не править.
- Любой новый роут воркера обязательно регистрируется в `worker/index.js`.
- Призы: монеты 2500/1000/500 + ключи кейсов `flora` / `steam` / `smeltery` за места 1/2/3; таблица на сайте — топ-10, одна лучшая рыба на игрока; идентификатор игрока — `uuid`.
- Проверки: `./gradlew build` в `mods/aquatech-ui`, `node --check <file>` для JS, `node --test "tests/**/*.test.js"` (glob в кавычках).
- Сохранять CRLF там, где файл уже CRLF (проверять `git diff` на «весь файл изменён»); минимальный дифф.
- Деплой мода — не при запущенном клиенте Minecraft; выкатывать в будни (Пн–Пт): смена схемы недели посреди выходных сбросит текущий топ.

## Review Focus

- Пустая неделя (никто не поймал рыбу): статус `finalized` с пустым `top` не должен ломать GET и не должен попадать в архив как «победитель» → Task 3 (тест `empty finalized week`).
- Враждебные данные: ник с `<script>`/кавычками и повторяющийся `uuid` в снимке → валидатор отклоняет дубли (Task 1), страница экранирует ник (Task 7).
- Старый `config/aquatech_tournament.json` (топ-3, без новых полей) должен загружаться без потери данных → Task 4 (тест `loads legacy state file`).
- Офлайн-победитель получает приз ровно один раз при входе, повторный вход ничего не выдаёт → Task 4 (`claimable`/`removeClaimed`), Task 6 (ручная проверка).
- Рестарт сервера в понедельник после 00:00: итоги выдаются на первом тике ровно один раз (флаг `awarded`) → Task 6 (ручная проверка).

## File Structure

| Файл | Ответственность |
|---|---|
| `migrations/0011_tournament.sql` | таблицы `tournament_weeks`, `tournament_entries` |
| `functions/_lib/tournament.js` | `validateSnapshot`, `groupHistory`, `TOURNAMENT_TOP_LIMIT` |
| `functions/api/sync/tournament.js` | `POST /api/sync/tournament` (приём снимка) |
| `functions/api/tournament.js` | `GET /api/tournament` (текущая неделя + архив) |
| `worker/index.js` | регистрация двух роутов |
| `tests/worker/test-d1.js` | D1-совместимая обёртка над `node:sqlite` |
| `tests/worker/tournament-lib.test.js`, `tests/worker/tournament-endpoints.test.js` | тесты воркера |
| `scratch/tournament_dev_server.mjs` | локальный «портал» (не коммитится) для e2e и превью |
| `mods/aquatech-ui/build.gradle` | JUnit 5, `mavenCentral()` |
| `.../fishing/TournamentLogic.java` | чистая логика турнира |
| `.../fishing/TournamentState.java` | состояние турнира (Gson) |
| `.../fishing/PortalTournamentSync.java` | асинхронная отправка снимков |
| `.../server/auth/PortalSessionVerifier.java` | `readSyncKey()` становится `public` |
| `.../fishing/OceanEventsService.java` | интеграция (неделя, топ-10, призы, синк) |
| `docs/tournament.html`, `docs/assets/js/tournament.js`, `docs/assets/css/tournament.css` | страница |
| `tests/site/tournament.test.js` | тесты чистых функций страницы |
| `docs/assets/js/site.js`, `docs/events.html` | меню и ссылка |

---

### Task 1: Валидатор снимка и группировка архива (воркер, чистая логика)

**Files:**
- Create: `functions/_lib/tournament.js`
- Test: `tests/worker/tournament-lib.test.js`

**Interfaces:**
- Produces:
  - `TOURNAMENT_TOP_LIMIT = 10`
  - `validateSnapshot(body) -> {ok:false,error:string} | {ok:true,value:{week:number,status:"active"|"finalized",endsAt:string,nextStartsAt:string|null,top:{uuid:string,nick:string,weight:number,fish:string}[]}}`
  - `groupHistory(weeks:{week,ends_at}[], entries:{week,uuid,nick,weight,fish}[]) -> {week,ends_at,podium:{place,uuid,nick,weight,fish}[]}[]` (entries уже отсортированы `week DESC, weight DESC, nick ASC`; недели без записей пропускаются, подиум — максимум 3)

- [ ] **Step 1: Write the failing test**

Create `tests/worker/tournament-lib.test.js`:

```js
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test tests/worker/tournament-lib.test.js`
Expected: FAIL (`Cannot find module '.../functions/_lib/tournament.js'`).

- [ ] **Step 3: Write minimal implementation**

Create `functions/_lib/tournament.js`:

```js
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
```

- [ ] **Step 4: Run test to verify it passes**

Run: `node --test tests/worker/tournament-lib.test.js` and `node --check functions/_lib/tournament.js`
Expected: все тесты PASS, `node --check` без вывода.

- [ ] **Step 5: Checkpoint**

Run: `git status --short` — только `functions/_lib/tournament.js` и `tests/worker/tournament-lib.test.js` новые. Не коммитить без приказа владельца.

---

### Task 2: Миграция, D1-адаптер и приём снимка (`POST /api/sync/tournament`)

**Files:**
- Create: `migrations/0011_tournament.sql`, `tests/worker/test-d1.js`, `functions/api/sync/tournament.js`
- Test: `tests/worker/tournament-endpoints.test.js`

**Interfaces:**
- Consumes: `validateSnapshot` из Task 1; `bad`, `json`, `readJson` из `functions/_lib/http.js`.
- Produces:
  - Схема: `tournament_weeks(week PK, ends_at, next_starts_at NULL, status, updated_at)`, `tournament_entries(week, uuid, nick, weight, fish, PK(week,uuid))`.
  - `onRequestPost(context)` для `POST /api/sync/tournament`: 503 без БД, 403 при неверном ключе, 400 при невалидном теле, `{ok:true, ignored:true}` для завершённой недели, иначе `{ok:true, week, status, entries}`.
  - `createTestDb(migrationUrls:URL[]) -> {prepare, batch}` (D1-совместимый), `MIGRATION_0011:URL`.

- [ ] **Step 1: Write the failing test**

Create `tests/worker/test-d1.js`:

```js
import { DatabaseSync } from "node:sqlite";
import { readFileSync } from "node:fs";

export const MIGRATION_0011 = new URL("../../migrations/0011_tournament.sql", import.meta.url);

/** Минимальная D1-совместимая обёртка над node:sqlite: prepare().bind().first()/all()/run() и batch(). */
export function createTestDb(migrations) {
  const db = new DatabaseSync(":memory:");
  for (const file of migrations) db.exec(readFileSync(file, "utf8"));

  const stmt = (sql, binds = []) => ({
    bind: (...b) => stmt(sql, b),
    first: async () => {
      const row = db.prepare(sql).get(...binds);
      return row ? { ...row } : null;
    },
    all: async () => ({ results: db.prepare(sql).all(...binds).map((r) => ({ ...r })) }),
    run: async () => {
      const r = db.prepare(sql).run(...binds);
      return { meta: { last_row_id: Number(r.lastInsertRowid), changes: Number(r.changes) } };
    },
  });

  return {
    prepare: (sql) => stmt(sql),
    batch: async (statements) => {
      db.exec("BEGIN");
      try {
        const out = [];
        for (const s of statements) out.push(await s.run());
        db.exec("COMMIT");
        return out;
      } catch (err) {
        db.exec("ROLLBACK");
        throw err;
      }
    },
  };
}
```

Create `tests/worker/tournament-endpoints.test.js` (GET-тесты добавятся в Task 3 в этот же файл):

```js
import test from "node:test";
import assert from "node:assert/strict";
import { onRequestPost as syncPost } from "../../functions/api/sync/tournament.js";
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test tests/worker/tournament-endpoints.test.js`
Expected: FAIL (`Cannot find module '.../functions/api/sync/tournament.js'`).

- [ ] **Step 3: Write minimal implementation**

Create `migrations/0011_tournament.sql`:

```sql
-- AquaTech D1 Migration 0011: Weekly fishing tournament (live board + archive)

CREATE TABLE IF NOT EXISTS tournament_weeks (
  week INTEGER PRIMARY KEY,
  ends_at TEXT NOT NULL,
  next_starts_at TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'finalized')),
  updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE TABLE IF NOT EXISTS tournament_entries (
  week INTEGER NOT NULL,
  uuid TEXT NOT NULL,
  nick TEXT NOT NULL,
  weight REAL NOT NULL,
  fish TEXT NOT NULL DEFAULT '',
  PRIMARY KEY (week, uuid)
);

CREATE INDEX IF NOT EXISTS idx_tournament_entries_weight ON tournament_entries (week, weight DESC);
CREATE INDEX IF NOT EXISTS idx_tournament_entries_uuid ON tournament_entries (uuid);
```

Create `functions/api/sync/tournament.js`:

```js
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
```

- [ ] **Step 4: Run test to verify it passes**

Run: `node --test "tests/worker/*.test.js"` и `node --check functions/api/sync/tournament.js`
Expected: все тесты PASS (в Task 1 и Task 2 файлах), предупреждение `ExperimentalWarning: SQLite` допустимо.

- [ ] **Step 5: Checkpoint**

Run: `git status --short` — новые: миграция, три файла тестов/адаптера, обработчик. Не коммитить без приказа.

---

### Task 3: Публичный `GET /api/tournament`, история и регистрация роутов

**Files:**
- Create: `functions/api/tournament.js`
- Modify: `worker/index.js` (импорты рядом со строкой `import {... } from "../functions/api/sync/player.js";` и роуты рядом с блоком `path === "/api/sync/player"`)
- Modify: `tests/worker/tournament-endpoints.test.js` (добавить GET-тесты)
- Create (не коммитить): `scratch/tournament_dev_server.mjs`

**Interfaces:**
- Consumes: `groupHistory`, `TOURNAMENT_TOP_LIMIT` (Task 1); схема и `post`/`makeEnv`/`snapshot`/`UUID_*` из Task 2.
- Produces: `onRequestGet(context)`; ответ:
  `{ok:true, server_time:ISO, current:null | {week,status,ends_at,next_starts_at,top:[{uuid,nick,weight,fish}]}, history?:{weeks:[{week,ends_at,podium}], record:null|{week,nick,weight,fish}, wins:[{uuid,nick,wins}]}}`; `?history=N` (0..52, по умолчанию отсутствует).

- [ ] **Step 1: Write the failing test**

Добавить в конец `tests/worker/tournament-endpoints.test.js` (импорт `onRequestGet` в начало файла: `import { onRequestGet as getTournament } from "../../functions/api/tournament.js";`):

```js
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test tests/worker/tournament-endpoints.test.js`
Expected: FAIL (`Cannot find module '.../functions/api/tournament.js'`).

- [ ] **Step 3: Write minimal implementation**

Create `functions/api/tournament.js`:

```js
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
```

Modify `worker/index.js`: сразу после импорта `sync/player.js` добавить

```js
import { onRequestPost as syncTournamentPost } from "../functions/api/sync/tournament.js";
import { onRequestGet as tournamentGet } from "../functions/api/tournament.js";
```

и сразу после блока `if (path === "/api/sync/player") { ... }` добавить

```js
  if (path === "/api/sync/tournament" && method === "POST") return syncTournamentPost(ctx(request, env));
  if (path === "/api/tournament" && method === "GET") return tournamentGet(ctx(request, env));
```

Create `scratch/tournament_dev_server.mjs` (локальный портал: реальные обработчики + SQLite, статика из `docs/`; не коммитить):

```js
import http from "node:http";
import { readFile } from "node:fs/promises";
import { extname, join, normalize } from "node:path";
import { fileURLToPath } from "node:url";
import { onRequestPost as syncPost } from "../functions/api/sync/tournament.js";
import { onRequestGet as tournamentGet } from "../functions/api/tournament.js";
import { createTestDb, MIGRATION_0011 } from "../tests/worker/test-d1.js";

const PORT = Number(process.env.PORT || 8788);
const KEY = process.env.SERVER_SYNC_KEY || "local-test";
const DOCS = fileURLToPath(new URL("../docs/", import.meta.url));
const env = { DB: createTestDb([MIGRATION_0011]), SERVER_SYNC_KEY: KEY };
const MIME = { ".html": "text/html; charset=utf-8", ".js": "text/javascript", ".css": "text/css", ".png": "image/png", ".svg": "image/svg+xml", ".ico": "image/x-icon" };

async function toRequest(req) {
  const chunks = [];
  for await (const c of req) chunks.push(c);
  const body = chunks.length ? Buffer.concat(chunks) : undefined;
  return new Request(`http://localhost:${PORT}${req.url}`, { method: req.method, headers: req.headers, body: req.method === "GET" ? undefined : body });
}

async function send(res, response) {
  res.writeHead(response.status, Object.fromEntries(response.headers));
  res.end(Buffer.from(await response.arrayBuffer()));
}

http.createServer(async (req, res) => {
  const path = new URL(req.url, "http://x").pathname;
  try {
    if (path === "/api/sync/tournament" && req.method === "POST") return send(res, await syncPost({ request: await toRequest(req), env }));
    if (path === "/api/tournament" && req.method === "GET") return send(res, await tournamentGet({ request: await toRequest(req), env }));
    const file = join(DOCS, normalize(path === "/" ? "/index.html" : path));
    if (!file.startsWith(DOCS)) { res.writeHead(403); return res.end(); }
    const data = await readFile(file);
    res.writeHead(200, { "content-type": MIME[extname(file)] || "application/octet-stream" });
    res.end(data);
  } catch {
    res.writeHead(404);
    res.end("not found");
  }
}).listen(PORT, () => console.log(`tournament dev portal on http://localhost:${PORT} (key: ${KEY})`));
```

- [ ] **Step 4: Run test to verify it passes**

Run: `node --test "tests/worker/*.test.js"`, `node --check functions/api/tournament.js`, `node --check worker/index.js`, `node --check scratch/tournament_dev_server.mjs`
Expected: все тесты PASS; `node --check` без вывода. Затем `grep -n "tournament" worker/index.js` — видны 2 импорта и 2 роута.

- [ ] **Step 5: Checkpoint**

Run: `git status --short` — новый `functions/api/tournament.js`, изменены `worker/index.js` и тестовый файл; `scratch/` игнорируется. Не коммитить без приказа.

---

### Task 4: Чистая логика турнира и состояние (Java + JUnit)

**Files:**
- Modify: `mods/aquatech-ui/build.gradle` (репозитории и зависимости)
- Create: `mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/TournamentLogic.java`, `.../fishing/TournamentState.java`
- Test: `mods/aquatech-ui/src/test/java/net/aquatech/ui/fishing/TournamentLogicTest.java`

**Interfaces:**
- Produces (пакет `net.aquatech.ui.fishing`):
  - `TournamentLogic.TOP_LIMIT = 10`, `PRIZE_COINS = {2500,1000,500}`, `PRIZE_CASES = {"flora","steam","smeltery"}`
  - `TournamentLogic.Entry {String name, uuid; double weight; String fish}` (публичные поля, совместимы с текущим JSON: `name`, `uuid`, `weight`, `fish`)
  - `TournamentLogic.Prize {String uuid; int week; int place; long coins; String caseId}`
  - `static int weekId(LocalDate)` — `ISOгод*100 + ISOнеделя`
  - `static boolean record(List<Entry> top, String uuid, String name, double weight, String fish)` — true, если табло изменилось
  - `static int placeOf(List<Entry> top, String uuid)` — 1-based, 0 если нет
  - `static List<Prize> prizesFor(List<Entry> top, int week)`
  - `static List<Prize> claimable(List<Prize> pending, String uuid)`; `static void removeClaimed(List<Prize> pending, String uuid)`
  - `static String caseNumeral(String caseId)` — `flora`→`IV`, `steam`→`III`, `smeltery`→`II`, иначе `caseId`
  - `static long endOfWeekendMs(ZonedDateTime now)` — ближайший понедельник 00:00 в зоне `now`; `static long nextStartMs(ZonedDateTime now)` — ближайшая суббота 00:00 строго после `now`
  - `static String snapshotJson(int week, String status, long endsAtMs, long nextStartMs, List<Entry> top)` — JSON-тело для `/api/sync/tournament`
  - `TournamentState {int week=-1; long endsAt; boolean awarded; boolean finalSent=true; List<Entry> top; List<Prize> pending}` (Gson-дружелюбный, поля с инициализаторами)

- [ ] **Step 1: Set up JUnit and write the failing test**

В `mods/aquatech-ui/build.gradle`: в блок `repositories { ... }` добавить первой строкой `mavenCentral()`; в блок `dependencies { ... }` добавить

```groovy
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher:1.10.2'
```

и сразу после закрывающей скобки блока `dependencies` добавить

```groovy
tasks.withType(Test).configureEach {
    useJUnitPlatform()
}
```

Create `mods/aquatech-ui/src/test/java/net/aquatech/ui/fishing/TournamentLogicTest.java`:

```java
package net.aquatech.ui.fishing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TournamentLogicTest {

    private static final String A = "2102d9ae-dcb0-3598-8846-c4e76d4134fd";
    private static final String B = "3b5e083d-8b48-33d6-8f3c-85ab1ebb9b84";

    @Test
    void weekendStaysInOneWeekEvenWhenDayOfYearFormulaWouldSplitIt() {
        // 2024-01-06 (Sat) has dayOfYear 6, 2024-01-07 (Sun) has 7: the old dayOfYear/7 formula split them.
        assertEquals(TournamentLogic.weekId(LocalDate.of(2024, 1, 6)), TournamentLogic.weekId(LocalDate.of(2024, 1, 7)));
        assertEquals(202639, TournamentLogic.weekId(LocalDate.of(2026, 9, 26)));
        assertEquals(202639, TournamentLogic.weekId(LocalDate.of(2026, 9, 27)));
    }

    @Test
    void weekIdUsesTheIsoWeekBasedYearAtTheYearBoundary() {
        assertEquals(202601, TournamentLogic.weekId(LocalDate.of(2025, 12, 31)));
        assertEquals(202601, TournamentLogic.weekId(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void recordInsertsImprovesAndRejects() {
        List<TournamentLogic.Entry> top = new ArrayList<>();
        assertTrue(TournamentLogic.record(top, A, "a", 5.0, "Щука"));
        assertTrue(TournamentLogic.record(top, B, "b", 9.0, "Окунь"));
        assertEquals(List.of(B, A), top.stream().map(e -> e.uuid).toList());
        assertFalse(TournamentLogic.record(top, A, "a", 4.0, "Плотва"), "lower catch must not replace the best");
        assertFalse(TournamentLogic.record(top, A, "a", 5.0, "Плотва"), "equal catch changes nothing");
        assertFalse(TournamentLogic.record(top, A, "a", 0, "x"));
        assertTrue(TournamentLogic.record(top, A, "a2", 11.0, "Тунец"));
        assertEquals(1, TournamentLogic.placeOf(top, A));
        assertEquals("a2", top.get(0).name);
        assertEquals("Тунец", top.get(0).fish);
        assertEquals(0, TournamentLogic.placeOf(top, "nobody"));
    }

    @Test
    void recordCapsTheBoardAtTenAndEvictsTheLightest() {
        List<TournamentLogic.Entry> top = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            assertTrue(TournamentLogic.record(top, "u" + i, "n" + i, i, "f"));
        }
        assertEquals(10, top.size());
        assertFalse(TournamentLogic.record(top, "late", "late", 1.0, "f"), "not heavier than the 10th");
        assertFalse(TournamentLogic.record(top, "tie", "tie", 1.0, "f"), "tie with the 10th does not displace it");
        assertTrue(TournamentLogic.record(top, "big", "big", 50.0, "f"));
        assertEquals(10, top.size());
        assertEquals("big", top.get(0).uuid);
        assertEquals(0, TournamentLogic.placeOf(top, "u1"), "lightest entry is evicted");
    }

    @Test
    void prizesGoToTheTopThreeWithCoinsAndKeys() {
        List<TournamentLogic.Entry> top = new ArrayList<>();
        TournamentLogic.record(top, A, "a", 9, "f");
        TournamentLogic.record(top, B, "b", 7, "f");
        List<TournamentLogic.Prize> prizes = TournamentLogic.prizesFor(top, 202639);
        assertEquals(2, prizes.size());
        assertEquals(2500L, prizes.get(0).coins);
        assertEquals("flora", prizes.get(0).caseId);
        assertEquals(1000L, prizes.get(1).coins);
        assertEquals("steam", prizes.get(1).caseId);
        assertEquals(202639, prizes.get(1).week);
        assertEquals(2, prizes.get(1).place);
        assertTrue(TournamentLogic.prizesFor(new ArrayList<>(), 202639).isEmpty());
        assertEquals("IV", TournamentLogic.caseNumeral("flora"));
        assertEquals("III", TournamentLogic.caseNumeral("steam"));
        assertEquals("II", TournamentLogic.caseNumeral("smeltery"));
        assertEquals("abyss", TournamentLogic.caseNumeral("abyss"));
    }

    @Test
    void pendingPrizesAreClaimedOnceAndOnlyByTheirOwner() {
        List<TournamentLogic.Prize> pending = new ArrayList<>();
        pending.add(new TournamentLogic.Prize(A, 202639, 1, 2500, "flora"));
        pending.add(new TournamentLogic.Prize(B, 202639, 2, 1000, "steam"));
        List<TournamentLogic.Prize> mine = TournamentLogic.claimable(pending, A);
        assertEquals(1, mine.size());
        assertEquals("flora", mine.get(0).caseId);
        TournamentLogic.removeClaimed(pending, A);
        assertTrue(TournamentLogic.claimable(pending, A).isEmpty(), "second login must not grant again");
        assertEquals(1, pending.size(), "other players' prizes stay queued");
    }

    @Test
    void weekendBoundariesFollowTheGivenZone() {
        ZoneId msk = ZoneId.of("Europe/Moscow");
        ZonedDateTime saturday = ZonedDateTime.of(2026, 9, 26, 12, 0, 0, 0, msk);
        // Monday 2026-09-28 00:00 MSK = 2026-09-27T21:00:00Z
        assertEquals(java.time.Instant.parse("2026-09-27T21:00:00Z").toEpochMilli(), TournamentLogic.endOfWeekendMs(saturday));
        ZonedDateTime tuesday = ZonedDateTime.of(2026, 9, 29, 10, 0, 0, 0, msk);
        // Saturday 2026-10-03 00:00 MSK = 2026-10-02T21:00:00Z
        assertEquals(java.time.Instant.parse("2026-10-02T21:00:00Z").toEpochMilli(), TournamentLogic.nextStartMs(tuesday));
    }

    @Test
    void snapshotJsonMatchesTheWorkerContract() {
        List<TournamentLogic.Entry> top = new ArrayList<>();
        TournamentLogic.record(top, A, "xietoru", 12.5, "Тунец");
        String json = TournamentLogic.snapshotJson(202639, "active",
                java.time.Instant.parse("2026-09-27T21:00:00Z").toEpochMilli(),
                java.time.Instant.parse("2026-10-02T21:00:00Z").toEpochMilli(), top);
        JsonObject o = JsonParser.parseString(json).getAsJsonObject();
        assertEquals(202639, o.get("week").getAsInt());
        assertEquals("active", o.get("status").getAsString());
        assertEquals("2026-09-27T21:00:00Z", o.get("ends_at").getAsString());
        assertEquals("2026-10-02T21:00:00Z", o.get("next_starts_at").getAsString());
        JsonObject first = o.getAsJsonArray("top").get(0).getAsJsonObject();
        assertEquals(A, first.get("uuid").getAsString());
        assertEquals("xietoru", first.get("nick").getAsString());
        assertEquals(12.5, first.get("weight").getAsDouble());
        assertEquals("Тунец", first.get("fish").getAsString());
    }

    @Test
    void loadsLegacyStateFileWithoutLosingTheTopThree() {
        String legacy = "{\"week\":3912,\"top\":[{\"name\":\"xietoru\",\"uuid\":\"" + A + "\",\"weight\":12.5,\"fish\":\"Тунец\"}]}";
        TournamentState state = new Gson().fromJson(legacy, TournamentState.class);
        assertEquals(3912, state.week);
        assertEquals(1, state.top.size());
        assertEquals("xietoru", state.top.get(0).name);
        assertEquals(12.5, state.top.get(0).weight);
        assertNotNull(state.pending);
        assertTrue(state.pending.isEmpty());
        assertFalse(state.awarded);
        assertTrue(state.finalSent, "legacy files must not trigger a bogus finalize retry");
        assertEquals(0L, state.endsAt);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run (в `mods/aquatech-ui`): `./gradlew test`
Expected: FAIL при компиляции тестов (`cannot find symbol: class TournamentLogic`). Если Gradle не может скачать JUnit (ошибка резолва зависимости) — проверить сеть и что `mavenCentral()` добавлен в `repositories`.

- [ ] **Step 3: Write minimal implementation**

Create `mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/TournamentLogic.java`:

```java
package net.aquatech.ui.fishing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Чистая логика недельного турнира: без Minecraft-зависимостей, чтобы её можно было тестировать. */
public final class TournamentLogic {

    public static final int TOP_LIMIT = 10;
    public static final long[] PRIZE_COINS = {2500, 1000, 500};
    public static final String[] PRIZE_CASES = {"flora", "steam", "smeltery"};

    public static final class Entry {
        public String name;
        public String uuid;
        public double weight;
        public String fish;

        public Entry() {
        }

        public Entry(String name, String uuid, double weight, String fish) {
            this.name = name;
            this.uuid = uuid;
            this.weight = weight;
            this.fish = fish;
        }
    }

    public static final class Prize {
        public String uuid;
        public int week;
        public int place;
        public long coins;
        public String caseId;

        public Prize() {
        }

        public Prize(String uuid, int week, int place, long coins, String caseId) {
            this.uuid = uuid;
            this.week = week;
            this.place = place;
            this.coins = coins;
            this.caseId = caseId;
        }
    }

    private TournamentLogic() {
    }

    public static int weekId(LocalDate date) {
        return date.get(IsoFields.WEEK_BASED_YEAR) * 100 + date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    public static boolean record(List<Entry> top, String uuid, String name, double weight, String fish) {
        if (weight <= 0) {
            return false;
        }
        Entry mine = null;
        for (Entry e : top) {
            if (e.uuid.equals(uuid)) {
                mine = e;
                break;
            }
        }
        if (mine != null) {
            if (weight <= mine.weight) {
                return false;
            }
            mine.name = name;
            mine.weight = weight;
            mine.fish = fish;
        } else {
            if (top.size() >= TOP_LIMIT && weight <= top.get(top.size() - 1).weight) {
                return false;
            }
            top.add(new Entry(name, uuid, weight, fish));
        }
        top.sort(Comparator.comparingDouble((Entry e) -> e.weight).reversed());
        while (top.size() > TOP_LIMIT) {
            top.remove(top.size() - 1);
        }
        return true;
    }

    public static int placeOf(List<Entry> top, String uuid) {
        for (int i = 0; i < top.size(); i++) {
            if (top.get(i).uuid.equals(uuid)) {
                return i + 1;
            }
        }
        return 0;
    }

    public static List<Prize> prizesFor(List<Entry> top, int week) {
        List<Prize> prizes = new ArrayList<>();
        for (int i = 0; i < Math.min(PRIZE_COINS.length, top.size()); i++) {
            prizes.add(new Prize(top.get(i).uuid, week, i + 1, PRIZE_COINS[i], PRIZE_CASES[i]));
        }
        return prizes;
    }

    public static List<Prize> claimable(List<Prize> pending, String uuid) {
        List<Prize> mine = new ArrayList<>();
        for (Prize p : pending) {
            if (p.uuid.equals(uuid)) {
                mine.add(p);
            }
        }
        return mine;
    }

    public static void removeClaimed(List<Prize> pending, String uuid) {
        pending.removeIf(p -> p.uuid.equals(uuid));
    }

    public static String caseNumeral(String caseId) {
        return switch (caseId) {
            case "flora" -> "IV";
            case "steam" -> "III";
            case "smeltery" -> "II";
            default -> caseId;
        };
    }

    public static long endOfWeekendMs(ZonedDateTime now) {
        return now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atStartOfDay(now.getZone()).toInstant().toEpochMilli();
    }

    public static long nextStartMs(ZonedDateTime now) {
        return now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
                .atStartOfDay(now.getZone()).toInstant().toEpochMilli();
    }

    public static String snapshotJson(int week, String status, long endsAtMs, long nextStartMs, List<Entry> top) {
        JsonObject o = new JsonObject();
        o.addProperty("week", week);
        o.addProperty("status", status);
        o.addProperty("ends_at", Instant.ofEpochMilli(endsAtMs).toString());
        o.addProperty("next_starts_at", Instant.ofEpochMilli(nextStartMs).toString());
        JsonArray arr = new JsonArray();
        for (Entry e : top) {
            JsonObject row = new JsonObject();
            row.addProperty("uuid", e.uuid);
            row.addProperty("nick", e.name);
            row.addProperty("weight", e.weight);
            row.addProperty("fish", e.fish == null ? "" : e.fish);
            arr.add(row);
        }
        o.add("top", arr);
        return o.toString();
    }
}
```

Create `mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/TournamentState.java`:

```java
package net.aquatech.ui.fishing;

import java.util.ArrayList;
import java.util.List;

/** Состояние недельного турнира, хранится в config/aquatech_tournament.json (Gson). */
final class TournamentState {
    int week = -1;
    long endsAt;
    boolean awarded;
    boolean finalSent = true;
    List<TournamentLogic.Entry> top = new ArrayList<>();
    List<TournamentLogic.Prize> pending = new ArrayList<>();
}
```

- [ ] **Step 4: Run test to verify it passes**

Run (в `mods/aquatech-ui`): `./gradlew test` затем `./gradlew build`
Expected: `TournamentLogicTest` — все тесты PASS; `BUILD SUCCESSFUL` (сборка мода не сломана изменением `build.gradle`).

- [ ] **Step 5: Checkpoint**

Run: `git status --short` — изменён `mods/aquatech-ui/build.gradle`, новые: `TournamentLogic.java`, `TournamentState.java`, тест. Проверить `git diff mods/aquatech-ui/build.gradle` — только добавленные строки (нет «всего файла» из-за CRLF). Не коммитить без приказа.

---

### Task 5: Асинхронная отправка снимков (`PortalTournamentSync`)

**Files:**
- Create: `mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/PortalTournamentSync.java`
- Modify: `mods/aquatech-ui/src/main/java/net/aquatech/ui/server/auth/PortalSessionVerifier.java` (`private static String readSyncKey()` → `public static String readSyncKey()`)

**Interfaces:**
- Consumes: `PortalSessionVerifier.readSyncKey()`; `ModConfig.AUTH_API_BASE.get()`; `AquaTechUI.LOGGER`.
- Produces:
  - `PortalTournamentSync.push(int week, String status, String json, String baseUrl)` — не блокирует вызывающий поток; объединяет частые вызовы (последний побеждает, задержка 3 с)
  - `PortalTournamentSync.lastFinalizedWeek() -> int` — неделя, итоги которой портал принял (`-1`, пока ни одной)
  - `PortalTournamentSync.resolveBase() -> String` — системное свойство `aquatech.tournament.portalUrl`, иначе `ModConfig.AUTH_API_BASE.get()`, без завершающих `/`

- [ ] **Step 1: Make the key reader reachable**

В `PortalSessionVerifier.java` заменить сигнатуру

```java
    private static String readSyncKey() {
```

на

```java
    public static String readSyncKey() {
```

- [ ] **Step 2: Write the implementation**

Create `mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/PortalTournamentSync.java`:

```java
package net.aquatech.ui.fishing;

import net.aquatech.ui.AquaTechUI;
import net.aquatech.ui.common.ModConfig;
import net.aquatech.ui.server.auth.PortalSessionVerifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Отправляет снимки турнира на портал в отдельном потоке; игровой поток никогда не ждёт сеть. */
public final class PortalTournamentSync {

    private static final long DEBOUNCE_MS = 3_000L;
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final ScheduledExecutorService EXEC = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "aquatech-tournament-sync");
        t.setDaemon(true);
        return t;
    });
    private static final AtomicBoolean SCHEDULED = new AtomicBoolean(false);
    private static final AtomicReference<Pending> LATEST = new AtomicReference<>();
    private static volatile int lastFinalizedWeek = -1;

    private record Pending(int week, String status, String json, String baseUrl) {
    }

    private PortalTournamentSync() {
    }

    public static String resolveBase() {
        String base = System.getProperty("aquatech.tournament.portalUrl");
        if (base == null || base.isBlank()) {
            base = ModConfig.AUTH_API_BASE.get();
        }
        if (base == null) {
            return "";
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    public static int lastFinalizedWeek() {
        return lastFinalizedWeek;
    }

    public static void push(int week, String status, String json, String baseUrl) {
        LATEST.set(new Pending(week, status, json, baseUrl));
        if (SCHEDULED.compareAndSet(false, true)) {
            EXEC.schedule(PortalTournamentSync::flush, DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        }
    }

    private static void flush() {
        SCHEDULED.set(false);
        Pending p = LATEST.getAndSet(null);
        if (p == null) {
            return;
        }
        String key = PortalSessionVerifier.readSyncKey();
        if (key == null || key.isBlank() || p.baseUrl().isBlank()) {
            AquaTechUI.LOGGER.debug("[tournament] portal sync skipped: no sync key or base url");
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(p.baseUrl() + "/api/sync/tournament"))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .header("X-AquaTech-Server-Key", key)
                    .POST(HttpRequest.BodyPublishers.ofString(p.json()))
                    .build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                if ("finalized".equals(p.status())) {
                    lastFinalizedWeek = p.week();
                }
            } else {
                AquaTechUI.LOGGER.warn("[tournament] portal rejected snapshot: HTTP {}", response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            AquaTechUI.LOGGER.warn("[tournament] portal sync failed: {}", e.toString());
        }
    }
}
```

- [ ] **Step 3: Build**

Run (в `mods/aquatech-ui`): `./gradlew build`
Expected: `BUILD SUCCESSFUL`, тесты Task 4 проходят.

- [ ] **Step 4: Checkpoint**

Run: `git status --short` — новый `PortalTournamentSync.java`, изменён `PortalSessionVerifier.java` (одно слово). Не коммитить без приказа.

---

### Task 6: Интеграция в `OceanEventsService` и проверка на локальном портале

**Files:**
- Modify: `mods/aquatech-ui/src/main/java/net/aquatech/ui/fishing/OceanEventsService.java`

**Interfaces:**
- Consumes: всё из Task 4 и Task 5; `addCoins`, `pushHubUpdate`, `broadcast` (уже в файле).
- Produces: поведение — ISO-неделя, топ-10, призы (монеты + ключи), отложенные призы с выдачей при входе, снимки на портал; тест-шов `-Daquatech.tournament.forceActive=true` и `-Daquatech.tournament.portalUrl=http://localhost:8788`.

Правки в `OceanEventsService.java` (по именам, номера строк сдвинутся):

- [ ] **Step 1: Imports and fields**

Добавить импорты (по алфавиту рядом с остальными): `import net.minecraft.nbt.CompoundTag;`, `import java.time.LocalDate;`. Удалить поля `private static boolean lastTournamentActive = false;` и `private static boolean tournamentLoaded;`. Добавить рядом с `WEIGHT_TAG`:

```java
    private static final String CASE_KEYS_TAG = "aqualumen_case_keys";
    private static boolean startupSynced;
    private static long lastFinalRetryAt;
```

- [ ] **Step 2: Replace the tournament section**

Удалить вложенный класс `TournamentState` (заменён файлом `TournamentState.java`), а функции `tournament()`, `saveTournament()`, `weekNumber()`, `tournamentActive()`, `awardTournament(MinecraftServer)` заменить на:

```java
    private static TournamentState tournament;

    private static TournamentState tournament() {
        if (tournament == null) {
            tournament = new TournamentState();
            try {
                if (Files.exists(TOURNAMENT_FILE)) {
                    TournamentState loaded = GSON.fromJson(Files.readString(TOURNAMENT_FILE), TournamentState.class);
                    if (loaded != null) {
                        tournament = loaded;
                    }
                }
            } catch (Exception ignored) {
            }
            if (tournament.top == null) tournament.top = new ArrayList<>();
            if (tournament.pending == null) tournament.pending = new ArrayList<>();
        }
        return tournament;
    }

    private static void saveTournament() {
        try {
            Files.writeString(TOURNAMENT_FILE, GSON.toJson(tournament()));
        } catch (Exception ignored) {
        }
    }

    private static boolean tournamentActive() {
        if (Boolean.getBoolean("aquatech.tournament.forceActive")) return true;
        DayOfWeek d = ZonedDateTime.now().getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    private static void syncTournament(TournamentState t, String status) {
        ZonedDateTime now = ZonedDateTime.now();
        String json = TournamentLogic.snapshotJson(t.week, status, t.endsAt, TournamentLogic.nextStartMs(now), t.top);
        PortalTournamentSync.push(t.week, status, json, PortalTournamentSync.resolveBase());
    }

    private static void beginTournamentWeek(MinecraftServer server, TournamentState t) {
        ZonedDateTime now = ZonedDateTime.now();
        t.week = TournamentLogic.weekId(now.toLocalDate());
        t.endsAt = TournamentLogic.endOfWeekendMs(now);
        t.top = new ArrayList<>();
        t.awarded = false;
        t.finalSent = true;
        startupSynced = true;
        saveTournament();
        syncTournament(t, "active");
        broadcast(server, "§6[Турнир] §eНедельный турнир стартовал! §fСамая тяжёлая рыба субботы и воскресенья: "
                + "призы топ-3 — §62500§f/§71000§f/§8500 монет и ключи кейсов. Топ-10 — на сайте, топ-3 — в Tab!");
    }

    private static void grantPrize(ServerPlayer player, TournamentLogic.Prize prize) {
        addCoins(player, prize.coins);
        CompoundTag data = player.getPersistentData();
        CompoundTag keys = data.getCompound(CASE_KEYS_TAG);
        keys.putInt(prize.caseId, keys.getInt(prize.caseId) + 1);
        data.put(CASE_KEYS_TAG, keys);
        pushHubUpdate(player);
        player.sendSystemMessage(Component.literal("§6[Турнир] §aПриз за " + prize.place + " место (неделя " + prize.week
                + "): §6+" + prize.coins + " монет §aи §bключ Кейса " + TournamentLogic.caseNumeral(prize.caseId) + "§a!"));
    }

    private static void claimPendingPrizes(ServerPlayer player) {
        TournamentState t = tournament();
        String uuid = player.getUUID().toString();
        List<TournamentLogic.Prize> mine = TournamentLogic.claimable(t.pending, uuid);
        if (mine.isEmpty()) return;
        TournamentLogic.removeClaimed(t.pending, uuid);
        saveTournament();
        for (TournamentLogic.Prize prize : mine) {
            grantPrize(player, prize);
        }
    }

    private static void awardTournament(MinecraftServer server, TournamentState t) {
        t.awarded = true;
        List<TournamentLogic.Prize> prizes = TournamentLogic.prizesFor(t.top, t.week);
        if (prizes.isEmpty()) {
            broadcast(server, "§6[Турнир] §7Турнир недели завершён — никто не поймал рыбу. Призовой фонд сгорел!");
        } else {
            broadcast(server, "§6[Турнир] §eИтоги недели — самые тяжёлые уловы:");
            for (TournamentLogic.Prize prize : prizes) {
                TournamentLogic.Entry e = t.top.get(prize.place - 1);
                broadcast(server, "  §f" + prize.place + ". " + e.name + " — §b" + e.fish
                        + " §7(" + String.format("%.2f", e.weight) + " кг) §6+" + prize.coins
                        + " монет §7+ ключ Кейса " + TournamentLogic.caseNumeral(prize.caseId));
                ServerPlayer online = server.getPlayerList().getPlayer(java.util.UUID.fromString(e.uuid));
                if (online != null) {
                    grantPrize(online, prize);
                } else {
                    t.pending.add(prize);
                }
            }
        }
        t.finalSent = false;
        saveTournament();
        syncTournament(t, "finalized");
    }
```

- [ ] **Step 3: Replace the catch handler block**

В обработчике улова блок `// 3. Турнир ...` целиком (от `if (tournamentActive()) {` до его закрывающей скобки перед комментарием `// 4. Косяк`) заменить на:

```java
        // 3. Турнир (вес: свой тег; StarCatcher 2.3.19 вес в NBT не пишет — проставляем при выдаче)
        if (tournamentActive()) {
            TournamentState t = tournament();
            if (t.week != TournamentLogic.weekId(LocalDate.now())) {
                beginTournamentWeek(player.getServer(), t);
            }
            double best = -1;
            String fishName = "";
            for (ItemStack stack : awarded) {
                if (stack == null || stack.isEmpty()) continue;
                double w;
                if (stack.hasTag() && stack.getTag().contains(WEIGHT_TAG)) {
                    w = stack.getTag().getDouble(WEIGHT_TAG);
                } else if (stack.hasTag() && stack.getTag().contains("caught_fish_info")
                        && stack.getTag().getCompound("caught_fish_info").contains("weight")) {
                    w = stack.getTag().getCompound("caught_fish_info").getDouble("weight");
                } else {
                    var rnd = player.getRandom();
                    w = Math.round((0.3 + rnd.nextDouble() * rnd.nextDouble() * 24.0) * 100) / 100.0;
                    stack.getOrCreateTag().putDouble(WEIGHT_TAG, w);
                }
                if (w > best) {
                    best = w;
                    fishName = stack.getHoverName().getString();
                }
            }
            String uuid = player.getUUID().toString();
            if (best > 0 && TournamentLogic.record(t.top, uuid, player.getGameProfile().getName(), best, fishName)) {
                saveTournament();
                syncTournament(t, "active");
                int place = TournamentLogic.placeOf(t.top, uuid);
                if (place > 0) {
                    player.sendSystemMessage(Component.literal("§6[Турнир] §aВы на " + place + " месте недели! §7("
                            + String.format("%.2f", best) + " кг)"));
                }
            }
        }
```

- [ ] **Step 4: Replace the tick and login hooks**

В `onServerTick` блок от `boolean tourney = tournamentActive();` до конца `if (tourney != lastTournamentActive) { ... }` заменить на:

```java
        TournamentState t = tournament();
        ZonedDateTime zdt = ZonedDateTime.now();
        if (tournamentActive()) {
            if (t.week != TournamentLogic.weekId(zdt.toLocalDate())) {
                beginTournamentWeek(server, t);
            } else {
                if (t.endsAt == 0L) {
                    t.endsAt = TournamentLogic.endOfWeekendMs(zdt);
                    saveTournament();
                }
                if (!startupSynced) {
                    startupSynced = true;
                    syncTournament(t, "active");
                }
            }
        } else if (t.week != -1 && !t.awarded) {
            awardTournament(server, t);
        }
        if (!t.finalSent && t.week != -1) {
            if (PortalTournamentSync.lastFinalizedWeek() == t.week) {
                t.finalSent = true;
                saveTournament();
            } else if (now - lastFinalRetryAt > 300_000L) {
                lastFinalRetryAt = now;
                syncTournament(t, "finalized");
            }
        }
```

В `onLogin` после `ensureQuestDay(player);` добавить `claimPendingPrizes(player);`, а текст сообщения заменить на:

```java
            player.sendSystemMessage(Component.literal("§6[Турнир] §eИдёт недельный турнир! §fЛовите самую тяжёлую рыбу — топ-3 получат монеты и ключи кейсов, топ-10 виден на сайте."));
```

- [ ] **Step 5: Build and verify no stale references**

Run (в `mods/aquatech-ui`): `./gradlew build`
Затем из корня репозитория: `grep -rn "weekNumber\|TopEntry\|lastTournamentActive\|tournamentLoaded" mods/aquatech-ui/src/main/java` — ожидаемо: пусто.
Expected: `BUILD SUCCESSFUL`; grep пуст.

- [ ] **Step 6: End-to-end on a local server against the local portal**

1. В отдельном терминале: `node scratch/tournament_dev_server.mjs` (порт 8788, ключ `local-test`).
2. Убедиться, что `server/config/aquatech_sync_key.json` не отслеживается: `git check-ignore -v server/config/aquatech_sync_key.json`. Если файл отсутствует — создать `{"key":"local-test"}` (не коммитить; после проверки удалить).
3. Скопировать собранный jar в `server/mods/`, запустить локальный Mohist с JVM-аргументами `-Daquatech.tournament.forceActive=true -Daquatech.tournament.portalUrl=http://localhost:8788` (через `server/`-скрипт запуска, как в прошлых тестах).
4. Зайти клиентом, поймать рыбу (или выдать себе улов командой `/give` рыбы StarCatcher). Ожидаемо: в чате «Вы на 1 месте недели», в консоли портала нет ошибок, `http://localhost:8788/api/tournament` показывает игрока в `current.top`.
5. Остановить сервер, запустить **без** `forceActive` в будний день (сегодня Вт). Ожидаемо на первом тике: в чате/консоли итоги, при онлайн-игроке — монеты и ключ (`/aquatech` HUB → Кейсы показывает ключ Кейса IV), `aquatech_tournament.json` содержит `"awarded": true`; через 3–10 с `http://localhost:8788/api/tournament?history=4` показывает неделю в `history.weeks`.
6. Повторить п.5 с выходом игрока до итогов: в `aquatech_tournament.json` появляется запись `pending`; при следующем входе игрок получает приз один раз, `pending` пустеет; повторный вход ничего не выдаёт.
7. Перезапустить сервер ещё раз в будний день: приз не выдаётся повторно (`awarded` уже `true`).
8. Убрать тестовые артефакты: восстановить `server/config/aquatech_tournament.json` и прочие конфиги, изменённые локальным запуском (`git status`, `git checkout --` только для файлов, которые точно не относятся к работе), удалить временный `aquatech_sync_key.json`, остановить dev-портал.

- [ ] **Step 7: Checkpoint**

Run: `git status --short` — только `OceanEventsService.java` изменён в этой задаче (плюс файлы прошлых задач). Не коммитить без приказа.

---

### Task 7: Страница турнира на сайте

**Files:**
- Create: `docs/tournament.html`, `docs/assets/js/tournament.js`, `docs/assets/css/tournament.css`
- Modify: `docs/assets/js/site.js` (в `NAV_MORE` после строки `events.html`), `docs/events.html` (карточка «Недельный турнир»)
- Test: `tests/site/tournament.test.js`

**Interfaces:**
- Consumes: `GET /api/tournament?history=12` (Task 3).
- Produces (`docs/assets/js/tournament.js`, экспорт для теста через `module.exports`):
  - `escapeHtml(s) -> string`
  - `formatWeight(w:number) -> "12,34 кг"`
  - `formatCountdown(ms:number) -> "1д 03:22:10" | "03:22:10"` (отрицательное → `00:00:00`)
  - `statusView(current, offsetMs, nowMs) -> {label:string, remainingMs:number|null}` — активная неделя: «До конца турнира» и `ends_at`; завершённая: «До следующего турнира» и `next_starts_at`; нет данных: «Турнир ещё не проводился»
  - `boardRowsHtml(top) -> string`, `heroHtml(entry|undefined, isFinal) -> string`, `hallHtml(history) -> string` (везде ник и рыба экранируются)

- [ ] **Step 1: Write the failing test**

Create `tests/site/tournament.test.js`:

```js
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test tests/site/tournament.test.js`
Expected: FAIL (`Cannot find module '.../docs/assets/js/tournament.js'`).

- [ ] **Step 3: Write minimal implementation**

Create `docs/assets/js/tournament.js`:

```js
(function (root) {
  "use strict";

  var PRIZES = [
    { coins: 2500, key: "IV" },
    { coins: 1000, key: "III" },
    { coins: 500, key: "II" },
  ];

  function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }

  function pad(n) {
    return n < 10 ? "0" + n : String(n);
  }

  function formatWeight(weight) {
    return Number(weight).toFixed(2).replace(".", ",") + " кг";
  }

  function formatCountdown(ms) {
    var total = Math.max(0, Math.floor(ms / 1000));
    var days = Math.floor(total / 86400);
    var hms = pad(Math.floor((total % 86400) / 3600)) + ":" + pad(Math.floor((total % 3600) / 60)) + ":" + pad(total % 60);
    return days > 0 ? days + "д " + hms : hms;
  }

  function statusView(current, offsetMs, nowMs) {
    if (!current) return { label: "Турнир ещё не проводился", remainingMs: null };
    var now = nowMs + offsetMs;
    if (current.status === "active") {
      return { label: "До конца турнира", remainingMs: Date.parse(current.ends_at) - now };
    }
    return {
      label: "До следующего турнира",
      remainingMs: current.next_starts_at ? Date.parse(current.next_starts_at) - now : null,
    };
  }

  function prizeText(place) {
    var prize = PRIZES[place - 1];
    return prize ? "+" + prize.coins + " монет · ключ Кейса " + prize.key : "";
  }

  function boardRowsHtml(top) {
    if (!top || top.length === 0) {
      return '<p class="t-empty">Пока никто не поймал рыбу. Станьте первым!</p>';
    }
    return top
      .map(function (e, i) {
        var place = i + 1;
        var prize = prizeText(place);
        return (
          '<div class="t-row' + (place <= 3 ? " t-row--prize t-row--p" + place : "") + '">' +
          '<span class="t-place">' + place + "</span>" +
          '<span class="t-who"><b>' + escapeHtml(e.nick) + "</b><small>" + escapeHtml(e.fish || "") + "</small></span>" +
          '<span class="t-weight">' + formatWeight(e.weight) + "</span>" +
          (prize ? '<span class="t-prize">' + prize + "</span>" : "") +
          "</div>"
        );
      })
      .join("");
  }

  function heroHtml(entry, isFinal) {
    if (!entry) {
      return '<div class="t-hero-empty">Ждём первую рыбу недели</div>';
    }
    return (
      '<div class="t-hero-card">' +
      '<div class="t-hero-kicker">' + (isFinal ? "Топ-1 недели" : "Лидер прямо сейчас") + "</div>" +
      '<div class="t-hero-nick">' + escapeHtml(entry.nick) + "</div>" +
      '<div class="t-hero-weight">' + formatWeight(entry.weight) + "</div>" +
      '<div class="t-hero-fish">' + escapeHtml(entry.fish || "") + "</div>" +
      "</div>"
    );
  }

  function weekLabel(week) {
    var s = String(week);
    return s.slice(0, 4) + " · неделя " + Number(s.slice(4));
  }

  function hallHtml(history) {
    if (!history || !history.weeks || history.weeks.length === 0) {
      return '<p class="t-empty">Архив появится после первого завершённого турнира.</p>';
    }
    var parts = [];
    if (history.record) {
      parts.push(
        '<div class="t-record"><span>Рекорд за всё время</span><b>' + formatWeight(history.record.weight) +
        "</b><small>" + escapeHtml(history.record.nick) + " · " + escapeHtml(history.record.fish || "") + "</small></div>"
      );
    }
    if (history.wins && history.wins.length) {
      parts.push(
        '<div class="t-wins"><span>Больше всего побед</span>' +
        history.wins.map(function (w) { return "<div><b>" + escapeHtml(w.nick) + "</b> — " + Number(w.wins) + "</div>"; }).join("") +
        "</div>"
      );
    }
    parts.push(
      '<div class="t-weeks">' +
      history.weeks
        .map(function (w) {
          return (
            '<article class="t-week"><h4>' + weekLabel(w.week) + "</h4>" +
            w.podium.map(function (p) {
              return '<div class="t-week-row"><span>' + p.place + "</span><b>" + escapeHtml(p.nick) + "</b><em>" + formatWeight(p.weight) + "</em></div>";
            }).join("") +
            "</article>"
          );
        })
        .join("") +
      "</div>"
    );
    return parts.join("");
  }

  var api = {
    escapeHtml: escapeHtml,
    formatWeight: formatWeight,
    formatCountdown: formatCountdown,
    statusView: statusView,
    boardRowsHtml: boardRowsHtml,
    heroHtml: heroHtml,
    hallHtml: hallHtml,
  };

  if (typeof module !== "undefined" && module.exports) {
    module.exports = api;
  }
  if (typeof document === "undefined") return;

  var REFRESH_MS = 30000;
  var state = { current: null, offsetMs: 0 };

  function $(id) {
    return document.getElementById(id);
  }

  function renderCountdown() {
    var view = statusView(state.current, state.offsetMs, Date.now());
    $("t-status-label").textContent = view.label;
    $("t-countdown").textContent = view.remainingMs === null ? "—" : formatCountdown(view.remainingMs);
  }

  function render(data) {
    state.current = data.current;
    state.offsetMs = Date.parse(data.server_time) - Date.now();
    var top = data.current ? data.current.top : [];
    var isFinal = !!data.current && data.current.status === "finalized";
    $("t-hero").innerHTML = heroHtml(top[0], isFinal);
    $("t-board").innerHTML = boardRowsHtml(top);
    $("t-hall").innerHTML = hallHtml(data.history);
    renderCountdown();
  }

  function load() {
    fetch("/api/tournament?history=12", { headers: { accept: "application/json" } })
      .then(function (res) {
        if (!res.ok) throw new Error("HTTP " + res.status);
        return res.json();
      })
      .then(render)
      .catch(function () {
        $("t-board").innerHTML = '<p class="t-empty">Не удалось загрузить таблицу. Обновим через 30 секунд.</p>';
      });
  }

  document.addEventListener("DOMContentLoaded", function () {
    load();
    setInterval(load, REFRESH_MS);
    setInterval(renderCountdown, 1000);
  });
})(typeof window !== "undefined" ? window : globalThis);
```

Create `docs/assets/css/tournament.css` (использует токены `site.css`):

```css
.t-page { padding: 2.5rem 0 4rem; }
.t-top { display: grid; gap: 1.25rem; grid-template-columns: 1fr; }
@media (min-width: 860px) { .t-top { grid-template-columns: 1.1fr 1fr; } }
.t-hero { border: 1px solid var(--line); border-radius: var(--radius); background: var(--surface-2); padding: 1.75rem; display: grid; place-items: center; min-height: 240px; text-align: center; }
.t-hero-kicker { color: var(--gold); font-weight: 700; letter-spacing: .08em; text-transform: uppercase; font-size: .8rem; }
.t-hero-nick { font-size: clamp(1.8rem, 5vw, 2.8rem); font-weight: 800; margin-top: .4rem; word-break: break-word; }
.t-hero-weight { color: var(--aqua-2); font-size: clamp(1.4rem, 4vw, 2rem); font-weight: 700; margin-top: .25rem; }
.t-hero-fish { color: var(--muted); margin-top: .35rem; }
.t-hero-empty { color: var(--muted); }
.t-timer { border: 1px solid var(--line); border-radius: var(--radius); background: var(--surface); padding: 1.75rem; display: grid; align-content: center; gap: .35rem; }
.t-timer span { color: var(--muted); }
.t-timer b { font-size: clamp(1.8rem, 5vw, 2.6rem); font-variant-numeric: tabular-nums; color: var(--text); }
.t-board { margin-top: 1.5rem; border: 1px solid var(--line); border-radius: var(--radius); background: var(--surface); overflow: hidden; }
.t-row { display: grid; grid-template-columns: 2.5rem 1fr auto; gap: .75rem; align-items: center; padding: .8rem 1rem; border-bottom: 1px solid var(--separator); }
.t-row:last-child { border-bottom: 0; }
.t-row--prize { background: var(--fill); }
.t-row--p1 .t-place { color: var(--gold); }
.t-place { font-weight: 800; text-align: center; }
.t-who { display: grid; min-width: 0; }
.t-who b, .t-who small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.t-who small { color: var(--muted); }
.t-weight { font-variant-numeric: tabular-nums; font-weight: 700; }
.t-prize { grid-column: 2 / -1; color: var(--gold); font-size: .85rem; }
.t-empty { padding: 1.25rem; color: var(--muted); }
.t-hall { margin-top: 2rem; display: grid; gap: 1rem; }
.t-record, .t-wins { border: 1px solid var(--line); border-radius: var(--radius); background: var(--surface); padding: 1rem 1.25rem; display: grid; gap: .25rem; }
.t-record span, .t-wins span { color: var(--muted); font-size: .85rem; }
.t-weeks { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); }
.t-week { border: 1px solid var(--line); border-radius: var(--radius); background: var(--surface); padding: 1rem; }
.t-week h4 { margin: 0 0 .5rem; }
.t-week-row { display: grid; grid-template-columns: 1.5rem 1fr auto; gap: .5rem; padding: .2rem 0; }
.t-week-row em { font-style: normal; color: var(--muted); font-variant-numeric: tabular-nums; }
```

Create `docs/tournament.html`:

```html
<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
  <title>Недельный турнир · AquaTech</title>
  <meta name="description" content="Недельный турнир по улову AquaTech: живая таблица топ-10, призы монетами и ключами кейсов, зал славы прошлых недель." />
  <link rel="icon" href="/favicon.ico" sizes="any" />
  <meta property="og:type" content="website" />
  <meta property="og:site_name" content="AquaTech" />
  <meta property="og:title" content="Недельный турнир · AquaTech" />
  <meta property="og:description" content="Самая тяжёлая рыба недели: живой топ-10, призы и зал славы." />
  <meta property="og:url" content="https://aquateche.store/tournament.html" />
  <meta property="og:image" content="https://aquateche.store/assets/og-cover.png" />
  <script>try{document.documentElement.setAttribute("data-theme",localStorage.getItem("aquatech_theme")==="dark"?"dark":"light")}catch(e){document.documentElement.setAttribute("data-theme","light")}</script>
<link rel="stylesheet" href="assets/css/site.css?v=20260924_pink2" />
<link rel="stylesheet" href="assets/css/tournament.css?v=20260929" />
</head>
<body data-page="tournament">
  <a class="skip-link" href="#main">К содержимому</a>
  <div id="site-header"></div>
  <main id="main">
    <section class="t-page">
      <div class="container">
        <div class="eyebrow"><span>Недельный турнир</span></div>
        <h1>Самая тяжёлая рыба недели</h1>
        <p class="muted-line">Турнир идёт по субботам и воскресеньям. Топ-3 забирают монеты и ключи кейсов, таблица обновляется каждые 30 секунд.</p>

        <div class="t-top">
          <div class="t-hero" id="t-hero" aria-live="polite"><div class="t-hero-empty">Загрузка…</div></div>
          <div class="t-timer">
            <span id="t-status-label">Загрузка…</span>
            <b id="t-countdown">—</b>
          </div>
        </div>

        <h2 style="margin-top:2rem">Топ-10 недели</h2>
        <div class="t-board" id="t-board"><p class="t-empty">Загрузка таблицы…</p></div>

        <h2 style="margin-top:2.5rem">Зал славы</h2>
        <div class="t-hall" id="t-hall"></div>
      </div>
    </section>
  </main>
  <div id="site-footer"></div>
  <script src="assets/js/site.js?v=20260925_dl"></script>
  <script src="assets/js/tournament.js?v=20260929"></script>
</body>
</html>
```

Modify `docs/assets/js/site.js`: в `NAV_MORE` после строки `{ href: "events.html", label: "События", id: "events" },` добавить строку

```js
    { href: "tournament.html", label: "Турнир", id: "tournament" },
```

Modify `docs/events.html` (карточка «Недельный турнир»): заменить

`Топ-3 рыболова недели забирают <b>2500 / 1000 / 500 монет</b>. Итоги — в чате в понедельник, топ — в <a href="top.html">Топах</a>.`

на

`Топ-3 рыболова недели забирают <b>2500 / 1000 / 500 монет</b> и ключи кейсов IV / III / II. Итоги — в чате в понедельник, живая таблица и архив — на странице <a href="tournament.html">Турнира</a>.`

- [ ] **Step 4: Run test to verify it passes**

Run: `node --test "tests/**/*.test.js"` и `node --check docs/assets/js/tournament.js`, `node --check docs/assets/js/site.js`
Expected: все тесты PASS (worker + site); `node --check` без вывода.

- [ ] **Step 5: Visual check against the local portal**

1. `node scratch/tournament_dev_server.mjs`.
2. Наполнить портал тестовыми данными (PowerShell/bash), затем открыть `http://localhost:8788/tournament.html` во встроенном браузере:

```bash
curl -s -X POST http://localhost:8788/api/sync/tournament -H "X-AquaTech-Server-Key: local-test" -H "content-type: application/json" -d '{"week":202638,"status":"finalized","ends_at":"2026-09-21T21:00:00Z","next_starts_at":"2026-09-25T21:00:00Z","top":[{"uuid":"2102d9ae-dcb0-3598-8846-c4e76d4134fd","nick":"xietoru","weight":18.4,"fish":"Тунец"},{"uuid":"3b5e083d-8b48-33d6-8f3c-85ab1ebb9b84","nick":"renfild","weight":11.2,"fish":"Окунь"}]}'
curl -s -X POST http://localhost:8788/api/sync/tournament -H "X-AquaTech-Server-Key: local-test" -H "content-type: application/json" -d '{"week":202639,"status":"active","ends_at":"2026-09-27T21:00:00Z","next_starts_at":"2026-10-02T21:00:00Z","top":[{"uuid":"2102d9ae-dcb0-3598-8846-c4e76d4134fd","nick":"<b>xietoru</b>","weight":12.34,"fish":"Тунец"},{"uuid":"3b5e083d-8b48-33d6-8f3c-85ab1ebb9b84","nick":"renfild","weight":15.5,"fish":"Окунь"}]}'
```

3. Проверить: герой «Лидер прямо сейчас» = renfild 15,50 кг; таблица из 2 строк с призами; ник `<b>xietoru</b>` показан буквально (не жирным); таймер тикает; «Зал славы» показывает неделю 2026 · неделя 38 и рекорд; переключение светлой/тёмной темы, ширина 375 px (`resize_window` mobile) без горизонтальной прокрутки; консоль браузера без ошибок.

- [ ] **Step 6: Checkpoint**

Run: `git status --short` — три новых файла страницы, тест, изменены `site.js` и `events.html`. Не коммитить без приказа.

---

### Task 8: Документация, память проекта и передача на выкатку

**Files:**
- Modify: `IMPLEMENTATION_PLAN.md` (Раздел 6, журнал ревизий — новая строка после последней), `GUIDEBOOK.md` (строка про турнир, ~67), `docs/superpowers/specs/2026-09-29-weekly-tournament-portal-design.md` (добавить `next_starts_at`)

- [ ] **Step 1: Update the spec for `next_starts_at`**

В спеке: в «Часть 1» добавить в `tournament_weeks` колонку `next_starts_at TEXT` (nullable), в тело `POST` поле `next_starts_at` (ISO, опционально), в ответ `GET /api/tournament` поля `current.next_starts_at`; в «Часть 3» уточнить: вне выходных таймер считает до `next_starts_at`.

- [ ] **Step 2: Update GUIDEBOOK.md and IMPLEMENTATION_PLAN.md**

`GUIDEBOOK.md`: строку `| Турнир | сб–вс, самая тяжёлая рыба, призы 2500/1000/500, топ-3 в Tab |` заменить на

`| Турнир | сб–вс, самая тяжёлая рыба, топ-10 на сайте (tournament.html), призы топ-3: 2500/1000/500 монет + ключи кейсов IV/III/II (офлайн-победитель получает при входе), архив недель; состояние `config/aquatech_tournament.json` |`

`IMPLEMENTATION_PLAN.md` (Раздел 6): добавить строку в формате соседних, с датой `2026-09-29`: недельный турнир — ISO-неделя вместо `dayOfYear/7`, топ-10, ключи в призах, очередь призов для офлайн-победителей, снимки на `POST /api/sync/tournament`, `GET /api/tournament`, миграция `0011_tournament.sql`, страница `tournament.html`; проверка — `node --test tests/`, `./gradlew test build`, локальный e2e на dev-портале; статус «ждёт деплоя владельцем».

- [ ] **Step 3: Refresh the graph**

Run: `python -m graphify update .`
Expected: `Code graph updated.`

- [ ] **Step 4: Final verification sweep**

Run из корня: `node --test "tests/**/*.test.js"`; `node --check worker/index.js`; `cd mods/aquatech-ui && ./gradlew build`.
Expected: всё зелёное.

- [ ] **Step 5: Hand-off — commands for the owner (production, blocked for the agent)**

Передать владельцу (в будни, игра закрыта), в этом порядке:

```bash
npx wrangler d1 execute aquatech --remote --file=migrations/0011_tournament.sql
python tools/deploy_to_cloudflare.py
python tools/deploy_to_cloudflare.py worker
python tools/deploy_first_party.py --mods aquatech
```

После выкатки проверить в браузере (curl режет Cloudflare-challenge): `https://aquateche.store/api/tournament` отдаёт `{"ok":true,...,"current":null}` до первого снимка, затем `https://aquateche.store/tournament.html` показывает таблицу после первого улова или при старте выходных.

- [ ] **Step 6: Checkpoint**

Run: `git status --short` — сверить список файлов со «File Structure». Коммит и пуш — только по приказу владельца.
