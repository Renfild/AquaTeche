/** Emit AquaTech FancyMenu v3 layouts (title + pause). */
import fs from "node:fs";
import path from "node:path";
import zlib from "node:zlib";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const OUT = path.join(ROOT, "config/fancymenu/customization");
const ASSETS = path.join(ROOT, "config/fancymenu/assets");

/** MetaLabs tokens (metalabs.dev :root + .btn / .card). */
const ML = {
  aqua: [128, 239, 255], // generated Apple CTA, not neon #66FFFF
  card: [12, 12, 12], // .card --background-color #0C0C0C
  ink: [3, 16, 24],
  white: [243, 251, 255],
};

const NINE_X = 8;
const NINE_Y = 8;

function crc32(buf) {
  let crc = 0xffffffff;
  for (let i = 0; i < buf.length; i++) {
    crc ^= buf[i];
    for (let j = 0; j < 8; j++) crc = (crc >>> 1) ^ (0xedb88320 & -(crc & 1));
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function pngChunk(type, data) {
  const t = Buffer.from(type);
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([t, data]);
  const c = Buffer.alloc(4);
  c.writeUInt32BE(crc32(body));
  return Buffer.concat([len, body, c]);
}

function writeRgba(file, w, h, sample) {
  const raw = Buffer.alloc((w * 4 + 1) * h);
  for (let y = 0; y < h; y++) {
    const row = y * (w * 4 + 1);
    raw[row] = 0;
    for (let x = 0; x < w; x++) {
      const p = sample(x, y);
      const i = row + 1 + x * 4;
      raw[i] = p[0];
      raw[i + 1] = p[1];
      raw[i + 2] = p[2];
      raw[i + 3] = p[3];
    }
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(w, 0);
  ihdr.writeUInt32BE(h, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  const sig = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
  fs.writeFileSync(
    file,
    Buffer.concat([
      sig,
      pngChunk("IHDR", ihdr),
      pngChunk("IDAT", zlib.deflateSync(raw, { level: 9 })),
      pngChunk("IEND", Buffer.alloc(0)),
    ]),
  );
}

function sdRoundBox(x, y, cx, cy, hw, hh, r) {
  const dx = Math.abs(x - cx) - (hw - r);
  const dy = Math.abs(y - cy) - (hh - r);
  const ox = Math.max(dx, 0);
  const oy = Math.max(dy, 0);
  return Math.min(Math.max(dx, dy), 0) + Math.hypot(ox, oy) - r;
}

function clamp01(t) {
  return t < 0 ? 0 : t > 1 ? 1 : t;
}

function srcOver(dr, dg, db, da, sr, sg, sb, sa) {
  const a = sa + da * (1 - sa);
  if (a < 0.001) return [0, 0, 0, 0];
  return [
    Math.round((sr * sa + dr * da * (1 - sa)) / a),
    Math.round((sg * sa + dg * da * (1 - sa)) / a),
    Math.round((sb * sa + db * da * (1 - sa)) / a),
    Math.round(a * 255),
  ];
}

function glassRect(w, h, { fill, rimA = 0.3, rimW = 1.2, radiusFrac = 0.14, pad = 2 }) {
  const cx = w * 0.5;
  const cy = h * 0.5;
  const hw = w * 0.5 - pad;
  const hh = h * 0.5 - pad;
  const r = Math.min(hw, hh, (h - pad * 2) * radiusFrac);
  const inset = NINE_X;
  return (x, y) => {
    const d = sdRoundBox(x, y, cx, cy, hw, hh, r);
    const cover = clamp01(0.5 - d);
    if (cover <= 0) return [0, 0, 0, 0];
    const flat = x >= inset && x < w - inset && y >= inset && y < h - inset;
    if (flat) return [fill[0], fill[1], fill[2], fill[3]];
    const spec = clamp01((cy - pad - 1 - y) / Math.max(hh * 2.2, 1)) * 0.12;
    const fr = Math.round(fill[0] + (255 - fill[0]) * spec);
    const fg = Math.round(fill[1] + (255 - fill[1]) * spec);
    const fb = Math.round(fill[2] + (255 - fill[2]) * spec);
    let p = srcOver(0, 0, 0, 0, fr, fg, fb, cover * (fill[3] / 255));
    const ring = clamp01(1 - Math.abs(d) / rimW) * cover;
    if (ring > 0) p = srcOver(p[0], p[1], p[2], p[3] / 255, 255, 255, 255, ring * rimA);
    return p;
  };
}

function glassPanel(w, h) {
  return (x, y) => {
    const edge = x > w - 2 ? 0.7 : x < 1 ? 0.08 : 0;
    const top = y < 2 ? 0.18 : 0;
    const g = y / h;
    const [cr, cg, cb] = ML.card;
    const a = Math.round(150 + 28 * (1 - g));
    const hi = Math.max(edge, top);
    return [
      Math.round(cr + (255 - cr) * hi * 0.35),
      Math.round(cg + (255 - cg) * hi * 0.35),
      Math.round(cb + (255 - cb) * hi * 0.4),
      a,
    ];
  };
}

function logoGlow(w, h) {
  return (x, y) => {
    const dx = (x - w / 2) / (w * 0.42);
    const dy = (y - h / 2) / (h * 0.42);
    const a = Math.exp(-(dx * dx + dy * dy) * 1.8) * 0.45;
    return srcOver(0, 0, 0, 0, ML.aqua[0], ML.aqua[1], ML.aqua[2], a);
  };
}

fs.mkdirSync(ASSETS, { recursive: true });
const BW = 256;
const BH = 48;
writeRgba(
  path.join(ASSETS, "btn_play.png"),
  BW,
  BH,
  glassRect(BW, BH, {
    fill: [128, 239, 255, 255],
    rimA: 0.32,
    rimW: 1.1,
  }),
);
writeRgba(
  path.join(ASSETS, "btn_play_hover.png"),
  BW,
  BH,
  glassRect(BW, BH, {
    fill: [176, 246, 255, 255],
    rimA: 0.38,
    rimW: 1.1,
  }),
);
writeRgba(
  path.join(ASSETS, "btn_ghost.png"),
  BW,
  BH,
  glassRect(BW, BH, {
    fill: [255, 255, 255, 88],
    rimA: 0.72,
    rimW: 1.15,
  }),
);
writeRgba(
  path.join(ASSETS, "btn_ghost_hover.png"),
  BW,
  BH,
  glassRect(BW, BH, {
    fill: [255, 255, 255, 128],
    rimA: 0.82,
    rimW: 1.15,
  }),
);
writeRgba(path.join(ASSETS, "pause_glass.png"), 48, 256, glassPanel(48, 256));
writeRgba(path.join(ASSETS, "rule.png"), 8, 2, () => [255, 255, 255, 76]);
writeRgba(path.join(ASSETS, "logo_glow.png"), 96, 96, logoGlow(96, 96));

{
  const ps1 = path.join(ROOT, "scripts/tasks/render_fancymenu_wordmark.ps1");
  const png = path.join(ASSETS, "brand_wordmark.png");
  const wm = spawnSync(
    "powershell",
    ["-NoProfile", "-ExecutionPolicy", "Bypass", "-File", ps1, png],
    { encoding: "utf8" },
  );
  if (wm.status !== 0) {
    throw new Error(`wordmark failed: ${wm.stderr || wm.stdout || wm.status}`);
  }
}

const metaTail = ` appearance_delay = no_delay
 appearance_delay_seconds = 1.0
 fade_in_v2 = no_fading
 fade_in_speed = 1.0
 fade_out = no_fading
 fade_out_speed = 1.0
 base_opacity = 1.0
 auto_sizing = false
 auto_sizing_base_screen_width = 1920
 auto_sizing_base_screen_height = 1080
 sticky_anchor = false`;

function req(id) {
  return ` element_loading_requirement_container_identifier = ${id}
 [loading_requirement_container_meta:${id}] = [groups:][instances:]
 enable_parallax = false
 parallax_intensity_v2 = 0.5
 invert_parallax = false
 animated_offset_x = 0
 animated_offset_y = 0
 load_once_per_session = false
 in_editor_color = #FFC800FF
 layer_hidden_in_editor = false`;
}

function widgetMeta(id) {
  return ` navigatable = true
 widget_active_state_requirement_container_identifier = ${id}
 [loading_requirement_container_meta:${id}] = [groups:][instances:]
 is_template = false
 template_apply_width = false
 template_apply_height = false
 template_apply_posx = false
 template_apply_posy = false
 template_apply_opacity = false
 template_apply_visibility = false
 template_apply_label = false
 template_share_with = buttons
 nine_slice_slider_handle = false
 nine_slice_slider_handle_border_x = 5
 nine_slice_slider_handle_border_y = 5`;
}

function hiddenVanilla(instance, execId, wid, loadId) {
  return `vanilla_button {
 button_element_executable_block_identifier = ${execId}
 [executable_block:${execId}][type:generic] = [executables:]
 restartbackgroundanimations = true
 nine_slice_custom_background = false
 nine_slice_border_x = 5
 nine_slice_border_y = 5
${widgetMeta(wid)}
 element_type = vanilla_button
 instance_identifier = ${instance}
${metaTail}
 anchor_point = vanilla
 x = 0
 y = 0
 width = 20
 height = 20
 stretch_x = false
 stretch_y = false
 stay_on_screen = true
${req(loadId)}
 is_hidden = true
 automated_button_clicks = 0
}
`;
}

function movedVanilla({ instance, execId, wid, loadId, x, y, w, h, label, bg, hover }) {
  const bgLines = bg
    ? ` backgroundnormal = [source:local]/config/fancymenu/assets/${bg}
 backgroundhovered = [source:local]/config/fancymenu/assets/${hover}
`
    : "";
  const labelLine = label ? ` label = ${label}\n` : "";
  return `vanilla_button {
 button_element_executable_block_identifier = ${execId}
 [executable_block:${execId}][type:generic] = [executables:]
${bgLines} restartbackgroundanimations = true
 nine_slice_custom_background = true
 nine_slice_border_x = ${NINE_X}
 nine_slice_border_y = ${NINE_Y}
${labelLine}${widgetMeta(wid)}
 element_type = vanilla_button
 instance_identifier = ${instance}
${metaTail}
 anchor_point = top-left
 x = ${x}
 y = ${y}
 width = ${w}
 height = ${h}
 stretch_x = false
 stretch_y = false
 stay_on_screen = true
${req(loadId)}
 is_hidden = false
 automated_button_clicks = 0
}
`;
}

function customButton({ id, execId, actionId, action, value, x, y, w, h, label, bg, hover }) {
  return `element {
 button_element_executable_block_identifier = ${execId}
 [executable_action_instance:${actionId}][action_type:${action}] = ${value}
 [executable_block:${execId}][type:generic] = [executables:${actionId};]
 backgroundnormal = [source:local]/config/fancymenu/assets/${bg}
 backgroundhovered = [source:local]/config/fancymenu/assets/${hover}
 restartbackgroundanimations = true
 nine_slice_custom_background = true
 nine_slice_border_x = ${NINE_X}
 nine_slice_border_y = ${NINE_Y}
 label = ${label}
${widgetMeta(wid(id))}
 element_type = custom_button
 instance_identifier = ${id}
${metaTail}
 anchor_point = top-left
 x = ${x}
 y = ${y}
 width = ${w}
 height = ${h}
 stretch_x = false
 stretch_y = false
 stay_on_screen = true
${req(id.replace("btn", "load"))}
}
`;
}

function wid(s) {
  return `wid-${s}`;
}

function imageEl({ id, file, x, y, w, h, stretchY = false, nineSlice = false }) {
  return `element {
 source = [source:local]/config/fancymenu/assets/${file}
 repeat_texture = false
 nine_slice_texture = ${nineSlice}
 nine_slice_texture_border_x = 4
 nine_slice_texture_border_y = 4
 image_tint = #FFFFFF
 restart_animated_on_menu_load = false
 element_type = image
 instance_identifier = ${id}
${metaTail}
 anchor_point = top-left
 x = ${x}
 y = ${y}
 width = ${w}
 height = ${h}
 stretch_x = false
 stretch_y = ${stretchY}
 stay_on_screen = true
${req(id + "-load")}
}
`;
}

function header(identifier, { image = true, blur = false, behind = false } = {}) {
  const bg = image
    ? `menu_background {
 image_path = [source:local]/config/fancymenu/assets/menu_background.png
 slide = false
 repeat_texture = false
 parallax = true
 parallax_intensity = 0.04
 invert_parallax = false
 restart_animated_on_menu_load = false
 background_type = image
}

customization {
 action = backgroundoptions
 keepaspectratio = true
}
`
    : "";
  return `type = fancymenu_layout

layout-meta {
 identifier = ${identifier}
 render_custom_elements_behind_vanilla = ${behind}
 last_edited_time = 1756531200000
 is_enabled = true
 randommode = false
 randomgroup = 1
 randomonlyfirsttime = false
 layout_index = 0
 [loading_requirement_container_meta:meta-${identifier}] = [groups:][instances:]
}

customization {
 action = setscale
 scale = 2.0
}

customization {
 action = autoscale
 basewidth = 1920
 baseheight = 1080
}

${bg}
scroll_list_customization {
 preserve_scroll_list_header_footer_aspect_ratio = true
 render_scroll_list_header_shadow = true
 render_scroll_list_footer_shadow = true
 show_scroll_list_header_footer_preview_in_editor = false
 repeat_scroll_list_header_texture = false
 repeat_scroll_list_footer_texture = false
 show_screen_background_overlay_on_custom_background = false
 apply_vanilla_background_blur = ${blur ? "true" : "false"}
}

layout_action_executable_blocks {
}

`;
}

const HIDDEN_TITLE = [
  "mc_titlescreen_singleplayer_button",
  "mc_titlescreen_multiplayer_button",
  "mc_titlescreen_realms_button",
  "mc_titlescreen_language_button",
  "mc_titlescreen_accessibility_button",
  "forge_titlescreen_mods_button",
  "minecraft_logo_widget",
  "minecraft_splash_widget",
  "minecraft_branding_widget",
  "title_screen_logo",
  "title_screen_splash",
  "title_screen_branding",
  "title_screen_realms_notification",
  "title_screen_forge_copyright",
  "title_screen_forge_top",
  "minecraft_realms_notification_icons_widget",
];

let n = 0;
const uid = (p) => `${p}-${String(++n).padStart(3, "0")}`;

const RAIL = 280;
const PAD = 24;
const INNER = RAIL - PAD * 2;
const HALF = Math.floor((INNER - 8) / 2);
const CTA_H = 48;
const BTN_H = 44;
const GAP = 8;

function stackY(start, heights) {
  const ys = [];
  let y = start;
  for (const h of heights) {
    ys.push(y);
    y += h + GAP;
  }
  return ys;
}

function sidebar(prefix) {
  return [
    imageEl({
      id: `${prefix}-glass`,
      file: "pause_glass.png",
      x: 0,
      y: 0,
      w: RAIL,
      h: 540,
      stretchY: true,
      nineSlice: true,
    }),
    imageEl({ id: `${prefix}-glow`, file: "logo_glow.png", x: PAD - 8, y: 12, w: 64, h: 64 }),
    imageEl({ id: `${prefix}-logo`, file: "logo.png", x: PAD, y: 20, w: 40, h: 40 }),
    imageEl({
      id: `${prefix}-word`,
      file: "brand_wordmark.png",
      x: PAD,
      y: 68,
      w: INNER,
      h: 44,
    }),
  ];
}

const titleYs = stackY(128, [CTA_H, BTN_H, BTN_H, BTN_H]);
const pauseYs = stackY(128, [CTA_H, BTN_H, BTN_H, BTN_H, BTN_H]);

const title = [
  header("title_screen", { behind: true }),
  ...sidebar("title"),
  customButton({
    id: "btn-play",
    execId: uid("ex"),
    actionId: uid("act"),
    action: "joinserver",
    value: "g-pl-3.apexnodes.xyz:21561",
    x: PAD,
    y: titleYs[0],
    w: INNER,
    h: CTA_H,
    label: '{"text":"Играть","color":"#031018","bold":true}',
    bg: "btn_play.png",
    hover: "btn_play_hover.png",
  }),
  movedVanilla({
    instance: "mc_titlescreen_options_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: PAD,
    y: titleYs[1],
    w: INNER,
    h: BTN_H,
    label: '{"text":"Настройки","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  customButton({
    id: "btn-site",
    execId: uid("ex"),
    actionId: uid("act"),
    action: "openlink",
    value: "https://aquateche.store",
    x: PAD,
    y: titleYs[2],
    w: HALF,
    h: BTN_H,
    label: '{"text":"Сайт","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  customButton({
    id: "btn-discord",
    execId: uid("ex"),
    actionId: uid("act"),
    action: "openlink",
    value: "https://discord.gg/3Khzr5z4fQ",
    x: PAD + HALF + 8,
    y: titleYs[2],
    w: HALF,
    h: BTN_H,
    label: '{"text":"Discord","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "mc_titlescreen_quit_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: PAD,
    y: titleYs[3],
    w: INNER,
    h: BTN_H,
    label: '{"text":"Выход","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "mc_titlescreen_copyright_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 320,
    y: 516,
    w: 400,
    h: 16,
    label: "",
  }),
];

for (const id of HIDDEN_TITLE) {
  title.push(hiddenVanilla(id, uid("ex"), uid("w"), uid("l")));
}

const pauseHidden = [
  "pause_title_widget",
  "pause_send_feedback_button",
  "pause_feedback_button",
  "pause_report_bugs_button",
  "pause_share_to_lan_button",
  "pause_server_links_button",
  "mc_pausescreen_feedback_button",
  "mc_pausescreen_report_bugs_button",
  "mc_pausescreen_lan_button",
  "forge_titlescreen_mods_button",
  "fml.menu.mods",
  "fml.menu.mods.title",
  "modmenu:modsbutton",
  "modmenu.mods",
  "40",
  "376324",
  "398348",
  "374300",
  "604330",
  "606300",
  "374276",
  "606252",
  "376348",
  "504348",
  "580332",
  "604346",
  "vanillabtn:40",
  "vanillabtn:376324",
  "vanillabtn:398348",
  "vanillabtn:504348",
  "vanillabtn:604330",
  "button_compatibility_id:376324",
  "button_compatibility_id:40",
];

const pause = [
  header("pause_screen", { image: false, blur: true, behind: true }),
  ...sidebar("pause"),
  movedVanilla({
    instance: "pause_return_to_game_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: PAD,
    y: pauseYs[0],
    w: INNER,
    h: CTA_H,
    label: '{"text":"Продолжить","color":"#031018","bold":true}',
    bg: "btn_play.png",
    hover: "btn_play_hover.png",
  }),
  movedVanilla({
    instance: "pause_options_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: PAD,
    y: pauseYs[1],
    w: INNER,
    h: BTN_H,
    label: '{"text":"Настройки","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "pause_advancements_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: PAD,
    y: pauseYs[2],
    w: INNER,
    h: BTN_H,
    label: '{"text":"Прогресс","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "pause_stats_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: PAD,
    y: pauseYs[3],
    w: INNER,
    h: BTN_H,
    label: '{"text":"Статистика","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "pause_disconnect_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: PAD,
    y: pauseYs[4],
    w: INNER,
    h: BTN_H,
    label: '{"text":"Отключиться","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
];

for (const id of pauseHidden) {
  pause.push(hiddenVanilla(id, uid("ex"), uid("w"), uid("l")));
}

fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, "title_screen_layout.txt"), title.join("\n"));
fs.writeFileSync(path.join(OUT, "pause_screen_layout.txt"), pause.join("\n"));
console.log("wrote", OUT);
