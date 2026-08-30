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

function writePng(file, w, h, rgba) {
  const raw = Buffer.alloc((w * 4 + 1) * h);
  for (let y = 0; y < h; y++) {
    const row = y * (w * 4 + 1);
    raw[row] = 0;
    for (let x = 0; x < w; x++) {
      const i = row + 1 + x * 4;
      raw[i] = rgba[0];
      raw[i + 1] = rgba[1];
      raw[i + 2] = rgba[2];
      raw[i + 3] = rgba[3];
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
      pngChunk("IDAT", zlib.deflateSync(raw)),
      pngChunk("IEND", Buffer.alloc(0)),
    ]),
  );
}

fs.mkdirSync(ASSETS, { recursive: true });
writePng(path.join(ASSETS, "btn_play.png"), 16, 16, [45, 212, 224, 255]);
writePng(path.join(ASSETS, "btn_play_hover.png"), 16, 16, [126, 233, 242, 255]);
writePng(path.join(ASSETS, "btn_ghost.png"), 16, 16, [14, 58, 88, 210]);
writePng(path.join(ASSETS, "btn_ghost_hover.png"), 16, 16, [20, 80, 110, 230]);
writePng(path.join(ASSETS, "pause_glass.png"), 16, 16, [8, 22, 34, 214]);

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
 nine_slice_border_x = 4
 nine_slice_border_y = 4
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
 nine_slice_border_x = 4
 nine_slice_border_y = 4
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
  header("title_screen"),
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
  textEl({
    id: "aqua-sub",
    source: "Океанский сервер",
    x: 48,
    y: 138,
    w: 360,
    h: 22,
    color: "#B7C9D4",
  }),
  customButton({
    id: "btn-play",
    execId: uid("ex"),
    actionId: uid("act"),
    action: "joinserver",
    value: "g-pl-3.apexnodes.xyz:21561",
    x: 48,
    y: 178,
    w: 220,
    h: 40,
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
    y: 228,
    w: 220,
    h: 36,
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
    y: 272,
    w: 106,
    h: 36,
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
    y: 272,
    w: 106,
    h: 36,
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
    y: 316,
    w: 220,
    h: 36,
    label: '{"text":"Выход","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "mc_titlescreen_copyright_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 280,
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
  "40",
];

const pause = [
  header("pause_screen", { image: false, blur: true, behind: true }),
  imageEl({
    id: "pause-glass",
    file: "pause_glass.png",
    x: 0,
    y: 0,
    w: 292,
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
  textEl({
    id: "pause-hello",
    source: '{"placeholder":"playername"}',
    x: 48,
    y: 130,
    w: 220,
    h: 22,
    color: "#B7C9D4",
  }),
  playerEl({ id: "pause-skin", x: 330, y: 120, w: 90, h: 180 }),
  movedVanilla({
    instance: "pause_return_to_game_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 178,
    w: 220,
    h: 40,
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
    y: 228,
    w: 220,
    h: 36,
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
    y: 272,
    w: 106,
    h: 36,
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
    y: 272,
    w: 106,
    h: 36,
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
    y: 316,
    w: 220,
    h: 36,
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
