/** Emit AquaTech FancyMenu v3 layouts (title + pause). */
import fs from "node:fs";
import path from "node:path";
import zlib from "node:zlib";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const OUT = path.join(ROOT, "config/fancymenu/customization");
const ASSETS = path.join(ROOT, "config/fancymenu/assets");

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

function capsule(w, h, { fill, glow, pad = 10, yShift = 1 }) {
  const r = (h - pad * 2) * 0.5;
  const cx = w * 0.5;
  const cy = h * 0.5 - yShift;
  const hw = w * 0.5 - pad;
  const hh = (h - pad * 2) * 0.5;
  return (x, y) => {
    const d = sdRoundBox(x, y, cx, cy, hw, hh, r);
    const dGlow = sdRoundBox(x, y, cx, cy + 5, hw + 3, hh + 5, r + 6);
    const glowA = Math.exp(-(Math.max(dGlow, 0) ** 2) / 28) * (glow[3] / 255);
    let p = srcOver(0, 0, 0, 0, glow[0], glow[1], glow[2], glowA);
    const cover = clamp01(0.65 - d);
    if (cover > 0) {
      const spec = clamp01((cy - 4 - y) / (hh * 1.4)) * 0.38;
      const fr = Math.round(fill[0] + (255 - fill[0]) * spec);
      const fg = Math.round(fill[1] + (255 - fill[1]) * spec);
      const fb = Math.round(fill[2] + (255 - fill[2]) * spec);
      const rim = clamp01(1.2 - Math.abs(d + 0.4) * 1.4) * 0.35;
      p = srcOver(
        p[0],
        p[1],
        p[2],
        p[3] / 255,
        Math.round(fr + (255 - fr) * rim * 0.5),
        Math.round(fg + (255 - fg) * rim * 0.5),
        Math.round(fb + (255 - fb) * rim * 0.4),
        cover * (fill[3] / 255),
      );
    }
    return p;
  };
}

function glassPanel(w, h) {
  return (x, y) => {
    const edge = x > w - 2 ? 0.55 : x < 1 ? 0.12 : 0;
    const top = y < 2 ? 0.22 : 0;
    const g = y / h;
    const r = Math.round(6 + 8 * (1 - g));
    const gch = Math.round(16 + 14 * (1 - g));
    const b = Math.round(28 + 18 * (1 - g));
    const a = Math.round(132 + 36 * (1 - g));
    const hi = Math.max(edge, top);
    return [
      Math.round(r + (90 - r) * hi + edge * 40),
      Math.round(gch + (210 - gch) * hi),
      Math.round(b + (235 - b) * hi),
      a,
    ];
  };
}

fs.mkdirSync(ASSETS, { recursive: true });
writeRgba(
  path.join(ASSETS, "btn_play.png"),
  128,
  64,
  capsule(128, 64, { fill: [45, 212, 224, 255], glow: [34, 211, 238, 150], yShift: 2 }),
);
writeRgba(
  path.join(ASSETS, "btn_play_hover.png"),
  128,
  64,
  capsule(128, 64, { fill: [126, 233, 242, 255], glow: [94, 234, 248, 190], yShift: 2 }),
);
writeRgba(
  path.join(ASSETS, "btn_ghost.png"),
  128,
  64,
  capsule(128, 64, { fill: [10, 28, 44, 168], glow: [80, 200, 220, 40], pad: 8, yShift: 0 }),
);
writeRgba(
  path.join(ASSETS, "btn_ghost_hover.png"),
  128,
  64,
  capsule(128, 64, { fill: [14, 48, 72, 200], glow: [80, 220, 235, 70], pad: 8, yShift: 0 }),
);
writeRgba(path.join(ASSETS, "pause_glass.png"), 48, 256, glassPanel(48, 256));
writeRgba(path.join(ASSETS, "rule.png"), 8, 2, () => [180, 240, 250, 70]);

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
 nine_slice_border_x = 28
 nine_slice_border_y = 22
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
 nine_slice_border_x = 28
 nine_slice_border_y = 22
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

function textEl({ id, source, x, y, w, h, color }) {
  return `element {
 source = ${source}
 source_mode = direct
 shadow = false
 scale = 1.0
 base_color = ${color}
 text_border = 0
 line_spacing = 1
 enable_scrolling = false
 element_type = text_v2
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
${req(id + "-load")}
}
`;
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

function playerEl({ id, x, y, w, h }) {
  return `element {
 copy_client_player = true
 playername = {"placeholder":"playername"}
 auto_skin = true
 auto_cape = true
 slim = false
 scale = 30
 parrot = false
 is_baby = false
 crouching = false
 showname = false
 follow_mouse = true
 head_follows_mouse = true
 body_follows_mouse = true
 element_type = fancymenu_customization_player_entity
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

const title = [
  header("title_screen", { behind: true }),
  imageEl({
    id: "title-glass",
    file: "pause_glass.png",
    x: 0,
    y: 0,
    w: 300,
    h: 540,
    stretchY: true,
    nineSlice: true,
  }),
  imageEl({ id: "aqua-logo", file: "logo.png", x: 48, y: 28, w: 56, h: 56 }),
  textEl({
    id: "aqua-title",
    source: "# AquaTech",
    x: 48,
    y: 92,
    w: 360,
    h: 44,
    color: "#F3FBFF",
  }),
  imageEl({ id: "aqua-rule", file: "rule.png", x: 48, y: 132, w: 220, h: 2 }),
  textEl({
    id: "aqua-sub",
    source: "Океанский сервер",
    x: 48,
    y: 140,
    w: 360,
    h: 22,
    color: "#8FB0C2",
  }),
  customButton({
    id: "btn-play",
    execId: uid("ex"),
    actionId: uid("act"),
    action: "joinserver",
    value: "g-pl-3.apexnodes.xyz:21561",
    x: 48,
    y: 176,
    w: 220,
    h: 48,
    label: '{"text":"Играть","color":"#031018","bold":true}',
    bg: "btn_play.png",
    hover: "btn_play_hover.png",
  }),
  movedVanilla({
    instance: "mc_titlescreen_options_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 232,
    w: 220,
    h: 40,
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
    x: 48,
    y: 278,
    w: 106,
    h: 40,
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
    x: 162,
    y: 278,
    w: 106,
    h: 40,
    label: '{"text":"Discord","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "mc_titlescreen_quit_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 324,
    w: 220,
    h: 40,
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
];

const pause = [
  header("pause_screen", { image: false, blur: true, behind: true }),
  imageEl({
    id: "pause-glass",
    file: "pause_glass.png",
    x: 0,
    y: 0,
    w: 300,
    h: 540,
    stretchY: true,
    nineSlice: true,
  }),
  imageEl({ id: "pause-logo", file: "logo.png", x: 48, y: 28, w: 56, h: 56 }),
  textEl({
    id: "pause-brand",
    source: "# AquaTech",
    x: 48,
    y: 92,
    w: 220,
    h: 36,
    color: "#F3FBFF",
  }),
  imageEl({ id: "pause-rule", file: "rule.png", x: 48, y: 128, w: 220, h: 2 }),
  textEl({
    id: "pause-hello",
    source: "Океанский сервер",
    x: 48,
    y: 136,
    w: 220,
    h: 22,
    color: "#8FB0C2",
  }),
  playerEl({ id: "pause-skin", x: 330, y: 120, w: 90, h: 180 }),
  movedVanilla({
    instance: "pause_return_to_game_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 176,
    w: 220,
    h: 48,
    label: '{"text":"Продолжить","color":"#031018","bold":true}',
    bg: "btn_play.png",
    hover: "btn_play_hover.png",
  }),
  movedVanilla({
    instance: "pause_options_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 232,
    w: 220,
    h: 40,
    label: '{"text":"Настройки","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "pause_advancements_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 278,
    w: 106,
    h: 40,
    label: '{"text":"Прогресс","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "pause_stats_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 162,
    y: 278,
    w: 106,
    h: 40,
    label: '{"text":"Статистика","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "pause_disconnect_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 324,
    w: 220,
    h: 40,
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
