import test from "node:test";
import assert from "node:assert/strict";
import { onRequestPost as forgot } from "../../functions/api/auth/forgot-password.js";
import { onRequestPost as reset } from "../../functions/api/auth/reset-password.js";
import { createTestDb } from "./test-d1.js";

const migrations = ["0001_init", "0009_auth_recovery_and_vault"].map((m) => new URL(`../../migrations/${m}.sql`, import.meta.url));

/** Запоминает сообщения, которые бот «отправил»: код достаём из текста, как это сделает игрок. */
function fakeTelegram() {
  const sent = [];
  const original = globalThis.fetch;
  globalThis.fetch = async (url, init) => {
    if (String(url).startsWith("https://api.telegram.org/")) {
      sent.push(JSON.parse(init.body));
      return new Response("{}", { status: 200 });
    }
    return original(url, init);
  };
  return { sent, restore: () => (globalThis.fetch = original) };
}

async function makeEnv({ linked = true } = {}) {
  const db = createTestDb(migrations);
  await db.prepare("INSERT INTO users (nick, password_hash, password_salt) VALUES ('Steve', 'oldhash', 'oldsalt')").run();
  await db.prepare("INSERT INTO sessions (id, user_id, expires_at) VALUES ('s1', 1, '2099-01-01T00:00:00.000Z')").run();
  await db.prepare("CREATE TABLE tg_links (user_id INTEGER PRIMARY KEY, chat_id TEXT NOT NULL, tg_name TEXT NOT NULL DEFAULT '', linked_at TEXT)").run();
  if (linked) await db.prepare("INSERT INTO tg_links (user_id, chat_id, tg_name) VALUES (1, '777', 'steve_tg')").run();
  return { DB: db, TG_BOT_TOKEN: "test-token" };
}

const post = (fn, env, path, body, ip = "1.1.1.1") =>
  fn({
    env,
    request: new Request(`https://aquateche.store${path}`, {
      method: "POST",
      headers: { "content-type": "application/json", "cf-connecting-ip": ip },
      body: JSON.stringify(body),
    }),
  });

const askCode = (env, nick = "Steve", ip) => post(forgot, env, "/api/auth/forgot-password", { nick }, ip);
const sendReset = (env, body, ip) => post(reset, env, "/api/auth/reset-password", body, ip);
const codeFrom = (tg) => tg.sent.at(-1).text.match(/Код: (\d{6})/)[1];
const passwordHash = async (env) => (await env.DB.prepare("SELECT password_hash FROM users WHERE id = 1").first()).password_hash;

test("код уходит в привязанный Telegram, не попадает в ответ, и по нему меняется пароль", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv();
    const res = await askCode(env);
    assert.equal(res.status, 200);
    const body = await res.json();
    assert.equal(body.channel, "telegram");
    assert.match(body.target, /^@st\*\*\*$/);
    assert.equal(JSON.stringify(body).includes(codeFrom(tg)), false, "код не должен быть в ответе");
    assert.equal(tg.sent.at(-1).chat_id, "777");

    const ok = await sendReset(env, { nick: "steve", code: codeFrom(tg), password: "new-password-1" });
    assert.equal(ok.status, 200);
    assert.notEqual(await passwordHash(env), "oldhash");
    assert.equal((await env.DB.prepare("SELECT COUNT(*) AS n FROM sessions WHERE user_id = 1").first()).n, 0, "старые сессии сброшены");

    const again = await sendReset(env, { nick: "Steve", code: codeFrom(tg), password: "another-password-2" });
    assert.equal(again.status, 404, "код одноразовый");
  } finally {
    tg.restore();
  }
});

test("без привязанного Telegram и почты код никуда не уходит", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv({ linked: false });
    const res = await askCode(env);
    assert.equal(res.status, 403);
    assert.equal(tg.sent.length, 0);
    assert.equal((await env.DB.prepare("SELECT COUNT(*) AS n FROM password_reset_tokens").first()).n, 0);
  } finally {
    tg.restore();
  }
});

test("код привязан к нику: чужой ник с этим кодом пароль не меняет", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv();
    await env.DB.prepare("INSERT INTO users (nick, password_hash, password_salt) VALUES ('Alex', 'alexhash', 's')").run();
    await askCode(env);
    const res = await sendReset(env, { nick: "Alex", code: codeFrom(tg), password: "new-password-1" });
    assert.equal(res.status, 404);
    assert.equal((await env.DB.prepare("SELECT password_hash FROM users WHERE nick = 'Alex'").first()).password_hash, "alexhash");
  } finally {
    tg.restore();
  }
});

test("перебор: после пяти неверных кодов запрос сгорает, даже верный код уже не подходит", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv();
    await askCode(env);
    const real = codeFrom(tg);
    const wrong = real === "000000" ? "000001" : "000000";
    for (let i = 0; i < 5; i++) {
      const res = await sendReset(env, { nick: "Steve", code: wrong, password: "new-password-1" }, `9.9.9.${i}`);
      assert.equal(res.status, 403);
    }
    const burned = await sendReset(env, { nick: "Steve", code: real, password: "new-password-1" }, "9.9.9.9");
    assert.equal(burned.status, 429);
    assert.equal(await passwordHash(env), "oldhash");
    const after = await sendReset(env, { nick: "Steve", code: real, password: "new-password-1" }, "9.9.9.10");
    assert.equal(after.status, 404, "сгоревший запрос больше не находится");
  } finally {
    tg.restore();
  }
});

test("новый запрос гасит старый код и даёт свежий счётчик попыток", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv();
    await askCode(env, "Steve", "2.2.2.2");
    const first = codeFrom(tg);
    await askCode(env, "Steve", "2.2.2.3");
    const second = codeFrom(tg);
    if (first !== second) {
      const old = await sendReset(env, { nick: "Steve", code: first, password: "new-password-1" }, "2.2.2.4");
      assert.equal(old.status, 403, "старый код недействителен");
    }
    const fresh = await sendReset(env, { nick: "Steve", code: second, password: "new-password-1" }, "2.2.2.5");
    assert.equal(fresh.status, 200);
  } finally {
    tg.restore();
  }
});

test("не больше трёх запросов кода на ник: чужой Telegram не заспамить", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv();
    for (let i = 0; i < 3; i++) assert.equal((await askCode(env, "Steve", `3.3.3.${i}`)).status, 200);
    assert.equal((await askCode(env, "Steve", "3.3.3.9")).status, 429);
    assert.equal(tg.sent.length, 3);
  } finally {
    tg.restore();
  }
});

test("слабый пароль отклоняется и код остаётся рабочим", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv();
    await askCode(env);
    const weak = await sendReset(env, { nick: "Steve", code: codeFrom(tg), password: "short" });
    assert.equal(weak.status, 400);
    assert.equal(await passwordHash(env), "oldhash");
    const ok = await sendReset(env, { nick: "Steve", code: codeFrom(tg), password: "long-enough-password" });
    assert.equal(ok.status, 200);
  } finally {
    tg.restore();
  }
});

// ---------- почта ----------
import { onRequestGet as emailGet, onRequestPost as emailPost, onRequestDelete as emailDelete } from "../../functions/api/auth/email.js";

function fakeMail({ fail = false } = {}) {
  const sent = [];
  return { sent, send: async (msg) => { if (fail) throw Object.assign(new Error("boom"), { code: "E_FAIL" }); sent.push(msg); return { messageId: "m" + sent.length }; } };
}

const cabinet = (fn, env, body, ip = "5.5.5.5") =>
  fn({
    env,
    request: new Request("https://aquateche.store/api/auth/email", {
      method: body === undefined ? "GET" : "POST",
      headers: { "content-type": "application/json", cookie: "at_session=s1", "cf-connecting-ip": ip },
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    }),
  });
const mailCode = (mail) => mail.sent.at(-1).text.match(/(\d{6})/)[1];
const emailOf = async (env) => (await env.DB.prepare("SELECT email FROM users WHERE id = 1").first()).email;

test("почта привязывается только после подтверждения кодом из письма", async () => {
  const env = await makeEnv({ linked: false });
  const mail = fakeMail();
  env.EMAIL = mail;
  const start = await cabinet(emailPost, env, { action: "start", email: "Steve@Example.com" });
  assert.equal(start.status, 200);
  assert.equal((await start.json()).target, "st***@example.com");
  assert.equal(mail.sent[0].to, "steve@example.com");
  assert.equal(await emailOf(env), null, "до подтверждения почта в аккаунт не пишется");

  const wrong = await cabinet(emailPost, env, { action: "confirm", code: mailCode(mail) === "000000" ? "000001" : "000000" });
  assert.equal(wrong.status, 403);
  assert.equal(await emailOf(env), null);

  const ok = await cabinet(emailPost, env, { action: "confirm", code: mailCode(mail) });
  assert.equal(ok.status, 200);
  assert.equal(await emailOf(env), "steve@example.com");
  const status = await (await cabinet(emailGet, env)).json();
  assert.equal(status.linked, true);
  assert.equal(status.email_masked, "st***@example.com");

  const del = await emailDelete({ env, request: new Request("https://aquateche.store/api/auth/email", { method: "DELETE", headers: { cookie: "at_session=s1" } }) });
  assert.equal(del.status, 200);
  assert.equal(await emailOf(env), null);
});

test("без настроенной почты привязка честно отвечает 503", async () => {
  const env = await makeEnv({ linked: false });
  const res = await cabinet(emailPost, env, { action: "start", email: "a@b.co" });
  assert.equal(res.status, 503);
  assert.equal((await (await cabinet(emailGet, env)).json()).configured, false);
});

test("после пяти неверных кодов привязки код сгорает", async () => {
  const env = await makeEnv({ linked: false });
  const mail = fakeMail();
  env.EMAIL = mail;
  await cabinet(emailPost, env, { action: "start", email: "a@b.co" });
  const real = mailCode(mail);
  const wrong = real === "000000" ? "000001" : "000000";
  for (let i = 0; i < 5; i++) assert.equal((await cabinet(emailPost, env, { action: "confirm", code: wrong })).status, 403);
  assert.equal((await cabinet(emailPost, env, { action: "confirm", code: real })).status, 429);
  assert.equal(await emailOf(env), null);
});

test("сброс пароля по почте, когда Telegram не привязан", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv({ linked: false });
    const mail = fakeMail();
    env.EMAIL = mail;
    await env.DB.prepare("UPDATE users SET email = 'steve@example.com' WHERE id = 1").run();
    const res = await askCode(env);
    assert.equal(res.status, 200);
    const body = await res.json();
    assert.deepEqual(body.channels, [{ type: "email", target: "st***@example.com" }]);
    assert.equal(tg.sent.length, 0);
    const ok = await sendReset(env, { nick: "Steve", code: mailCode(mail), password: "new-password-1" });
    assert.equal(ok.status, 200);
    assert.notEqual(await passwordHash(env), "oldhash");
  } finally {
    tg.restore();
  }
});

test("если привязаны Telegram и почта, код приходит в оба канала и он один", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv();
    const mail = fakeMail();
    env.EMAIL = mail;
    await env.DB.prepare("UPDATE users SET email = 'steve@example.com' WHERE id = 1").run();
    const body = await (await askCode(env)).json();
    assert.deepEqual(body.channels.map((c) => c.type).sort(), ["email", "telegram"]);
    assert.equal(mailCode(mail), codeFrom(tg));
  } finally {
    tg.restore();
  }
});

test("если письмо не ушло и Telegram нет, запрос сгорает и отвечает 503", async () => {
  const tg = fakeTelegram();
  try {
    const env = await makeEnv({ linked: false });
    env.EMAIL = fakeMail({ fail: true });
    await env.DB.prepare("UPDATE users SET email = 'steve@example.com' WHERE id = 1").run();
    const res = await askCode(env);
    assert.equal(res.status, 503);
    assert.equal((await env.DB.prepare("SELECT COUNT(*) AS n FROM password_reset_tokens WHERE used = 0").first()).n, 0);
  } finally {
    tg.restore();
  }
});

// ---------- регистрация с почтой ----------
import { onRequestPost as register } from "../../functions/api/register.js";

const signUp = (env, body, ip = "7.7.7.7") =>
  register({
    env,
    request: new Request("https://aquateche.store/api/register", {
      method: "POST",
      headers: { "content-type": "application/json", "cf-connecting-ip": ip },
      body: JSON.stringify(body),
    }),
  });

test("регистрация с почтой шлёт код, а почта в аккаунт попадает только после подтверждения", async () => {
  const env = await makeEnv({ linked: false });
  const mail = fakeMail();
  env.EMAIL = mail;
  const res = await signUp(env, { nick: "Newbie", password: "long-password-1", email: "New@Example.com" });
  assert.equal(res.status, 201);
  const body = await res.json();
  assert.equal(body.email_pending, "ne***@example.com");
  assert.equal(mail.sent[0].to, "new@example.com");
  const user = await env.DB.prepare("SELECT id, email FROM users WHERE nick = 'Newbie'").first();
  assert.equal(user.email, null);

  const sid = res.headers.get("set-cookie").match(/at_session=([^;]+)/)[1];
  const confirm = await emailPost({
    env,
    request: new Request("https://aquateche.store/api/auth/email", {
      method: "POST",
      headers: { "content-type": "application/json", cookie: `at_session=${sid}` },
      body: JSON.stringify({ action: "confirm", code: mailCode(mail) }),
    }),
  });
  assert.equal(confirm.status, 200);
  assert.equal((await env.DB.prepare("SELECT email FROM users WHERE nick = 'Newbie'").first()).email, "new@example.com");
});

test("регистрация без почты (лаунчер) работает как раньше", async () => {
  const env = await makeEnv({ linked: false });
  env.EMAIL = fakeMail();
  const res = await signUp(env, { nick: "Launcher1", password: "long-password-1" });
  assert.equal(res.status, 201);
  assert.equal((await res.json()).email_pending, undefined);
  assert.equal(env.EMAIL.sent?.length ?? 0, 0);
});

test("кривой адрес почты отклоняет регистрацию, а сбой письма её не отменяет", async () => {
  const env = await makeEnv({ linked: false });
  env.EMAIL = fakeMail();
  const bad = await signUp(env, { nick: "BadMail", password: "long-password-1", email: "not-an-email" }, "8.8.8.1");
  assert.equal(bad.status, 400);
  assert.equal(await env.DB.prepare("SELECT id FROM users WHERE nick = 'BadMail'").first(), null);

  env.EMAIL = fakeMail({ fail: true });
  const ok = await signUp(env, { nick: "MailDown", password: "long-password-1", email: "a@b.co" }, "8.8.8.2");
  assert.equal(ok.status, 201);
  const body = await ok.json();
  assert.equal(body.email_pending, undefined);
  assert.ok(body.email_error);
});
