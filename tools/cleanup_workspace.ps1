# Clean AquaTech workspace clutter. Safe deletes only (no server/world/config wipe).
$ErrorActionPreference = "Continue"
$root = "C:\Users\xieto\Desktop\AquaTech"
Set-Location $root

function Remove-PathSafe([string]$rel) {
  $p = Join-Path $root $rel
  if (-not (Test-Path -LiteralPath $p)) { return }
  $item = Get-Item -LiteralPath $p -Force
  $size = if ($item.PSIsContainer) {
    [math]::Round(((Get-ChildItem -LiteralPath $p -Recurse -File -Force -EA SilentlyContinue | Measure-Object Length -Sum).Sum)/1MB, 1)
  } else {
    [math]::Round($item.Length/1MB, 1)
  }
  try {
    Remove-Item -LiteralPath $p -Recurse -Force -EA Stop
    Write-Host "DEL  ${size} MB  $rel"
  } catch {
    Write-Host "FAIL $rel  $_"
  }
}

Write-Host "=== 1) Cache / backup / parked / duplicate source trees ==="
@(
  "__pycache__",
  "_backup_ftbquests_20260803_234236",
  "_backup_ftbquests_20260803_234256",
  "_mod_dl_cache",
  "_mod_download",
  "_parked_mods_2026-08-03",
  "_tmp_iu_recipes",
  "aquatech-ui",
  "art_source",
  "balance-minigame-icons",
  "casesmod-fixed-source_1",
  "casesmod-fixed-source_1.zip",
  "extracted_elements_png",
  "img",
  "scratch",
  "scripts_crafttweaker_archived",
  "modlist",
  "_iu_GuideQuest.class.txt",
  "_iu_GuideTab.class.txt",
  "photo_2026-08-03_20-18-54.jpg"
) | ForEach-Object { Remove-PathSafe $_ }

Write-Host "=== 2) mods/ parked + zip archives ==="
@(
  "mods\_parked_casesmod_old",
  "mods\_parked_client_only",
  "mods\_parked_join_fix",
  "mods\_parked_old_ftb",
  "mods\aquatech-ui.zip",
  "mods\aquatech-ui (2).zip",
  "mods\mods.rar"
) | ForEach-Object { Remove-PathSafe $_ }

Write-Host "=== 3) tools/ cloned repos / MDK / caches ==="
@(
  "tools\Custom-Nameplates",
  "tools\Custom-Nameplates-247",
  "tools\forge-mdk-1.20.1.zip",
  "tools\__pycache__"
) | ForEach-Object { Remove-PathSafe $_ }

Write-Host "=== 4) Old fishing minigame textures (pixel/ is the live set) ==="
$mg = Join-Path $root "mods\aquatech-ui\src\main\resources\assets\aquatech_ui\textures\gui\minigame"
if (Test-Path $mg) {
  @(
    "balance",
    "tide_tension",
    "fish_and_hook_markers.png",
    "full_minigame_hud.png",
    "progress_bar_animated.gif",
    "sonar_radar_widget.png",
    "tension_gauge_frame.png",
    "tide_fish_a.png",
    "tide_fish_b.png",
    "tide_hook_marker.png",
    "tide_star_empty.png",
    "tide_star_filled.png",
    "victory_catch_screen.png"
  ) | ForEach-Object {
    $p = Join-Path $mg $_
    if (Test-Path -LiteralPath $p) {
      Remove-Item -LiteralPath $p -Recurse -Force
      Write-Host "DEL  minigame/$_"
    }
  }
}

Write-Host "=== 5) One-shot root scripts (keep deploy + maintenance whitelist) ==="
$keep = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
@(
  # deploy / setup
  "deploy_aquatech_ui.ps1","deploy_casesmod.ps1","deploy_industrial_upgrade.ps1","deploy_runtime.ps1","setup_horizon_route.ps1",
  "setup_luckperms_config.py","setup_luckperms_groups.py","setup_ocean_world.py","setup_mohist_server.py",
  "configure_minimal_default_permissions.py","configure_worldguard_explosions.py",
  "install_dev_overlay_mods.py","install_kubejs.py","export_client_pack.py","prune_mods_whitelist.py",
  "download_industrial_upgrade.py","update_fawe.py",
  # quests / content regen
  "generate_600_ocean_quests.py","generate_workshop_quests.py","workshop_guides.py","workshop_quest_extras.py",
  "wire_aquatech_quests.py","wire_aquatech_quests_p2.py","inject_aqua_xp_rewards.py","strip_op_quest_rewards.py",
  "validate_quests.py","check_all_chapters.py","build_ftb_quests.py","gen_iu_guide_ftbquests.py","patch_ftbquests.py",
  "make_boot_fixes_datapack.py","fix_iu_item_ids.py","patch_skyblockbuilder_exitportal.py",
  # GUI / assets still useful
  "gen_machine_guis.py","gen_clean_machine_guis.py",
  # docs
  "CHANGELOG.md","HORIZON_ROUTE.md","PLAYER_ROADMAP.md","QUEST_ID_FREEZE.md",".gitignore"
) | ForEach-Object { [void]$keep.Add($_) }

$deletedScripts = 0
Get-ChildItem $root -File | Where-Object {
  $_.Extension -in ".py",".ps1",".txt",".jpg",".jpeg",".png",".gif" -and -not $keep.Contains($_.Name)
} | ForEach-Object {
  # never delete jar/json/snbt here
  Remove-Item -LiteralPath $_.FullName -Force
  Write-Host "DEL  script $($_.Name)"
  $script:deletedScripts++
}
Write-Host "Deleted $deletedScripts root scripts/misc files"

Write-Host "=== 6) Nested __pycache__ under tools/mods sources ==="
Get-ChildItem $root -Recurse -Directory -Filter "__pycache__" -EA SilentlyContinue |
  Where-Object { $_.FullName -notmatch '\\server\\|\\client\\|\\dist\\|\\\.git\\|\\mods\\[^\\]+\.jar' } |
  ForEach-Object {
    Remove-Item -LiteralPath $_.FullName -Recurse -Force -EA SilentlyContinue
    Write-Host "DEL  $($_.FullName.Substring($root.Length+1))"
  }

Write-Host "DONE"
