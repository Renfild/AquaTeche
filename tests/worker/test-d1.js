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
