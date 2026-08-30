/**
 * Delta-publish FancyMenu pack 2.9.265 (Python is broken on this machine).
 * Merges new files into docs/pack/manifest.json, uploads only those assets
 * to a draft GitHub release, then publishes the tag.
 */
import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const TAG = "pack-2.9.265";
const VERSION = "2.9.265";
const REPO = "Renfild/AquaTeche";
const DOCS_PACK = path.join(ROOT, "docs/pack");
const RELEASE = `https://github.com/${REPO}/releases/download/${TAG}`;

const ADD = [
  ["client/mods/fancymenu_forge_3.9.12_MC_1.20.1.jar", "mods/fancymenu_forge_3.9.12_MC_1.20.1.jar"],
  ["client/mods/konkrete_forge_1.8.0_MC_1.20-1.20.1.jar", "mods/konkrete_forge_1.8.0_MC_1.20-1.20.1.jar"],
  ["client/mods/melody_forge_1.0.3_MC_1.20.1-1.20.4.jar", "mods/melody_forge_1.0.3_MC_1.20.1-1.20.4.jar"],
  ["config/fancymenu/options.txt", "config/fancymenu/options.txt"],
  ["config/fancymenu/customizablemenus.txt", "config/fancymenu/customizablemenus.txt"],
  ["config/fancymenu/customization/title_screen_layout.txt", "config/fancymenu/customization/title_screen_layout.txt"],
  ["config/fancymenu/customization/pause_screen_layout.txt", "config/fancymenu/customization/pause_screen_layout.txt"],
  ["config/fancymenu/assets/menu_background.png", "config/fancymenu/assets/menu_background.png"],
  ["config/fancymenu/assets/btn_play.png", "config/fancymenu/assets/btn_play.png"],
  ["config/fancymenu/assets/btn_play_hover.png", "config/fancymenu/assets/btn_play_hover.png"],
  ["config/fancymenu/assets/btn_ghost.png", "config/fancymenu/assets/btn_ghost.png"],
  ["config/fancymenu/assets/btn_ghost_hover.png", "config/fancymenu/assets/btn_ghost_hover.png"],
  ["config/fancymenu/assets/logo.png", "config/fancymenu/assets/logo.png"],
];

function md5File(abs) {
  const h = crypto.createHash("md5");
  h.update(fs.readFileSync(abs));
  return h.digest("hex");
}

function assetName(rel) {
  const posix = rel.replace(/\\/g, "/");
  if (posix.startsWith("mods/")) return path.posix.basename(posix);
  return posix.replace(/\//g, "__");
}

function token() {
  const t = fs.readFileSync(path.join(ROOT, ".gh_token"), "utf8").trim();
  if (!t) throw new Error("missing .gh_token");
  return t;
}

async function gh(method, url, { body, contentType, raw } = {}) {
  const headers = {
    Authorization: `token ${token()}`,
    Accept: "application/vnd.github+json",
    "User-Agent": "AquaTechPackUploader",
  };
  if (contentType) headers["Content-Type"] = contentType;
  const res = await fetch(url, { method, headers, body });
  const text = await res.text();
  if (!res.ok) throw new Error(`${method} ${url} HTTP ${res.status}: ${text.slice(0, 400)}`);
  if (raw) return text;
  return text ? JSON.parse(text) : {};
}

const entries = [];
for (const [localRel, packPath] of ADD) {
  const abs = path.join(ROOT, localRel);
  if (!fs.existsSync(abs)) throw new Error(`missing ${localRel}`);
  const st = fs.statSync(abs);
  const asset = assetName(packPath);
  entries.push({
    path: packPath,
    md5: md5File(abs),
    size: st.size,
    url: `${RELEASE}/${asset}`,
    asset,
    localAbs: abs,
  });
}

const manPath = path.join(DOCS_PACK, "manifest.json");
const prev = JSON.parse(fs.readFileSync(manPath, "utf8"));
const files = [...(prev.files || [])];
const byPath = new Map(files.map((f) => [f.path, f]));
for (const e of entries) {
  const { localAbs, ...pub } = e;
  byPath.set(e.path, pub);
}
const nextFiles = [...byPath.values()].sort((a, b) => a.path.localeCompare(b.path));
const next = {
  ...prev,
  version: VERSION,
  files: nextFiles,
};

const prevMap = Object.fromEntries((prev.files || []).map((f) => [f.path, f]));
const newMap = Object.fromEntries(nextFiles.map((f) => [f.path, f]));
const added = Object.keys(newMap).filter((p) => !prevMap[p]);
const removed = Object.keys(prevMap).filter((p) => !newMap[p]);
const changed = Object.keys(newMap).filter(
  (p) => prevMap[p] && (prevMap[p].md5 !== newMap[p].md5 || prevMap[p].size !== newMap[p].size),
);
const delta = {
  from_version: prev.version,
  to_version: VERSION,
  added: added.map((p) => newMap[p]),
  changed: changed.map((p) => newMap[p]),
  removed: removed.map((p) => ({ path: p })),
  stats: {
    added: added.length,
    changed: changed.length,
    removed: removed.length,
    unchanged: nextFiles.length - added.length - changed.length,
    download_bytes: [...added, ...changed].reduce((n, p) => n + newMap[p].size, 0),
  },
};

fs.writeFileSync(manPath, JSON.stringify(next, null, 2) + "\n");
fs.writeFileSync(path.join(DOCS_PACK, "delta.json"), JSON.stringify(delta, null, 2) + "\n");
console.log(
  `manifest ${VERSION}: +${delta.stats.added} ~${delta.stats.changed} -${delta.stats.removed} (~${(delta.stats.download_bytes / 1e6).toFixed(1)} MB)`,
);

const existing = await gh(
  "GET",
  `https://api.github.com/repos/${REPO}/releases/tags/${TAG}`,
).catch((e) => {
  if (String(e).includes("HTTP 404")) return null;
  throw e;
});
if (existing && !existing.draft) {
  throw new Error(`${TAG} already published (immutable). bump tag.`);
}

let releaseId = existing?.id;
if (!releaseId) {
  const rel = await gh("POST", `https://api.github.com/repos/${REPO}/releases`, {
    body: JSON.stringify({
      tag_name: TAG,
      target_commitish: "main",
      name: `AquaTech Pack ${TAG}`,
      body: "FancyMenu title + pause screens. Other pack files still served from earlier tags.\n",
      draft: true,
      prerelease: false,
    }),
    contentType: "application/json",
  });
  releaseId = rel.id;
  console.log(`draft id=${releaseId}`);
} else {
  console.log(`reuse draft id=${releaseId}`);
}

for (const e of entries) {
  const q = new URLSearchParams({ name: e.asset });
  const buf = fs.readFileSync(e.localAbs);
  const res = await fetch(`https://uploads.github.com/repos/${REPO}/releases/${releaseId}/assets?${q}`, {
    method: "POST",
    headers: {
      Authorization: `token ${token()}`,
      Accept: "application/vnd.github+json",
      "Content-Type": "application/octet-stream",
      "User-Agent": "AquaTechPackUploader",
    },
    body: buf,
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`upload ${e.asset} HTTP ${res.status}: ${text.slice(0, 300)}`);
  console.log(`put ${e.asset} (${e.size})`);
}

const published = await gh("PATCH", `https://api.github.com/repos/${REPO}/releases/${releaseId}`, {
  body: JSON.stringify({ draft: false, make_latest: "false" }),
  contentType: "application/json",
});
console.log("published", published.html_url);
