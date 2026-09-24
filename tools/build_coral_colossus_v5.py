"""Build Coral Colossus v5 - 100% 1:1 replica of user's reference screenshots."""
import json
import base64
import time
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from tools.bb_mcp import handshake, call

UVS = {
    "COPPER_RIVETS": [0, 0, 16, 16],
    "COPPER_PATINA": [16, 0, 32, 16],
    "COPPER_RUNE_DIAMOND": [0, 16, 16, 32],
    "COPPER_BAR": [16, 16, 32, 32],
    "STEEL_PLATES": [32, 0, 48, 16],
    "STEEL_VENTS": [48, 0, 64, 16],
    "STEEL_BOLTS": [32, 16, 48, 32],
    "STEEL_DISC_JOINT": [48, 16, 64, 32],
    "CORE_CYAN": [0, 32, 32, 64],
    "GEM_CYAN": [8, 40, 24, 56],
    "CORAL_RED": [32, 32, 48, 48],
    "CORAL_YELLOW": [48, 32, 64, 48],
    "PRISMARINE_WAIST": [32, 48, 48, 64],
    "DEEPSLATE_MEDALLION": [48, 48, 64, 64],
}

def generate_script() -> str:
    js_lines = []
    def emit(line: str):
        js_lines.append(line)

    emit("(() => {")
    emit("  Project.box_uv = false;")
    emit("  Project.texture_width = 64;")
    emit("  Project.texture_height = 64;")
    emit("  Cube.all.slice().forEach(c => c.remove());")
    emit("  Group.all.slice().forEach(g => g.remove());")
    emit("  Animation.all.slice().forEach(a => a.remove());")

    emit("  let atlasTex = Texture.all.find(t => t.name.includes('coral_colossus_atlas'));")
    emit("  let emissiveTex = Texture.all.find(t => t.name.includes('coral_colossus_emissive'));")
    emit("  if (!atlasTex && Texture.all.length > 0) atlasTex = Texture.all[0];")
    emit("  if (!emissiveTex && Texture.all.length > 1) emissiveTex = Texture.all[1];")

    emit("  const atlasUuid = atlasTex ? atlasTex.uuid : null;")
    emit("  const uvs = " + json.dumps(UVS) + ";")

    emit("""
  function makeCube(name, from, to, group, uvKey, customFaces = null) {
    const c = new Cube({
      name: name,
      from: from,
      to: to,
      autouv: 0
    }).init(null);
    c.box_uv = false;
    const defaultUV = uvs[uvKey] || [0, 0, 16, 16];
    const faceKeys = ['north', 'south', 'east', 'west', 'up', 'down'];
    faceKeys.forEach(f => {
      let faceUv = defaultUV;
      let faceTex = atlasUuid;
      if (customFaces && customFaces[f]) {
        faceUv = uvs[customFaces[f].uv] || defaultUV;
      }
      c.faces[f].texture = faceTex;
      c.faces[f].uv = [faceUv[0], faceUv[1], faceUv[2], faceUv[3]];
    });
    if (group) c.addTo(group);
    return c;
  }
""")

    # Rigging: authentic dynamic heroic stance matching reference screenshots
    emit("""
  const rootGroup = new Group({ name: "Root", origin: [0, 0, 0] }).init();

  const bodyGroup = new Group({ name: "Body", origin: [0, 30, 0], rotation: [3, 0, 0] }).init();
  bodyGroup.addTo(rootGroup);

  const headGroup = new Group({ name: "Head", origin: [0, 58, 0], rotation: [-2, 0, 0] }).init();
  headGroup.addTo(bodyGroup);

  const armLGroup = new Group({ name: "Arm_L", origin: [19, 49, 0], rotation: [-6, 0, -8] }).init();
  armLGroup.addTo(bodyGroup);

  const forearmLGroup = new Group({ name: "Forearm_L", origin: [20, 35, 0], rotation: [30, -10, 6] }).init();
  forearmLGroup.addTo(armLGroup);

  const armRGroup = new Group({ name: "Arm_R", origin: [-19, 49, 0], rotation: [-6, 0, 8] }).init();
  armRGroup.addTo(bodyGroup);

  const forearmRGroup = new Group({ name: "Forearm_R", origin: [-20, 35, 0], rotation: [30, 10, -6] }).init();
  forearmRGroup.addTo(armRGroup);

  const legLGroup = new Group({ name: "Leg_L", origin: [9, 30, 0], rotation: [-6, -10, -4] }).init();
  legLGroup.addTo(rootGroup);

  const calfLGroup = new Group({ name: "Calf_L", origin: [9, 20, 0], rotation: [12, 0, 0] }).init();
  calfLGroup.addTo(legLGroup);

  const footLGroup = new Group({ name: "Foot_L", origin: [9, 4, 0], rotation: [-6, 10, 4] }).init();
  footLGroup.addTo(calfLGroup);

  const legRGroup = new Group({ name: "Leg_R", origin: [-9, 30, 0], rotation: [-6, 10, 4] }).init();
  legRGroup.addTo(rootGroup);

  const calfRGroup = new Group({ name: "Calf_R", origin: [-9, 20, 0], rotation: [12, 0, 0] }).init();
  calfRGroup.addTo(legRGroup);

  const footRGroup = new Group({ name: "Foot_R", origin: [-9, 4, 0], rotation: [-6, -10, -4] }).init();
  footRGroup.addTo(calfRGroup);
""")

    # 1. HEAD & HELMET (slits, brow ridge, stepped plates, crest)
    emit("""
  makeCube("HEAD_NECK", [-3.5, 56, -3.5], [3.5, 59, 3.5], headGroup, "STEEL_BOLTS");
  makeCube("HEAD_BASE", [-6, 58, -6], [6, 68, 6], headGroup, "COPPER_PATINA");
  makeCube("HEAD_BROW", [-6.2, 64.5, -7.2], [6.2, 67.5, -5.0], headGroup, "COPPER_RIVETS");
  makeCube("HEAD_FACEPLATE", [-5, 60.5, -7.0], [5, 64.5, -5.5], headGroup, "COPPER_BAR");
  makeCube("HEAD_EYE_L", [1.5, 62.5, -7.3], [4.5, 64.5, -6.8], headGroup, "GEM_CYAN");
  makeCube("HEAD_EYE_R", [-4.5, 62.5, -7.3], [-1.5, 64.5, -6.8], headGroup, "GEM_CYAN");
  makeCube("HEAD_NOSE_BRIDGE", [-1.5, 61, -7.2], [1.5, 65, -6.8], headGroup, "COPPER_BAR");
  makeCube("HEAD_CHIN", [-4, 58, -7.0], [4, 61, -5.5], headGroup, "COPPER_RIVETS");
  makeCube("HEAD_CREST", [-1.8, 68, -5.5], [1.8, 72, 4.5], headGroup, "COPPER_RIVETS");
  makeCube("HEAD_TOP", [-5.8, 67.5, -5.8], [5.8, 69.2, 5.8], headGroup, "COPPER_PATINA");
  makeCube("HEAD_SIDE_L", [5.8, 59, -5.5], [7.0, 66, 2.5], headGroup, "COPPER_BAR");
  makeCube("HEAD_SIDE_R", [-7.0, 59, -5.5], [-5.8, 66, 2.5], headGroup, "COPPER_BAR");
""")

    # 2. TORSO, PECTORALS, ABDOMEN & BACK
    emit("""
  makeCube("PELVIS_BASE", [-8, 29, -5.5], [8, 37, 5.5], bodyGroup, "COPPER_PATINA");
  makeCube("PELVIS_BELT_RIM", [-7, 30.5, -6.8], [7, 36.5, -5.5], bodyGroup, "COPPER_RIVETS");
  makeCube("PELVIS_GROIN_SHIELD", [-4, 23.5, -7.0], [4, 31.5, -5.5], bodyGroup, "COPPER_RUNE_DIAMOND");
  makeCube("PELVIS_TASSET_L", [7.5, 27, -5.0], [9.5, 34, 4.0], bodyGroup, "COPPER_BAR");
  makeCube("PELVIS_TASSET_R", [-9.5, 27, -5.0], [-7.5, 34, 4.0], bodyGroup, "COPPER_BAR");
  makeCube("PELVIS_HIP_SOCKET_L", [7.0, 29.5, -3.0], [9.5, 34.5, 3.0], bodyGroup, "STEEL_DISC_JOINT");
  makeCube("PELVIS_HIP_SOCKET_R", [-9.5, 29.5, -3.0], [-7.0, 34.5, 3.0], bodyGroup, "STEEL_DISC_JOINT");

  makeCube("TORSO_WAIST_GREEN", [-9.5, 36, -6.2], [9.5, 42.5, 6.2], bodyGroup, "PRISMARINE_WAIST");
  makeCube("TORSO_PISTON_L", [7.0, 36, -7.0], [9.5, 42.5, -5.0], bodyGroup, "STEEL_PLATES");
  makeCube("TORSO_PISTON_R", [-9.5, 36, -7.0], [-7.0, 42.5, -5.0], bodyGroup, "STEEL_PLATES");

  makeCube("BELLY_MEDALLION_FRAME", [-6, 36.5, -7.8], [6, 44.5, -6.5], bodyGroup, "STEEL_BOLTS");
  makeCube("BELLY_MEDALLION_RING", [-5, 37.5, -8.4], [5, 43.5, -7.5], bodyGroup, "PRISMARINE_WAIST");
  makeCube("BELLY_MEDALLION_DISC", [-4, 38.5, -8.8], [4, 42.5, -8.2], bodyGroup, "DEEPSLATE_MEDALLION");

  makeCube("TORSO_CHASSIS", [-13.5, 42, -7.5], [13.5, 57, 7.5], bodyGroup, "COPPER_PATINA");
  makeCube("PECTORAL_L", [5, 44, -9.2], [14, 56.5, -7.2], bodyGroup, "COPPER_RIVETS");
  makeCube("PECTORAL_INSET_L", [7, 47, -9.5], [12, 53.5, -9.1], bodyGroup, "STEEL_BOLTS");
  makeCube("PECTORAL_R", [-14, 44, -9.2], [-5, 56.5, -7.2], bodyGroup, "COPPER_RIVETS");
  makeCube("PECTORAL_INSET_R", [-12, 47, -9.5], [-7, 53.5, -9.1], bodyGroup, "STEEL_BOLTS");
  makeCube("CHEST_CHEVRON", [-5, 43, -8.6], [5, 47, -7.2], bodyGroup, "COPPER_BAR");
  makeCube("COLLAR_PLATE", [-8.5, 56, -9.2], [8.5, 59.5, -7.2], bodyGroup, "COPPER_RIVETS");

  makeCube("BACK_ARMOR_PLATE", [-12, 42, 7.5], [12, 57, 10.5], bodyGroup, "COPPER_PATINA");
  makeCube("BACK_VENT_SLOT_L", [4, 48, 10.2], [9, 56, 11.2], bodyGroup, "STEEL_VENTS");
  makeCube("BACK_VENT_SLOT_R", [-9, 48, 10.2], [-4, 56, 11.2], bodyGroup, "STEEL_VENTS");
  makeCube("SPINE_CONDUIT", [-2.5, 34, 7.5], [2.5, 46, 8.8], bodyGroup, "STEEL_PLATES");

  makeCube("CORAL_BACK_RED_BASE", [-4, 57, 8.5], [4, 73, 12.5], bodyGroup, "CORAL_RED");
  makeCube("CORAL_BACK_RED_BRANCH_1", [-6, 68, 9.5], [-2, 79, 13.5], bodyGroup, "CORAL_RED");
  makeCube("CORAL_BACK_RED_BRANCH_2", [2, 68, 9.5], [6, 79, 13.5], bodyGroup, "CORAL_RED");
  makeCube("CORAL_BACK_RED_SPIRE", [-2, 75, 10.5], [2, 83, 14], bodyGroup, "CORAL_RED");

  makeCube("CORAL_BACK_YEL_L1", [5, 60, 8.5], [10, 72, 12.5], bodyGroup, "CORAL_YELLOW");
  makeCube("CORAL_BACK_YEL_L2", [7, 70, 9.5], [11.5, 80, 13.5], bodyGroup, "CORAL_YELLOW");
  makeCube("CORAL_BACK_YEL_R1", [-10, 60, 8.5], [-5, 72, 12.5], bodyGroup, "CORAL_YELLOW");
  makeCube("CORAL_BACK_YEL_R2", [-11.5, 70, 9.5], [-7, 80, 13.5], bodyGroup, "CORAL_YELLOW");
""")

    # 3. LEFT ARM & CANNON GAUNTLET (with branching corals sprouting to the side)
    emit("""
  makeCube("SHOULDER_MAIN_L", [14, 48, -7.5], [27, 59.5, 7.5], armLGroup, "COPPER_PATINA");
  makeCube("SHOULDER_RIM_L", [26.5, 50, -6.5], [28.2, 57.5, 6.5], armLGroup, "COPPER_RIVETS");
  makeCube("SHOULDER_RIDGE_L", [15, 59, -6.5], [26, 61, 6.5], armLGroup, "COPPER_RIVETS");
  makeCube("SHOULDER_SLOT_FRONT_L", [17, 50, -8.2], [22, 57, -7.4], armLGroup, "STEEL_VENTS");
  makeCube("SHOULDER_SLOT_SIDE_L", [26.8, 51, -4], [27.8, 57, 4], armLGroup, "STEEL_VENTS");

  makeCube("CORAL_SHOULDER_YEL_L1", [17, 59.5, -4.5], [22, 69, 0.5], armLGroup, "CORAL_YELLOW");
  makeCube("CORAL_SHOULDER_YEL_L2", [18, 68, -3.5], [23, 74, 1.5], armLGroup, "CORAL_YELLOW");
  makeCube("CORAL_SHOULDER_RED_L1", [20, 59.5, 0], [25.5, 67, 5.5], armLGroup, "CORAL_RED");
  makeCube("CORAL_SHOULDER_RED_L2", [21, 66, 1], [26.5, 73, 6], armLGroup, "CORAL_RED");

  makeCube("UPPER_ARM_L", [16.5, 38, -4], [23.5, 49, 4], armLGroup, "STEEL_PLATES");
  makeCube("BICEP_ARMOR_L", [16, 39, -5.5], [24, 48, -3.8], armLGroup, "COPPER_BAR");
  makeCube("ELBOW_JOINT_L", [16, 33, -4.5], [24.5, 38, 4.5], armLGroup, "STEEL_DISC_JOINT");

  makeCube("GAUNTLET_BODY_L", [14.5, 21, -7.5], [25.5, 36, 5.5], forearmLGroup, "COPPER_PATINA");
  makeCube("GAUNTLET_CUFF_L", [14.0, 33.5, -8.0], [26.0, 36.5, 6.0], forearmLGroup, "COPPER_RIVETS");

  makeCube("CANNON_BEZEL_L", [15.0, 21.5, -9.0], [25.0, 31.5, -7.2], forearmLGroup, "COPPER_RIVETS");
  makeCube("CANNON_COLLAR_L", [16.5, 23.0, -9.5], [23.5, 30.0, -8.8], forearmLGroup, "STEEL_BOLTS");
  makeCube("CANNON_CORE_L", [17.5, 24.0, -9.8], [22.5, 29.0, -9.0], forearmLGroup, "CORE_CYAN");

  makeCube("GAUNTLET_SHIELD_L", [25.2, 23, -5.0], [26.8, 33, 4.0], forearmLGroup, "COPPER_RUNE_DIAMOND");
  makeCube("GAUNTLET_GEM_L", [26.5, 26, -2.5], [27.4, 30, 0.5], forearmLGroup, "GEM_CYAN");

  makeCube("CORAL_WRIST_RED_L1", [22, 33, -2], [27, 41, 3], forearmLGroup, "CORAL_RED");
  makeCube("CORAL_WRIST_RED_L2", [24, 39, -1], [29, 46, 4], forearmLGroup, "CORAL_RED");
  makeCube("CORAL_WRIST_YEL_L1", [23, 31, 2], [28, 39, 6.5], forearmLGroup, "CORAL_YELLOW");
  makeCube("CORAL_WRIST_YEL_L2", [25, 37, 3], [30, 44, 8], forearmLGroup, "CORAL_YELLOW");
""")

    # 4. RIGHT ARM & CANNON GAUNTLET (with branching corals sprouting to the side)
    emit("""
  makeCube("SHOULDER_MAIN_R", [-27, 48, -7.5], [-14, 59.5, 7.5], armRGroup, "COPPER_PATINA");
  makeCube("SHOULDER_RIM_R", [-28.2, 50, -6.5], [-26.5, 57.5, 6.5], armRGroup, "COPPER_RIVETS");
  makeCube("SHOULDER_RIDGE_R", [-26, 59, -6.5], [-15, 61, 6.5], armRGroup, "COPPER_RIVETS");
  makeCube("SHOULDER_SLOT_FRONT_R", [-22, 50, -8.2], [-17, 57, -7.4], armRGroup, "STEEL_VENTS");
  makeCube("SHOULDER_SLOT_SIDE_R", [-27.8, 51, -4], [-26.8, 57, 4], armRGroup, "STEEL_VENTS");

  makeCube("CORAL_SHOULDER_RED_R1", [-25.5, 59.5, 0], [-20, 67, 5.5], armRGroup, "CORAL_RED");
  makeCube("CORAL_SHOULDER_RED_R2", [-26.5, 66, 1], [-21, 73, 6], armRGroup, "CORAL_RED");
  makeCube("CORAL_SHOULDER_YEL_R1", [-22, 59.5, -4.5], [-17, 69, 0.5], armRGroup, "CORAL_YELLOW");
  makeCube("CORAL_SHOULDER_YEL_R2", [-23, 68, -3.5], [-18, 74, 1.5], armRGroup, "CORAL_YELLOW");

  makeCube("UPPER_ARM_R", [-23.5, 38, -4], [-16.5, 49, 4], armRGroup, "STEEL_PLATES");
  makeCube("BICEP_ARMOR_R", [-24, 39, -5.5], [-16, 48, -3.8], armRGroup, "COPPER_BAR");
  makeCube("ELBOW_JOINT_R", [-24.5, 33, -4.5], [-16, 38, 4.5], armRGroup, "STEEL_DISC_JOINT");

  makeCube("GAUNTLET_BODY_R", [-25.5, 21, -7.5], [-14.5, 36, 5.5], forearmRGroup, "COPPER_PATINA");
  makeCube("GAUNTLET_CUFF_R", [-26.0, 33.5, -8.0], [-14.0, 36.5, 6.0], forearmRGroup, "COPPER_RIVETS");

  makeCube("CANNON_BEZEL_R", [-25.0, 21.5, -9.0], [-15.0, 31.5, -7.2], forearmRGroup, "COPPER_RIVETS");
  makeCube("CANNON_COLLAR_R", [-23.5, 23.0, -9.5], [-16.5, 30.0, -8.8], forearmRGroup, "STEEL_BOLTS");
  makeCube("CANNON_CORE_R", [-22.5, 24.0, -9.8], [-17.5, 29.0, -9.0], forearmRGroup, "CORE_CYAN");

  makeCube("GAUNTLET_SHIELD_R", [-26.8, 23, -5.0], [-25.2, 33, 4.0], forearmRGroup, "COPPER_RUNE_DIAMOND");
  makeCube("GAUNTLET_GEM_R", [-27.4, 26, -2.5], [-26.5, 30, 0.5], forearmRGroup, "GEM_CYAN");

  makeCube("CORAL_WRIST_RED_R1", [-27, 33, -2], [-22, 41, 3], forearmRGroup, "CORAL_RED");
  makeCube("CORAL_WRIST_RED_R2", [-29, 39, -1], [-24, 46, 4], forearmRGroup, "CORAL_RED");
  makeCube("CORAL_WRIST_YEL_R1", [-28, 31, 2], [-23, 39, 6.5], forearmRGroup, "CORAL_YELLOW");
  makeCube("CORAL_WRIST_YEL_R2", [-30, 37, 3], [-25, 44, 8], forearmRGroup, "CORAL_YELLOW");
""")

    # 5. TALL & POWERFUL LEGS (45% OF TOTAL HEIGHT, DOMINATING SILHOUETTE!)
    emit("""
  makeCube("HIP_JOINT_L", [6.5, 28.5, -3.0], [10.5, 33.5, 3.0], legLGroup, "STEEL_DISC_JOINT");
  makeCube("THIGH_CORE_L", [5, 21, -4.5], [13, 33, 4.5], legLGroup, "STEEL_PLATES");
  makeCube("THIGH_ARMOR_FRONT_L", [5.5, 22, -6.0], [12.5, 32, -4.5], legLGroup, "COPPER_RIVETS");
  makeCube("THIGH_PLATE_SIDE_L", [13.0, 23, -3.5], [14.2, 31, 3.5], legLGroup, "COPPER_BAR");

  makeCube("KNEE_ARMOR_L", [4.5, 19, -7.5], [13.5, 25, -4.5], calfLGroup, "COPPER_RIVETS");
  makeCube("KNEE_BEVEL_L", [5.5, 24, -7.0], [12.5, 27, -4.8], calfLGroup, "COPPER_BAR");
  makeCube("KNEE_DISC_L", [13.0, 18.5, -2.5], [14.5, 23.5, 2.5], calfLGroup, "STEEL_DISC_JOINT");

  makeCube("SHIN_CORE_L", [5, 5, -4.5], [13, 20, 4.5], calfLGroup, "STEEL_PLATES");
  makeCube("SHIN_GUARD_L", [5.5, 7, -6.2], [12.5, 19, -4.5], calfLGroup, "COPPER_RUNE_DIAMOND");
  makeCube("SHIN_PISTON_L", [7.5, 6, 4.5], [10.5, 18, 6.2], calfLGroup, "STEEL_VENTS");
  makeCube("ANKLE_COLLAR_L", [4.5, 5, -5.0], [13.5, 8, 5.0], calfLGroup, "COPPER_BAR");

  makeCube("FOOT_SOLE_L", [4, 0, -11], [14, 2, 7], footLGroup, "COPPER_PATINA");
  makeCube("FOOT_BODY_L", [4.5, 2, -10], [13.5, 5, 6], footLGroup, "COPPER_RIVETS");
  makeCube("FOOT_TOE_1_L", [5.0, 0, -14], [7.5, 3.5, -11], footLGroup, "COPPER_BAR");
  makeCube("FOOT_TOE_2_L", [8.0, 0, -15], [10.5, 4.0, -11], footLGroup, "COPPER_RIVETS");
  makeCube("FOOT_TOE_3_L", [11.0, 0, -14], [13.5, 3.5, -11], footLGroup, "COPPER_BAR");
  makeCube("FOOT_HEEL_L", [6.5, 1, 6], [11.5, 5.5, 8.5], footLGroup, "STEEL_BOLTS");

  makeCube("HIP_JOINT_R", [-10.5, 28.5, -3.0], [-6.5, 33.5, 3.0], legRGroup, "STEEL_DISC_JOINT");
  makeCube("THIGH_CORE_R", [-13, 21, -4.5], [-5, 33, 4.5], legRGroup, "STEEL_PLATES");
  makeCube("THIGH_ARMOR_FRONT_R", [-12.5, 22, -6.0], [-5.5, 32, -4.5], legRGroup, "COPPER_RIVETS");
  makeCube("THIGH_PLATE_SIDE_R", [-14.2, 23, -3.5], [-13.0, 31, 3.5], legRGroup, "COPPER_BAR");

  makeCube("KNEE_ARMOR_R", [-13.5, 19, -7.5], [-4.5, 25, -4.5], calfRGroup, "COPPER_RIVETS");
  makeCube("KNEE_BEVEL_R", [-12.5, 24, -7.0], [-5.5, 27, -4.8], calfRGroup, "COPPER_BAR");
  makeCube("KNEE_DISC_R", [-14.5, 18.5, -2.5], [-13.0, 23.5, 2.5], calfRGroup, "STEEL_DISC_JOINT");

  makeCube("SHIN_CORE_R", [-13, 5, -4.5], [-5, 20, 4.5], calfRGroup, "STEEL_PLATES");
  makeCube("SHIN_GUARD_R", [-12.5, 7, -6.2], [-5.5, 19, -4.5], calfRGroup, "COPPER_RUNE_DIAMOND");
  makeCube("SHIN_PISTON_R", [-10.5, 6, 4.5], [-7.5, 18, 6.2], calfRGroup, "STEEL_VENTS");
  makeCube("ANKLE_COLLAR_R", [-13.5, 5, -5.0], [-4.5, 8, 5.0], calfRGroup, "COPPER_BAR");

  makeCube("FOOT_SOLE_R", [-14, 0, -11], [-4, 2, 7], footRGroup, "COPPER_PATINA");
  makeCube("FOOT_BODY_R", [-13.5, 2, -10], [-4.5, 5, 6], footRGroup, "COPPER_RIVETS");
  makeCube("FOOT_TOE_1_R", [-7.5, 0, -14], [-5.0, 3.5, -11], footRGroup, "COPPER_BAR");
  makeCube("FOOT_TOE_2_R", [-10.5, 0, -15], [-8.0, 4.0, -11], footRGroup, "COPPER_RIVETS");
  makeCube("FOOT_TOE_3_R", [-13.5, 0, -14], [-11.0, 3.5, -11], footRGroup, "COPPER_BAR");
  makeCube("FOOT_HEEL_R", [-11.5, 1, 6], [-6.5, 5.5, 8.5], footRGroup, "STEEL_BOLTS");
""")

    # 6. BLOCKBENCH REFRESH & EMISSIVE MATERIAL
    emit("""
  Cube.all.forEach(c => Canvas.updateUVs(c));
  Canvas.updateAllFaces();
  Canvas.updateAllUVs();

  Texture.all.forEach(t => {
    t.mode = 'bitmap';
    if (!t.canvas && t.img) {
      const c = document.createElement('canvas');
      c.width = t.img.naturalWidth || 64;
      c.height = t.img.naturalHeight || 64;
      const ctx = c.getContext('2d');
      ctx.drawImage(t.img, 0, 0);
      t.canvas = c;
      t.image = c;
    }
    if (t.canvas && t.material) {
      if (t.material.map && t.material.map.dispose) t.material.map.dispose();
      t.material.map = new THREE.CanvasTexture(t.canvas);
      t.material.map.magFilter = THREE.NearestFilter;
      t.material.map.minFilter = THREE.NearestFilter;
      t.material.needsUpdate = true;
    }
  });

  if (atlasTex && emissiveTex && atlasTex.material && emissiveTex.canvas) {
    atlasTex.material.emissiveMap = new THREE.CanvasTexture(emissiveTex.canvas);
    atlasTex.material.emissiveMap.magFilter = THREE.NearestFilter;
    atlasTex.material.emissiveMap.minFilter = THREE.NearestFilter;
    atlasTex.material.emissive = new THREE.Color(0xffffff);
    atlasTex.material.emissiveIntensity = 1.0;
    atlasTex.material.needsUpdate = true;
  }
  Canvas.updateAll();
""")

    # 7. ANIMATIONS (20 FPS)
    emit("""
  const animIdle = new Animation({
    name: "idle",
    loop: "loop",
    length: 3.0,
    snapping: 20
  }).add();

  const bodyIdle = animIdle.getBoneAnimator(bodyGroup);
  bodyIdle.addKeyframe({ channel: 'position', time: 0, data_points: [{ x: 0, y: 0, z: 0 }] });
  bodyIdle.addKeyframe({ channel: 'position', time: 1.5, data_points: [{ x: 0, y: -0.6, z: 0 }] });
  bodyIdle.addKeyframe({ channel: 'position', time: 3.0, data_points: [{ x: 0, y: 0, z: 0 }] });
  bodyIdle.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: 3, y: 0, z: 0 }] });
  bodyIdle.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: 4.5, y: 0, z: 0 }] });
  bodyIdle.addKeyframe({ channel: 'rotation', time: 3.0, data_points: [{ x: 3, y: 0, z: 0 }] });

  const armLIdle = animIdle.getBoneAnimator(armLGroup);
  armLIdle.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -6, y: 0, z: -8 }] });
  armLIdle.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -4, y: 0, z: -6 }] });
  armLIdle.addKeyframe({ channel: 'rotation', time: 3.0, data_points: [{ x: -6, y: 0, z: -8 }] });

  const armRIdle = animIdle.getBoneAnimator(armRGroup);
  armRIdle.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -6, y: 0, z: 8 }] });
  armRIdle.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -4, y: 0, z: 6 }] });
  armRIdle.addKeyframe({ channel: 'rotation', time: 3.0, data_points: [{ x: -6, y: 0, z: 8 }] });

  const animWalk = new Animation({
    name: "walk",
    loop: "loop",
    length: 2.0,
    snapping: 20
  }).add();

  const legLWalk = animWalk.getBoneAnimator(legLGroup);
  legLWalk.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -6, y: -10, z: -4 }] });
  legLWalk.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: -22, y: -10, z: -4 }] });
  legLWalk.addKeyframe({ channel: 'rotation', time: 1.0, data_points: [{ x: -6, y: -10, z: -4 }] });
  legLWalk.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: 10, y: -10, z: -4 }] });
  legLWalk.addKeyframe({ channel: 'rotation', time: 2.0, data_points: [{ x: -6, y: -10, z: -4 }] });

  const legRWalk = animWalk.getBoneAnimator(legRGroup);
  legRWalk.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -6, y: 10, z: 4 }] });
  legRWalk.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: 10, y: 10, z: 4 }] });
  legRWalk.addKeyframe({ channel: 'rotation', time: 1.0, data_points: [{ x: -6, y: 10, z: 4 }] });
  legRWalk.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -22, y: 10, z: 4 }] });
  legRWalk.addKeyframe({ channel: 'rotation', time: 2.0, data_points: [{ x: -6, y: 10, z: 4 }] });

  const animPunch = new Animation({
    name: "attack_punch",
    loop: "once",
    length: 1.0,
    snapping: 20
  }).add();

  const armRPunch = animPunch.getBoneAnimator(armRGroup);
  armRPunch.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -6, y: 0, z: 8 }] });
  armRPunch.addKeyframe({ channel: 'rotation', time: 0.3, data_points: [{ x: -45, y: 15, z: 12 }] });
  armRPunch.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: 35, y: -10, z: -5 }] });
  armRPunch.addKeyframe({ channel: 'rotation', time: 1.0, data_points: [{ x: -6, y: 0, z: 8 }] });

  const forearmRPunch = animPunch.getBoneAnimator(forearmRGroup);
  forearmRPunch.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: 30, y: 10, z: -6 }] });
  forearmRPunch.addKeyframe({ channel: 'rotation', time: 0.3, data_points: [{ x: 65, y: 12, z: -6 }] });
  forearmRPunch.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: 5, y: 0, z: 0 }] });
  forearmRPunch.addKeyframe({ channel: 'rotation', time: 1.0, data_points: [{ x: 30, y: 10, z: -6 }] });

  const animSlam = new Animation({
    name: "attack_slam",
    loop: "once",
    length: 1.5,
    snapping: 20
  }).add();

  const bodySlam = animSlam.getBoneAnimator(bodyGroup);
  bodySlam.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: 3, y: 0, z: 0 }] });
  bodySlam.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: -15, y: 0, z: 0 }] });
  bodySlam.addKeyframe({ channel: 'rotation', time: 0.8, data_points: [{ x: 25, y: 0, z: 0 }] });
  bodySlam.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: 3, y: 0, z: 0 }] });

  const armLSlam = animSlam.getBoneAnimator(armLGroup);
  armLSlam.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -6, y: 0, z: -8 }] });
  armLSlam.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: -75, y: 0, z: -10 }] });
  armLSlam.addKeyframe({ channel: 'rotation', time: 0.8, data_points: [{ x: 45, y: 0, z: 5 }] });
  armLSlam.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -6, y: 0, z: -8 }] });

  const armRSlam = animSlam.getBoneAnimator(armRGroup);
  armRSlam.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -6, y: 0, z: 8 }] });
  armRSlam.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: -75, y: 0, z: 10 }] });
  armRSlam.addKeyframe({ channel: 'rotation', time: 0.8, data_points: [{ x: 45, y: 0, z: -5 }] });
  armRSlam.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -6, y: 0, z: 8 }] });
""")

    emit("  return {")
    emit("    success: true,")
    emit("    cubes: Cube.all.length,")
    emit("    groups: Group.all.length,")
    emit("    animations: Animation.all.map(a => a.name)")
    emit("  };")
    emit("})()")

    return "\n".join(js_lines)

def run():
    script_content = generate_script()
    out_file = Path("scratch/coral_colossus/build_model_v5.js")
    out_file.write_text(script_content, encoding="utf-8")
    print(f"Generated {out_file} ({len(script_content)} bytes)")

    handshake()
    print("Executing in Blockbench...")
    res = call("risky_eval", {"code": script_content})
    print("Build result:", json.dumps(res, ensure_ascii=False)[:300])

    # Position camera and capture views matching all user reference angles
    views = [
        ("ref_34_front", [-65, 55, -95], [0, 36, 0]),
        ("ref_profile", [105, 38, 0], [0, 36, 0]),
        ("ref_front", [0, 38, -105], [0, 36, 0]),
        ("ref_top", [0, 115, -15], [0, 42, 0]),
        ("ref_bottom", [-50, -40, -85], [0, 26, 0])
    ]

    out_dir = Path("scratch/coral_colossus/v5_renders")
    out_dir.mkdir(parents=True, exist_ok=True)

    for name, cam_pos, target_pos in views:
        cam_code = f"""(() => {{
            main_preview.controls.target.set({target_pos[0]}, {target_pos[1]}, {target_pos[2]});
            main_preview.camera.position.set({cam_pos[0]}, {cam_pos[1]}, {cam_pos[2]});
            main_preview.controls.update();
            return true;
        }})()"""
        call("risky_eval", {"code": cam_code})
        time.sleep(0.4)
        snap = call("capture_screenshot", {})
        for item in (snap.get("result") or {}).get("content", []):
            if item.get("type") == "image":
                p = out_dir / f"{name}.png"
                p.write_bytes(base64.b64decode(item.get("data")))
                print(f"Captured {p}")
                break

if __name__ == "__main__":
    run()
