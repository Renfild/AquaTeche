import { spawnSync } from "node:child_process";
import { existsSync } from "node:fs";
import { resolve } from "node:path";

const file = process.argv[2];
if (!file) {
  console.error("usage: node scripts/verify-render.mjs <mp4> [--duration N] [--tolerance N]");
  process.exit(1);
}

const durationIdx = process.argv.indexOf("--duration");
const expectDuration = durationIdx >= 0 ? Number(process.argv[durationIdx + 1]) : null;
const tolIdx = process.argv.indexOf("--tolerance");
const tolerance = tolIdx >= 0 ? Number(process.argv[tolIdx + 1]) : 0.5;

const abs = resolve(file);
if (!existsSync(abs)) {
  console.error("missing file", abs);
  process.exit(1);
}

const remotionFfprobe = resolve(
  "..",
  "node_modules",
  "@remotion",
  "compositor-win32-x64-msvc",
  "ffprobe.exe",
);
const bin = existsSync(remotionFfprobe) ? remotionFfprobe : "ffprobe";

const probe = spawnSync(
  bin,
  ["-v", "error", "-show_entries", "stream=codec_name,codec_type,width,height,pix_fmt", "-show_entries", "format=duration", "-of", "json", abs],
  { encoding: "utf8" },
);

if (probe.status !== 0) {
  console.error(probe.stderr || "ffprobe failed");
  process.exit(1);
}

const data = JSON.parse(probe.stdout);
const video = (data.streams || []).find((s) => s.codec_type === "video");
const audio = (data.streams || []).find((s) => s.codec_type === "audio");
const duration = Number(data.format?.duration);
const errors = [];

if (!video) errors.push("no video stream");
else {
  if (video.width !== 1080 || video.height !== 1920) {
    errors.push(`size ${video.width}x${video.height}, expected 1080x1920`);
  }
  if (video.codec_name !== "h264") errors.push(`video codec ${video.codec_name}, expected h264`);
  if (video.pix_fmt && video.pix_fmt !== "yuv420p" && video.pix_fmt !== "yuvj420p") {
    errors.push(`pix_fmt ${video.pix_fmt}, expected yuv420p`);
  }
}
if (!audio) errors.push("no audio stream");
if (expectDuration != null && Number.isFinite(duration)) {
  if (Math.abs(duration - expectDuration) > tolerance) {
    errors.push(`duration ${duration}s, expected ${expectDuration}±${tolerance}`);
  }
}

if (errors.length) {
  console.error("FAIL");
  for (const e of errors) console.error("-", e);
  process.exit(1);
}

console.log("PASS");
console.log(`${video.width}x${video.height} ${video.codec_name}/${video.pix_fmt || "?"} ${duration.toFixed(2)}s audio=${audio.codec_name}`);
