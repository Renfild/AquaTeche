"""AquaTech FancyMenu Assets & Layout Generator.
Generates pixel-perfect 9-slice textures, high-res oceanic background, crystal emblem,
and centered FancyMenu v3 layouts (title screen + pause screen).
"""
from __future__ import annotations
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter
import numpy as np

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "config" / "fancymenu" / "assets"
CUSTOM = ROOT / "config" / "fancymenu" / "customization"
BRAIN = Path(r"C:\Users\xieto\.gemini\antigravity-ide\brain\e8997355-62d0-4496-aa07-dc7f04f75423")


def clamp(val, low=0.0, high=1.0):
    return max(low, min(high, val))


def render_9slice_button(
    w: int,
    h: int,
    border: int,
    radius: float,
    fill_top: tuple[int, int, int, int],
    fill_bottom: tuple[int, int, int, int],
    border_color: tuple[int, int, int, int],
    border_width: float = 1.5,
    outer_glow_color: tuple[int, int, int, int] | None = None,
    outer_glow_radius: float = 2.5,
    specular_top: tuple[int, int, int, int] | None = None,
) -> Image.Image:
    """Renders a mathematical 9-slice button with supersampling for pristine antialiasing."""
    scale = 4
    sw, sh = w * scale, h * scale
    sb = border * scale
    sr = radius * scale
    sbw = border_width * scale
    sgr = outer_glow_radius * scale

    pad = int(math.ceil(sgr + sbw))
    x0, y0 = pad, pad
    x1, y1 = sw - pad, sh - pad
    bh = y1 - y0

    y_coords, x_coords = np.mgrid[0:sh, 0:sw]

    dx = np.maximum(np.maximum(x0 + sr - x_coords, 0), np.maximum(x_coords - (x1 - sr), 0))
    dy = np.maximum(np.maximum(y0 + sr - y_coords, 0), np.maximum(y_coords - (y1 - sr), 0))

    is_corner = (dx > 0) & (dy > 0)
    corner_dist = np.sqrt(dx * dx + dy * dy) - sr
    edge_dist = np.maximum(dx, dy) - sr
    dist = np.where(is_corner, corner_dist, edge_dist)

    ft = np.array(fill_top, dtype=np.float32) / 255.0
    fb = np.array(fill_bottom, dtype=np.float32) / 255.0
    bc = np.array(border_color, dtype=np.float32) / 255.0

    y_frac = np.clip((y_coords - y0) / max(bh, 1), 0.0, 1.0)[:, :, np.newaxis]
    fill_col = ft * (1.0 - y_frac) + fb * y_frac

    body_alpha = np.clip(0.5 - dist, 0.0, 1.0)[:, :, np.newaxis]

    if outer_glow_color is not None:
        gc = np.array(outer_glow_color, dtype=np.float32) / 255.0
        glow_dist = np.clip(dist / max(sgr, 0.1), 0.0, 1.0)
        glow_factor = (1.0 - glow_dist) ** 1.8
        glow_alpha = (glow_factor * gc[3])[:, :, np.newaxis]
        glow_layer = np.zeros((sh, sw, 4), dtype=np.float32)
        glow_layer[:, :, :3] = gc[:3]
        glow_layer[:, :, 3:] = glow_alpha
    else:
        glow_layer = np.zeros((sh, sw, 4), dtype=np.float32)

    body_layer = np.zeros((sh, sw, 4), dtype=np.float32)
    body_layer[:, :, :3] = fill_col[:, :, :3]
    body_layer[:, :, 3:] = fill_col[:, :, 3:] * body_alpha

    spec_layer = np.zeros((sh, sw, 4), dtype=np.float32)
    if specular_top is not None:
        sc = np.array(specular_top, dtype=np.float32) / 255.0
        spec_y = np.clip(1.0 - (y_coords - y0) / max(sbw * 3.5, 1.0), 0.0, 1.0)
        spec_mask = np.clip(0.5 - np.abs(dist + sbw * 0.7), 0.0, 1.0) * spec_y
        spec_layer[:, :, :3] = sc[:3]
        spec_layer[:, :, 3:] = (sc[3] * spec_mask)[:, :, np.newaxis]

    stroke_layer = np.zeros((sh, sw, 4), dtype=np.float32)
    stroke_cover = np.clip(1.0 - np.abs(dist) / max(sbw * 0.5, 0.1), 0.0, 1.0)
    stroke_layer[:, :, :3] = bc[:3]
    stroke_layer[:, :, 3:] = (bc[3] * stroke_cover)[:, :, np.newaxis]

    def blend(dst, src):
        sa = src[:, :, 3:]
        da = dst[:, :, 3:]
        out_a = sa + da * (1.0 - sa)
        safe_a = np.where(out_a > 1e-5, out_a, 1.0)
        out_rgb = (src[:, :, :3] * sa + dst[:, :, :3] * da * (1.0 - sa)) / safe_a
        res = np.zeros_like(dst)
        res[:, :, :3] = out_rgb
        res[:, :, 3:] = out_a
        return res

    result = blend(glow_layer, body_layer)
    result = blend(result, spec_layer)
    result = blend(result, stroke_layer)

    out = Image.fromarray(np.clip(result * 255.0, 0, 255).astype(np.uint8), "RGBA")
    return out.resize((w, h), Image.Resampling.LANCZOS)


def generate_textures():
    ASSETS.mkdir(parents=True, exist_ok=True)

    print("1. Generating Primary Play Button (btn_play.png / btn_play_hover.png)...")
    btn_play = render_9slice_button(
        w=256,
        h=48,
        border=16,
        radius=14.0,
        fill_top=(0, 210, 240, 245),
        fill_bottom=(0, 150, 185, 250),
        border_color=(0, 240, 255, 255),
        border_width=1.5,
        outer_glow_color=(0, 229, 255, 120),
        outer_glow_radius=3.0,
        specular_top=(235, 255, 255, 180),
    )
    btn_play.save(ASSETS / "btn_play.png", "PNG")

    btn_play_hover = render_9slice_button(
        w=256,
        h=48,
        border=16,
        radius=14.0,
        fill_top=(70, 245, 255, 255),
        fill_bottom=(0, 195, 230, 255),
        border_color=(255, 255, 255, 255),
        border_width=2.0,
        outer_glow_color=(0, 240, 255, 200),
        outer_glow_radius=4.5,
        specular_top=(255, 255, 255, 240),
    )
    btn_play_hover.save(ASSETS / "btn_play_hover.png", "PNG")

    print("2. Generating Secondary Ghost Buttons (btn_ghost.png / btn_ghost_hover.png)...")
    btn_ghost = render_9slice_button(
        w=256,
        h=44,
        border=14,
        radius=11.0,
        fill_top=(8, 24, 34, 215),
        fill_bottom=(4, 14, 22, 225),
        border_color=(0, 140, 170, 160),
        border_width=1.2,
        outer_glow_color=(0, 180, 216, 50),
        outer_glow_radius=2.0,
        specular_top=(140, 220, 240, 90),
    )
    btn_ghost.save(ASSETS / "btn_ghost.png", "PNG")

    btn_ghost_hover = render_9slice_button(
        w=256,
        h=44,
        border=14,
        radius=11.0,
        fill_top=(12, 38, 54, 240),
        fill_bottom=(6, 22, 34, 245),
        border_color=(0, 235, 255, 255),
        border_width=1.8,
        outer_glow_color=(0, 229, 255, 160),
        outer_glow_radius=4.0,
        specular_top=(200, 250, 255, 180),
    )
    btn_ghost_hover.save(ASSETS / "btn_ghost_hover.png", "PNG")

    print("3. Generating Frosted Glass Center Card (pause_glass.png)...")
    menu_card = render_9slice_button(
        w=256,
        h=256,
        border=24,
        radius=20.0,
        fill_top=(5, 16, 24, 210),
        fill_bottom=(3, 10, 16, 220),
        border_color=(0, 160, 190, 140),
        border_width=1.5,
        outer_glow_color=(0, 210, 240, 70),
        outer_glow_radius=4.0,
        specular_top=(100, 215, 240, 90),
    )
    menu_card.save(ASSETS / "pause_glass.png", "PNG")

    print("4. Generating Crystal Emblem Icon (logo_crystal.png)...")
    crop_path = BRAIN / "scratch" / "crystal_icon.png"
    if crop_path.exists():
        raw = Image.open(crop_path).convert("RGBA")
        cw, ch = raw.size
        mask = Image.new("L", (cw, ch), 0)
        mdraw = ImageDraw.Draw(mask)
        cx, cy = cw / 2.0, ch / 2.0
        r = 54.0
        mdraw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=255)
        mask = mask.filter(ImageFilter.GaussianBlur(1.8))

        emblem = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
        glow = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
        gdraw = ImageDraw.Draw(glow)
        gdraw.ellipse((cx - r - 4, cy - r - 4, cx + r + 4, cy + r + 4), fill=(0, 229, 255, 80))
        glow = glow.filter(ImageFilter.GaussianBlur(5.0))

        emblem.paste(glow, (0, 0), glow)
        emblem.paste(raw, (0, 0), mask)
        emblem = emblem.resize((128, 128), Image.Resampling.LANCZOS)
        emblem.save(ASSETS / "logo_crystal.png", "PNG")
        print("   -> logo_crystal.png saved")

    print("5. Processing Oceanic Wallpaper (menu_background.png)...")
    bg_candidates = sorted(BRAIN.glob("oceanic_submarine_bg_*.jpg"), key=lambda p: p.stat().st_mtime, reverse=True)
    if bg_candidates:
        src_bg = bg_candidates[0]
        bg_im = Image.open(src_bg).convert("RGB")
        bg_im = bg_im.resize((1920, 1080), Image.Resampling.LANCZOS)
        bg_im.save(ASSETS / "menu_background.png", "PNG", optimize=True)
        print("   -> menu_background.png (1920x1080) saved")


# Layout builder helper templates
META_TAIL = """ appearance_delay = no_delay
 appearance_delay_seconds = 1.0
 fade_in_v2 = no_fading
 fade_in_speed = 1.0
 fade_out = no_fading
 fade_out_speed = 1.0
 base_opacity = 1.0
 auto_sizing = false
 auto_sizing_base_screen_width = 1920
 auto_sizing_base_screen_height = 1080
 sticky_anchor = false"""


def req(id_str: str) -> str:
    return f""" element_loading_requirement_container_identifier = {id_str}
 [loading_requirement_container_meta:{id_str}] = [groups:][instances:]
 enable_parallax = false
 parallax_intensity_v2 = 0.5
 invert_parallax = false
 animated_offset_x = 0
 animated_offset_y = 0
 load_once_per_session = false
 in_editor_color = #FFC800FF
 layer_hidden_in_editor = false"""


def widget_meta(id_str: str) -> str:
    return f""" navigatable = true
 widget_active_state_requirement_container_identifier = {id_str}
 [loading_requirement_container_meta:{id_str}] = [groups:][instances:]
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
 nine_slice_slider_handle_border_y = 5"""


def hidden_vanilla(instance: str, exec_id: str, wid_id: str, load_id: str) -> str:
    return f"""vanilla_button {{
 button_element_executable_block_identifier = {exec_id}
 [executable_block:{exec_id}][type:generic] = [executables:]
 restartbackgroundanimations = true
 nine_slice_custom_background = false
 nine_slice_border_x = 5
 nine_slice_border_y = 5
{widget_meta(wid_id)}
 element_type = vanilla_button
 instance_identifier = {instance}
{META_TAIL}
 anchor_point = vanilla
 x = 0
 y = 0
 width = 20
 height = 20
 stretch_x = false
 stretch_y = false
 stay_on_screen = true
{req(load_id)}
 is_hidden = true
 automated_button_clicks = 0
}}
"""


def moved_vanilla(
    instance: str,
    exec_id: str,
    wid_id: str,
    load_id: str,
    x: int,
    y: int,
    w: int,
    h: int,
    label: str,
    bg: str,
    hover: str,
    nine_border: int = 14,
    anchor: str = "mid-centered",
) -> str:
    bg_lines = (
        f""" backgroundnormal = [source:local]/config/fancymenu/assets/{bg}
 backgroundhovered = [source:local]/config/fancymenu/assets/{hover}
"""
        if bg
        else ""
    )
    label_line = f" label = {label}\n" if label else ""
    return f"""vanilla_button {{
 button_element_executable_block_identifier = {exec_id}
 [executable_block:{exec_id}][type:generic] = [executables:]
{bg_lines} restartbackgroundanimations = true
 nine_slice_custom_background = true
 nine_slice_border_x = {nine_border}
 nine_slice_border_y = {nine_border}
{label_line} label_shadow = false
{widget_meta(wid_id)}
 element_type = vanilla_button
 instance_identifier = {instance}
{META_TAIL}
 anchor_point = {anchor}
 x = {x}
 y = {y}
 width = {w}
 height = {h}
 stretch_x = false
 stretch_y = false
 stay_on_screen = true
{req(load_id)}
 is_hidden = false
 automated_button_clicks = 0
}}
"""


def custom_button(
    id_str: str,
    exec_id: str,
    action_id: str,
    action: str,
    value: str,
    x: int,
    y: int,
    w: int,
    h: int,
    label: str,
    bg: str,
    hover: str,
    nine_border: int = 14,
    anchor: str = "mid-centered",
) -> str:
    return f"""element {{
 button_element_executable_block_identifier = {exec_id}
 [executable_action_instance:{action_id}][action_type:{action}] = {value}
 [executable_block:{exec_id}][type:generic] = [executables:{action_id};]
 backgroundnormal = [source:local]/config/fancymenu/assets/{bg}
 backgroundhovered = [source:local]/config/fancymenu/assets/{hover}
 restartbackgroundanimations = true
 nine_slice_custom_background = true
 nine_slice_border_x = {nine_border}
 nine_slice_border_y = {nine_border}
 label = {label}
 label_shadow = false
{widget_meta(f"wid-{id_str}")}
 element_type = custom_button
 instance_identifier = {id_str}
{META_TAIL}
 anchor_point = {anchor}
 x = {x}
 y = {y}
 width = {w}
 height = {h}
 stretch_x = false
 stretch_y = false
 stay_on_screen = true
{req(id_str.replace("btn", "load"))}
}}
"""


def image_element(
    id_str: str,
    file: str,
    x: int,
    y: int,
    w: int,
    h: int,
    anchor: str = "mid-centered",
    nine_slice: bool = False,
    nine_border: int = 24,
) -> str:
    return f"""element {{
 source = [source:local]/config/fancymenu/assets/{file}
 repeat_texture = false
 nine_slice_texture = {str(nine_slice).lower()}
 nine_slice_texture_border_x = {nine_border}
 nine_slice_texture_border_y = {nine_border}
 image_tint = #FFFFFF
 restart_animated_on_menu_load = false
 element_type = image
 instance_identifier = {id_str}
{META_TAIL}
 anchor_point = {anchor}
 x = {x}
 y = {y}
 width = {w}
 height = {h}
 stretch_x = false
 stretch_y = false
 stay_on_screen = true
{req(f"{id_str}-load")}
}}
"""


def layout_header(identifier: str, image: bool = True, blur: bool = False) -> str:
    bg_block = (
        """menu_background {
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
"""
        if image
        else ""
    )

    return f"""type = fancymenu_layout

layout-meta {{
 identifier = {identifier}
 render_custom_elements_behind_vanilla = true
 last_edited_time = 1756531200000
 is_enabled = true
 randommode = false
 randomgroup = 1
 randomonlyfirsttime = false
 layout_index = 0
 [loading_requirement_container_meta:meta-{identifier}] = [groups:][instances:]
}}

customization {{
 action = setscale
 scale = 2.0
}}

customization {{
 action = autoscale
 basewidth = 1920
 baseheight = 1080
}}

{bg_block}scroll_list_customization {{
 preserve_scroll_list_header_footer_aspect_ratio = true
 render_scroll_list_header_shadow = true
 render_scroll_list_footer_shadow = true
 show_scroll_list_header_footer_preview_in_editor = false
 repeat_scroll_list_header_texture = false
 repeat_scroll_list_footer_texture = false
 show_screen_background_overlay_on_custom_background = false
 apply_vanilla_background_blur = {'true' if blur else 'false'}
}}

layout_action_executable_blocks {{
}}

"""


def generate_layouts():
    CUSTOM.mkdir(parents=True, exist_ok=True)
    n = 0

    def uid(prefix):
        nonlocal n
        n += 1
        return f"{prefix}-{str(n).padStart(3, '0') if hasattr(str(n), 'padStart') else str(n).zfill(3)}"

    # Title screen geometry
    card_w = 280
    card_h = 420
    card_x = - (card_w // 2)  # -140
    card_y = - (card_h // 2)  # -210

    btn_w = 232
    btn_x = - (btn_w // 2)   # -116

    emblem_w = 64
    emblem_h = 64
    emblem_x = - (emblem_w // 2)  # -32
    emblem_y = card_y + 18        # -192

    btn_play_y = card_y + 92      # -118
    btn_sp_y = btn_play_y + 48    # -70
    btn_opt_y = btn_sp_y + 46     # -24
    btn_site_y = btn_opt_y + 46   # 22
    btn_disc_y = btn_site_y + 46  # 68
    btn_quit_y = btn_disc_y + 46  # 114

    title_elements = [
        layout_header("title_screen", image=True, blur=False),
        # Frosted glass card in center
        image_element("title-card", "pause_glass.png", card_x, card_y, card_w, card_h, anchor="mid-centered", nine_slice=True, nine_border=24),
        # Glowing Crystal Emblem
        image_element("title-crystal", "logo_crystal.png", emblem_x, emblem_y, emblem_w, emblem_h, anchor="mid-centered", nine_slice=False),
        # 1. Primary Play Button
        custom_button("btn-play", uid("ex"), uid("act"), "joinserver", "g-pl-3.apexnodes.xyz:21561",
                      btn_x, btn_play_y, btn_w, 42,
                      '{"text":"\\u0421\\u0435\\u0440\\u0432\\u0435\\u0440 AquaTech","color":"#031018","bold":false,"font":"aquatech_ui:header"}',
                      "btn_play.png", "btn_play_hover.png", nine_border=16, anchor="mid-centered"),
        # 2. Singleplayer
        moved_vanilla("mc_titlescreen_singleplayer_button", uid("ex"), uid("w"), uid("l"),
                      btn_x, btn_sp_y, btn_w, 40,
                      '{"text":"\\u041e\\u0434\\u0438\\u043d\\u043e\\u0447\\u043d\\u0430\\u044f \\u0438\\u0433\\u0440\\u0430","color":"#E0F7FA","font":"aquatech_ui:header"}',
                      "btn_ghost.png", "btn_ghost_hover.png", nine_border=14, anchor="mid-centered"),
        # 3. Settings
        moved_vanilla("mc_titlescreen_options_button", uid("ex"), uid("w"), uid("l"),
                      btn_x, btn_opt_y, btn_w, 40,
                      '{"text":"\\u041d\\u0430\\u0441\\u0442\\u0440\\u043e\\u0439\\u043a\\u0438","color":"#E0F7FA","font":"aquatech_ui:header"}',
                      "btn_ghost.png", "btn_ghost_hover.png", nine_border=14, anchor="mid-centered"),
        # 4. Website
        custom_button("btn-site", uid("ex"), uid("act"), "openlink", "https://aquateche.store",
                      btn_x, btn_site_y, btn_w, 40,
                      '{"text":"\\u0421\\u0430\\u0439\\u0442","color":"#E0F7FA","font":"aquatech_ui:header"}',
                      "btn_ghost.png", "btn_ghost_hover.png", nine_border=14, anchor="mid-centered"),
        # 5. Discord
        custom_button("btn-discord", uid("ex"), uid("act"), "openlink", "https://discord.gg/3Khzr5z4fQ",
                      btn_x, btn_disc_y, btn_w, 40,
                      '{"text":"Discord","color":"#E0F7FA","font":"aquatech_ui:header"}',
                      "btn_ghost.png", "btn_ghost_hover.png", nine_border=14, anchor="mid-centered"),
        # 6. Exit
        moved_vanilla("mc_titlescreen_quit_button", uid("ex"), uid("w"), uid("l"),
                      btn_x, btn_quit_y, btn_w, 40,
                      '{"text":"\\u0412\\u044b\\u0445\\u043e\\u0434","color":"#E0F7FA","font":"aquatech_ui:header"}',
                      "btn_ghost.png", "btn_ghost_hover.png", nine_border=14, anchor="mid-centered"),
        # Mojang copyright (clean, bottom-right corner)
        moved_vanilla("mc_titlescreen_copyright_button", uid("ex"), uid("w"), uid("l"),
                      -240, -18, 230, 14, "", "", "", anchor="bottom-right"),
    ]

    hidden_titles = [
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
    ]

    for hid in hidden_titles:
        title_elements.append(hidden_vanilla(hid, uid("ex"), uid("w"), uid("l")))

    (CUSTOM / "title_screen_layout.txt").write_text("\n".join(title_elements), encoding="utf-8")
    print("   -> title_screen_layout.txt written")

    # Pause screen geometry
    pause_card_h = 370
    p_card_y = - (pause_card_h // 2)  # -185
    p_emblem_y = p_card_y + 18        # -167

    p_btn1 = p_card_y + 90            # -95
    p_btn2 = p_btn1 + 48              # -47
    p_btn3 = p_btn2 + 46              # -1
    p_btn4 = p_btn3 + 46              # 45
    p_btn5 = p_btn4 + 46              # 91

    pause_elements = [
        layout_header("pause_screen", image=False, blur=True),
        # Center card
        image_element("pause-card", "pause_glass.png", card_x, p_card_y, card_w, pause_card_h, anchor="mid-centered", nine_slice=True, nine_border=24),
        # Emblem
        image_element("pause-crystal", "logo_crystal.png", emblem_x, p_emblem_y, emblem_w, emblem_h, anchor="mid-centered", nine_slice=False),
        # 1. Continue
        moved_vanilla("pause_return_to_game_button", uid("ex"), uid("w"), uid("l"),
                      btn_x, p_btn1, btn_w, 42,
                      '{"text":"\\u041f\\u0440\\u043e\\u0434\\u043e\\u043b\\u0436\\u0438\\u0442\\u044c","color":"#031018","bold":false,"font":"aquatech_ui:header"}',
                      "btn_play.png", "btn_play_hover.png", nine_border=16, anchor="mid-centered"),
        # 2. Options
        moved_vanilla("pause_options_button", uid("ex"), uid("w"), uid("l"),
                      btn_x, p_btn2, btn_w, 40,
                      '{"text":"\\u041d\\u0430\\u0441\\u0442\\u0440\\u043e\\u0439\\u043a\\u0438","color":"#E0F7FA","font":"aquatech_ui:header"}',
                      "btn_ghost.png", "btn_ghost_hover.png", nine_border=14, anchor="mid-centered"),
        # 3. Advancements
        moved_vanilla("pause_advancements_button", uid("ex"), uid("w"), uid("l"),
                      btn_x, p_btn3, btn_w, 40,
                      '{"text":"\\u041f\\u0440\\u043e\\u0433\\u0440\\u0435\\u0441\\u0441","color":"#E0F7FA","font":"aquatech_ui:header"}',
                      "btn_ghost.png", "btn_ghost_hover.png", nine_border=14, anchor="mid-centered"),
        # 4. Stats
        moved_vanilla("pause_stats_button", uid("ex"), uid("w"), uid("l"),
                      btn_x, p_btn4, btn_w, 40,
                      '{"text":"\\u0421\\u0442\\u0430\\u0442\\u0438\\u0441\\u0442\\u0438\\u043a\\u0430","color":"#E0F7FA","font":"aquatech_ui:header"}',
                      "btn_ghost.png", "btn_ghost_hover.png", nine_border=14, anchor="mid-centered"),
        # 5. Disconnect
        moved_vanilla("pause_disconnect_button", uid("ex"), uid("w"), uid("l"),
                      btn_x, p_btn5, btn_w, 40,
                      '{"text":"\\u041e\\u0442\\u043a\\u043b\\u044e\\u0447\\u0438\\u0442\\u044c\\u0441\\u044f","color":"#E0F7FA","font":"aquatech_ui:header"}',
                      "btn_ghost.png", "btn_ghost_hover.png", nine_border=14, anchor="mid-centered"),
    ]

    pause_hidden = [
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
    ]

    for hid in pause_hidden:
        pause_elements.append(hidden_vanilla(hid, uid("ex"), uid("w"), uid("l")))

    (CUSTOM / "pause_screen_layout.txt").write_text("\n".join(pause_elements), encoding="utf-8")
    print("   -> pause_screen_layout.txt written")


def main():
    print("=== Generating FancyMenu Assets ===")
    generate_textures()
    print("=== Generating FancyMenu Layouts ===")
    generate_layouts()
    print("=== Done! ===")


if __name__ == "__main__":
    main()
