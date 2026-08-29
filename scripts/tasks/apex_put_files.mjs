/**
 * Upload files to Apex (Pterodactyl) when Python is unavailable.
 * Secrets: repo-root .apex_deploy.json (never commit).
 *
 *   node scripts/tasks/apex_put_files.mjs --put local:remote ... [--delete remote] [--restart]
 */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const secrets = JSON.parse(fs.readFileSync(path.join(ROOT, ".apex_deploy.json"), "utf8"));
const panel = String(secrets.apex_panel || "https://panel.apexnodes.xyz").replace(/\/$/, "");
const sid = secrets.apex_server_id;
const key = secrets.apex_api_key;
if (!sid || !key) {
  console.error("missing apex_server_id / apex_api_key");
  process.exit(1);
}

const headers = {
  Authorization: `Bearer ${key}`,
  Accept: "Application/vnd.pterodactyl.v1+json",
};

function parseArgs(argv) {
  const puts = [];
  const deletes = [];
  let restart = false;
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === "--restart") restart = true;
    else if (a === "--put") {
      const spec = argv[++i];
      const idx = spec.indexOf(":");
      if (idx < 1) throw new Error(`bad --put ${spec}`);
      puts.push({ local: spec.slice(0, idx), remote: spec.slice(idx + 1) });
    } else if (a === "--delete") deletes.push(argv[++i]);
    else throw new Error(`unknown arg ${a}`);
  }
  return { puts, deletes, restart };
}

async function api(method, p, body, extra = {}) {
  const url = `${panel}${p}`;
  const init = { method, headers: { ...headers, ...extra.headers } };
  if (body !== undefined) init.body = body;
  const res = await fetch(url, init);
  const text = await res.text();
  if (!res.ok) throw new Error(`${method} ${p} HTTP ${res.status}: ${text.slice(0, 400)}`);
  return text ? JSON.parse(text) : {};
}

async function writeFile(remote, localAbs) {
  const posix = "/" + remote.replace(/\\/g, "/").replace(/^\/+/, "");
  const slash = posix.lastIndexOf("/");
  const dir = slash <= 0 ? "/" : posix.slice(0, slash);
  const name = posix.slice(slash + 1);
  const buf = fs.readFileSync(localAbs);
  // Panel /files/write is nginx-capped (~1MB). Jars go through Wings signed upload.
  if (buf.length < 512 * 1024) {
    const q = `file=${encodeURIComponent(posix)}`;
    await api("POST", `/api/client/servers/${sid}/files/write?${q}`, buf, {
      headers: { "Content-Type": "application/octet-stream" },
    });
    console.log(`put ${posix} (${buf.length} bytes)`);
    return;
  }
  const signed = await api(
    "GET",
    `/api/client/servers/${sid}/files/upload?directory=${encodeURIComponent(dir)}`,
  );
  const rawUrl = signed?.attributes?.url;
  if (!rawUrl) throw new Error("no signed upload url");
  const u = new URL(rawUrl);
  u.searchParams.set("directory", dir);
  const fd = new FormData();
  fd.append("files", new Blob([buf]), name);
  const res = await fetch(u.toString(), { method: "POST", body: fd });
  const text = await res.text();
  if (!res.ok) throw new Error(`upload ${posix} HTTP ${res.status}: ${text.slice(0, 400)}`);
  console.log(`put ${posix} (${buf.length} bytes)`);
}

async function deleteFiles(files) {
  if (!files.length) return;
  const names = files.map((f) => f.replace(/\\/g, "/").replace(/^\/+/, ""));
  await api(
    "POST",
    `/api/client/servers/${sid}/files/delete`,
    JSON.stringify({ root: "/", files: names }),
    { headers: { "Content-Type": "application/json" } },
  );
  for (const n of names) console.log(`deleted /${n}`);
}

async function command(cmd) {
  try {
    await api(
      "POST",
      `/api/client/servers/${sid}/command`,
      JSON.stringify({ command: cmd }),
      { headers: { "Content-Type": "application/json" } },
    );
    console.log(`console: ${cmd}`);
  } catch (e) {
    console.warn(`console skip: ${e.message}`);
  }
}

async function power(signal) {
  await api(
    "POST",
    `/api/client/servers/${sid}/power`,
    JSON.stringify({ signal }),
    { headers: { "Content-Type": "application/json" } },
  );
  console.log(`power ${signal}`);
}

async function waitRunning(timeoutMs = 180000) {
  const deadline = Date.now() + timeoutMs;
  let seenDown = false;
  while (Date.now() < deadline) {
    const data = await api("GET", `/api/client/servers/${sid}/resources`);
    const state = data?.attributes?.current_state || "unknown";
    console.log(`  panel ${state}`);
    if (state === "starting" || state === "offline" || state === "stopping") seenDown = true;
    if (seenDown && state === "running") return;
    await new Promise((r) => setTimeout(r, 5000));
  }
  console.warn("WARN not confirmed running");
}

const { puts, deletes, restart } = parseArgs(process.argv.slice(2));
for (const { local, remote } of puts) {
  const abs = path.isAbsolute(local) ? local : path.join(ROOT, local);
  if (!fs.existsSync(abs)) throw new Error(`missing ${abs}`);
  await writeFile(remote, abs);
}
await deleteFiles(deletes);
if (restart) {
  await command("save-all");
  await new Promise((r) => setTimeout(r, 3000));
  await power("restart");
  await waitRunning();
}
