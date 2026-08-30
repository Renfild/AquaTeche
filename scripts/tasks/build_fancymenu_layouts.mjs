/** Emit AquaTech FancyMenu v3 layouts (title + pause). */
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const OUT = path.join(ROOT, "config/fancymenu/customization");

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
 nine_slice_border_x = 24
 nine_slice_border_y = 24
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
 nine_slice_border_x = 24
 nine_slice_border_y = 24
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

function imageEl({ id, file, x, y, w, h }) {
  return `element {
 source = [source:local]/config/fancymenu/assets/${file}
 repeat_texture = false
 nine_slice_texture = false
 nine_slice_texture_border_x = 5
 nine_slice_texture_border_y = 5
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
 stretch_y = false
 stay_on_screen = true
${req(id + "-load")}
}
`;
}

function header(identifier, { image = true, blur = false } = {}) {
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
 render_custom_elements_behind_vanilla = false
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
    instance: "mc_titlescreen_accessibility_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 500,
    w: 24,
    h: 24,
    label: "",
  }),
  movedVanilla({
    instance: "forge_titlescreen_mods_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 880,
    y: 500,
    w: 72,
    h: 24,
    label: '{"text":"Моды","color":"#B7C9D4"}',
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
  "mc_pausescreen_feedback_button",
  "mc_pausescreen_report_bugs_button",
  "mc_pausescreen_lan_button",
  "pause_share_to_lan_button",
  "pause_feedback_button",
  "pause_report_bugs_button",
];

const pause = [
  header("pause_screen", { image: false, blur: true }),
  textEl({
    id: "pause-title",
    source: "Пауза",
    x: 48,
    y: 80,
    w: 280,
    h: 28,
    color: "#F3FBFF",
  }),
  movedVanilla({
    instance: "mc_pausescreen_return_to_game_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 130,
    w: 220,
    h: 40,
    label: '{"text":"Продолжить","color":"#031018","bold":true}',
    bg: "btn_play.png",
    hover: "btn_play_hover.png",
  }),
  movedVanilla({
    instance: "mc_pausescreen_options_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 180,
    w: 220,
    h: 36,
    label: '{"text":"Настройки","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "mc_pausescreen_advancements_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 224,
    w: 106,
    h: 36,
    label: '{"text":"Прогресс","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "mc_pausescreen_stats_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 162,
    y: 224,
    w: 106,
    h: 36,
    label: '{"text":"Статистика","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "mc_pausescreen_disconnect_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 276,
    w: 220,
    h: 36,
    label: '{"text":"Отключиться","color":"#F3FBFF"}',
    bg: "btn_ghost.png",
    hover: "btn_ghost_hover.png",
  }),
  movedVanilla({
    instance: "mc_pausescreen_return_to_menu_button",
    execId: uid("ex"),
    wid: uid("w"),
    loadId: uid("l"),
    x: 48,
    y: 276,
    w: 220,
    h: 36,
    label: '{"text":"В меню","color":"#F3FBFF"}',
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
