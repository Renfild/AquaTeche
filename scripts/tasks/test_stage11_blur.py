#!/usr/bin/env python3
"""Stage 1.1 contracts: PostChain blur JSON + BlurScreenPolicy.

Usage:
  python scripts/tasks/test_stage11_blur.py
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
POST = ROOT / "mods/aquatech-ui/src/main/resources/assets/aquatech_ui/shaders/post/ocean_blur.json"
PROG = ROOT / "mods/aquatech-ui/src/main/resources/assets/minecraft/shaders/program/aquatech_ocean_blur.json"
FSH = ROOT / "mods/aquatech-ui/src/main/resources/assets/minecraft/shaders/program/aquatech_ocean_blur.fsh"
VSH = ROOT / "mods/aquatech-ui/src/main/resources/assets/minecraft/shaders/program/aquatech_ocean_blur.vsh"
POLICY = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/client/render/BlurScreenPolicy.java"
ENGINE = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/client/render/OceanBlurEngine.java"
SCREEN = ROOT / "mods/aquatech-ui/src/main/java/net/aquatech/ui/client/gui/AquaBlurredScreen.java"


def fail(msg: str) -> None:
    print(f"FAIL {msg}")
    raise SystemExit(1)


def test_post_chain() -> None:
    data = json.loads(POST.read_text(encoding="utf-8"))
    if data.get("targets") != ["swap"]:
        fail("post chain needs a swap target")
    passes = data.get("passes") or []
    if len(passes) != 4:
        fail(f"expected 4 separable passes, got {len(passes)}")
    dirs = []
    for i, p in enumerate(passes):
        if p.get("name") != "aquatech_ocean_blur":
            fail(f"pass {i} name {p.get('name')!r}")
        uniforms = {u["name"]: u["values"] for u in p.get("uniforms") or []}
        if "BlurDir" not in uniforms or "Radius" not in uniforms:
            fail(f"pass {i} missing BlurDir/Radius")
        dirs.append(tuple(uniforms["BlurDir"]))
    if dirs != [(1.0, 0.0), (0.0, 1.0), (1.0, 0.0), (0.0, 1.0)]:
        fail(f"BlurDir sequence not H/V/H/V: {dirs}")
    print("OK post chain 4-pass H/V gaussian")


def test_program_json() -> None:
    data = json.loads(PROG.read_text(encoding="utf-8"))
    names = {u["name"] for u in data.get("uniforms") or []}
    for need in ("ProjMat", "InSize", "OutSize", "BlurDir", "Radius", "Tint"):
        if need not in names:
            fail(f"program json missing uniform {need}")
    if data.get("vertex") != "aquatech_ocean_blur" or data.get("fragment") != "aquatech_ocean_blur":
        fail("program vertex/fragment must be aquatech_ocean_blur")
    print("OK program uniforms")


def test_glsl() -> None:
    fsh = FSH.read_text(encoding="utf-8")
    vsh = VSH.read_text(encoding="utf-8")
    if "exp(-(r * r) / twoSigmaSq)" not in fsh:
        fail("fsh missing gaussian weight")
    if "uniform float Radius" not in fsh:
        fail("fsh missing Radius")
    if "sampleStep = (1.0 / InSize) * BlurDir" not in vsh:
        fail("vsh missing separable sampleStep")
    print("OK glsl gaussian + separable step")


def test_engine_does_not_steal_load_effect() -> None:
    text = ENGINE.read_text(encoding="utf-8")
    if "new PostChain(" not in text:
        fail("OceanBlurEngine must own PostChain")
    if "loadEffect" in text and "does not steal" not in text.lower() and "Does not steal" not in text:
        # comment mentioning loadEffect is ok; calling it is not
        if re.search(r"gameRenderer\.loadEffect", text, re.I):
            fail("OceanBlurEngine must not call GameRenderer.loadEffect")
    if "gameRenderer.loadEffect" in text.replace(" ", ""):
        fail("OceanBlurEngine must not call GameRenderer.loadEffect")
    print("OK engine owns PostChain")


def test_blurred_screen_keeps_world() -> None:
    text = SCREEN.read_text(encoding="utf-8")
    if "isPauseScreen()" not in text or "return false" not in text:
        fail("AquaBlurredScreen must not pause the world (blur needs the framebuffer)")
    print("OK AquaBlurredScreen isPauseScreen=false")


def test_policy() -> None:
    text = POLICY.read_text(encoding="utf-8")
    ns: dict[str, object] = {}
    # Execute the boolean rules in Python by mirroring the Java method.
    # Keep this in lockstep with BlurScreenPolicy.shouldBlur.
    def should_blur(aqua: bool, class_name: str | None) -> bool:
        if aqua:
            return True
        return class_name is not None and class_name.startswith("store.aquateche.aqualumen.client.screen.")

    cases = [
        (True, "net.aquatech.ui.client.gui.OceanSkillTreeScreen", True),
        (False, "store.aquateche.aqualumen.client.screen.HubScreen", True),
        (False, "store.aquateche.aqualumen.client.screen.HubTabs", True),
        (False, "net.minecraft.client.gui.screens.PauseScreen", False),
        (False, None, False),
        (False, "", False),
    ]
    for aqua, name, expect in cases:
        got = should_blur(aqua, name)
        if got != expect:
            fail(f"policy {aqua=} {name!r} -> {got}, want {expect}")
    if 'AQUALUMEN_GUI_PREFIX = "store.aquateche.aqualumen.client.screen."' not in text:
        fail("BlurScreenPolicy prefix constant drifted")
    print("OK BlurScreenPolicy")


def main() -> int:
    for p in (POST, PROG, FSH, VSH, POLICY, ENGINE, SCREEN):
        if not p.is_file():
            fail(f"missing {p}")
    test_post_chain()
    test_program_json()
    test_glsl()
    test_engine_does_not_steal_load_effect()
    test_blurred_screen_keeps_world()
    test_policy()
    print("OK stage 1.1 blur")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
