"""Build Coral Colossus with 1:1 proportions matching actual reference screenshots."""
import json
from pathlib import Path

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

    # Rigging: exact hierarchy with corrected proportions and pivots
    emit("""
  const rootGroup = new Group({ name: "Root", origin: [0, 0, 0] }).init();

  const bodyGroup = new Group({ name: "Body", origin: [0, 30, 0], rotation: [2, 0, 0] }).init();
  bodyGroup.addTo(rootGroup);

  const headGroup = new Group({ name: "Head", origin: [0, 58, 0], rotation: [-1, 0, 0] }).init();
  headGroup.addTo(bodyGroup);

  const armLGroup = new Group({ name: "Arm_L", origin: [19, 48, 0], rotation: [-3, 0, -4] }).init();
  armLGroup.addTo(bodyGroup);

  const forearmLGroup = new Group({ name: "Forearm_L", origin: [19, 36, 0], rotation: [24, -6, 5] }).init();
  forearmLGroup.addTo(armLGroup);

  const armRGroup = new Group({ name: "Arm_R", origin: [-19, 48, 0], rotation: [-3, 0, 4] }).init();
  armRGroup.addTo(bodyGroup);

  const forearmRGroup = new Group({ name: "Forearm_R", origin: [-19, 36, 0], rotation: [24, 6, -5] }).init();
  forearmRGroup.addTo(armRGroup);

  const legLGroup = new Group({ name: "Leg_L", origin: [8, 30, 0], rotation: [-4, 0, -3] }).init();
  legLGroup.addTo(rootGroup);

  const calfLGroup = new Group({ name: "Calf_L", origin: [8, 20, 0], rotation: [8, 0, 2] }).init();
  calfLGroup.addTo(legLGroup);

  const footLGroup = new Group({ name: "Foot_L", origin: [8, 4, 0], rotation: [-4, 0, 1] }).init();
  footLGroup.addTo(calfLGroup);

  const legRGroup = new Group({ name: "Leg_R", origin: [-8, 30, 0], rotation: [-4, 0, 3] }).init();
  legRGroup.addTo(rootGroup);

  const calfRGroup = new Group({ name: "Calf_R", origin: [-8, 20, 0], rotation: [8, 0, -2] }).init();
  calfRGroup.addTo(legRGroup);

  const footRGroup = new Group({ name: "Foot_R", origin: [-8, 4, 0], rotation: [-4, 0, -1] }).init();
  footRGroup.addTo(calfRGroup);
""")

    # 1. HEAD & HELMET
    emit("""
  makeCube("BOTTOM", [-3.5, 56, -3.5], [3.5, 59, 3.5], headGroup, "STEEL_BOLTS");
  makeCube("HEAD_HELMET", [-6, 58, -6], [6, 68, 6], headGroup, "COPPER_PATINA");
  makeCube("HEAD_BROW", [-6.2, 64, -7.2], [6.2, 67, -4.8], headGroup, "COPPER_RIVETS");
  makeCube("HEAD_FACEPLATE", [-5, 60, -7.0], [5, 64, -5.5], headGroup, "COPPER_BAR");
  makeCube("HEAD_FRONT_L", [1.5, 62, -7.3], [4.5, 64, -6.8], headGroup, "GEM_CYAN");
  makeCube("HEAD_FRONT_R", [-4.5, 62, -7.3], [-1.5, 64, -6.8], headGroup, "GEM_CYAN");
  makeCube("HEAD_NOSE_BRIDGE", [-1.5, 60.5, -7.2], [1.5, 64.5, -6.8], headGroup, "COPPER_BAR");
  makeCube("HEAD_CHIN", [-4, 58, -7.0], [4, 61, -5.5], headGroup, "COPPER_RIVETS");
  makeCube("HEAD_CREST", [-1.8, 68, -5.5], [1.8, 71.5, 4.5], headGroup, "COPPER_RIVETS");
  makeCube("HEAD_TOP", [-5.8, 67.5, -5.8], [5.8, 69.0, 5.8], headGroup, "COPPER_PATINA");
  makeCube("HEAD_SIDED_L", [5.8, 59, -6], [7.0, 65, 2], headGroup, "COPPER_BAR");
  makeCube("HEAD_SIDED_R", [-7.0, 59, -6], [-5.8, 65, 2], headGroup, "COPPER_BAR");

  makeCube("CORALS_HEAD_RED_1", [1, 68, -2], [5, 77, 2], headGroup, "CORAL_RED");
  makeCube("CORALS_HEAD_RED_TIP", [2, 76, -1], [6, 81, 2.5], headGroup, "CORAL_RED");
  makeCube("CORALS_HEAD_YEL_1", [-5, 68, -2], [-1, 77, 2], headGroup, "CORAL_YELLOW");
  makeCube("CORALS_HEAD_YEL_TIP", [-6, 76, -1], [-2, 81, 2.5], headGroup, "CORAL_YELLOW");
""")

    # 2. TORSO, CHEST PECTORALS, ABDOMEN & BACK
    emit("""
  makeCube("PELVIS_BASE", [-8, 29, -5.5], [8, 37, 5.5], bodyGroup, "COPPER_PATINA");
  makeCube("PELVIS_BELT_RIM", [-6.5, 30.5, -6.8], [6.5, 36.5, -5.5], bodyGroup, "COPPER_RIVETS");
  makeCube("PELVIS_GROIN_CHEVRON", [-3.5, 24, -6.8], [3.5, 31.5, -5.5], bodyGroup, "COPPER_RUNE_DIAMOND");
  makeCube("PELVIS_HIP_SOCKET_L", [7.0, 29.5, -3.0], [9.5, 34.5, 3.0], bodyGroup, "STEEL_DISC_JOINT");
  makeCube("PELVIS_HIP_SOCKET_R", [-9.5, 29.5, -3.0], [-7.0, 34.5, 3.0], bodyGroup, "STEEL_DISC_JOINT");

  makeCube("TORSO_WAIST", [-9, 36, -6.0], [9, 42.5, 6.0], bodyGroup, "PRISMARINE_WAIST");
  makeCube("TORSO_PISTON_L", [6.5, 36, -6.8], [9.5, 42.5, -4.8], bodyGroup, "STEEL_PLATES");
  makeCube("TORSO_PISTON_R", [-9.5, 36, -6.8], [-6.5, 42.5, -4.8], bodyGroup, "STEEL_PLATES");

  makeCube("TORSO_CORE_COLLAR", [-5.5, 36.5, -7.8], [5.5, 44.5, -6.5], bodyGroup, "STEEL_BOLTS");
  makeCube("TORSO_CORE", [-4.5, 37.5, -8.4], [4.5, 43.5, -7.5], bodyGroup, "DEEPSLATE_MEDALLION");

  makeCube("TORSO_TORSO", [-13.5, 42, -7.5], [13.5, 57, 7.5], bodyGroup, "COPPER_PATINA");
  makeCube("CHEST_PLATE_L", [5, 44, -9.2], [13.5, 56, -7.2], bodyGroup, "COPPER_RIVETS");
  makeCube("CHEST_BOLT_INSET_L", [7, 47, -9.5], [11.5, 53, -9.1], bodyGroup, "STEEL_BOLTS");
  makeCube("CHEST_PLATE_R", [-13.5, 44, -9.2], [-5, 56, -7.2], bodyGroup, "COPPER_RIVETS");
  makeCube("CHEST_BOLT_INSET_R", [-11.5, 47, -9.5], [-7, 53, -9.1], bodyGroup, "STEEL_BOLTS");
  makeCube("CHEST_LOWER_CHEVRON", [-5, 43, -8.6], [5, 46.5, -7.2], bodyGroup, "COPPER_BAR");
  makeCube("CHEST_COLLAR_PLATE", [-8.5, 56, -9.0], [8.5, 59, -7.2], bodyGroup, "COPPER_RIVETS");

  makeCube("BACK_ARMOR", [-11.5, 42, 7.5], [11.5, 57, 10.5], bodyGroup, "COPPER_PATINA");
  makeCube("BACK_REACTOR_L", [3.5, 48, 10.2], [9.5, 54, 11.6], bodyGroup, "STEEL_BOLTS", { south: { uv: "GEM_CYAN" } });
  makeCube("BACK_REACTOR_R", [-9.5, 48, 10.2], [-3.5, 54, 11.6], bodyGroup, "STEEL_BOLTS", { south: { uv: "GEM_CYAN" } });
  makeCube("SPINE_CORE", [-2.5, 34, 7.5], [2.5, 46, 8.8], bodyGroup, "STEEL_VENTS");

  makeCube("CORALS_BACK_RED_MAIN", [-3.5, 57, 8.5], [3.5, 76, 12.5], bodyGroup, "CORAL_RED");
  makeCube("CORALS_BACK_RED_L", [2, 60, 9], [7, 72, 12], bodyGroup, "CORAL_RED");
  makeCube("CORALS_BACK_RED_R", [-7, 60, 9], [-2, 72, 12], bodyGroup, "CORAL_RED");
  makeCube("CORALS_BACK_YEL_L", [5, 62, 8], [11, 75, 12.5], bodyGroup, "CORAL_YELLOW");
  makeCube("CORALS_BACK_YEL_R", [-11, 62, 8], [-5, 75, 12.5], bodyGroup, "CORAL_YELLOW");
  makeCube("CORALS_BACK_TOP_SPIRE", [-2, 75, 9.5], [2, 82, 13], bodyGroup, "CORAL_RED");
""")

    # 3. LEFT ARM & COMPACT PROPORTIONED CANNON GAUNTLET (NO FINGERS!)
    emit("""
  makeCube("SHOULDERS_L", [14, 48, -7.5], [27, 59, 7.5], armLGroup, "COPPER_PATINA");
  makeCube("SHOULDER_RIM_L", [26.5, 50, -6.5], [28.2, 57, 6.5], armLGroup, "COPPER_RIVETS");
  makeCube("SHOULDER_RIDGE_L", [15, 58.5, -6.5], [26, 60.5, 6.5], armLGroup, "COPPER_RIVETS");
  makeCube("LEFT_ARMS", [16, 38, -4], [23.5, 49, 4], armLGroup, "STEEL_PLATES");
  makeCube("BICEP_PLATE_L", [15.5, 39, -5.5], [23.8, 48, -3.8], armLGroup, "COPPER_BAR");
  makeCube("ELBOW_L", [15.5, 33, -4.5], [24.5, 38, 4.5], armLGroup, "STEEL_DISC_JOINT");

  makeCube("CORALS_SHOULDER_YEL_L", [17, 59, -4], [22, 69, 1], armLGroup, "CORAL_YELLOW");
  makeCube("CORALS_SHOULDER_YEL_TIP_L", [18, 68, -3], [23, 73, 2], armLGroup, "CORAL_YELLOW");
  makeCube("CORALS_SHOULDER_RED_L", [20, 59, 0], [25, 67, 5.5], armLGroup, "CORAL_RED");
  makeCube("CORALS_SHOULDER_RED_TIP_L", [21, 66, 1], [26, 72, 6], armLGroup, "CORAL_RED");

  makeCube("ARM_FOREARM_L", [14, 21, -7.5], [25.5, 36, 5.5], forearmLGroup, "COPPER_PATINA");
  makeCube("FOREARM_CUFF_L", [13.5, 33.5, -8.0], [26.0, 36.5, 6.0], forearmLGroup, "COPPER_RIVETS");
  makeCube("CANNON_BEZEL_L", [14.5, 21.5, -9.0], [25.0, 31, -7.2], forearmLGroup, "COPPER_RIVETS");
  makeCube("CANNON_COLLAR_L", [16.0, 23.0, -9.5], [23.5, 29.5, -8.8], forearmLGroup, "STEEL_BOLTS");
  makeCube("CANNON_DETAILS_L_CORE", [17.0, 24.0, -9.8], [22.5, 28.5, -8.8], forearmLGroup, "CORE_CYAN");
  makeCube("SHIELD_DETAILS_L", [25.2, 23, -5.5], [26.8, 33, 3.5], forearmLGroup, "COPPER_RUNE_DIAMOND");
  makeCube("SHIELD_GEM_L", [26.5, 26, -2], [27.3, 30, 0], forearmLGroup, "GEM_CYAN");

  makeCube("CORALS_FOREARM_RED_L", [18, 34, 2], [24, 43, 6.5], forearmLGroup, "CORAL_RED");
  makeCube("CORALS_FOREARM_YEL_L", [21, 31, 3], [25.5, 39, 7.5], forearmLGroup, "CORAL_YELLOW");
""")

    # 4. RIGHT ARM & COMPACT PROPORTIONED CANNON GAUNTLET (NO FINGERS!)
    emit("""
  makeCube("SHOULDERS_R", [-27, 48, -7.5], [-14, 59, 7.5], armRGroup, "COPPER_PATINA");
  makeCube("SHOULDER_RIM_R", [-28.2, 50, -6.5], [-26.5, 57, 6.5], armRGroup, "COPPER_RIVETS");
  makeCube("SHOULDER_RIDGE_R", [-26, 58.5, -6.5], [-15, 60.5, 6.5], armRGroup, "COPPER_RIVETS");
  makeCube("RIGHT_ARMS", [-23.5, 38, -4], [-16, 49, 4], armRGroup, "STEEL_PLATES");
  makeCube("BICEP_PLATE_R", [-23.8, 39, -5.5], [-15.5, 48, -3.8], armRGroup, "COPPER_BAR");
  makeCube("ELBOW_R", [-24.5, 33, -4.5], [-15.5, 38, 4.5], armRGroup, "STEEL_DISC_JOINT");

  makeCube("CORALS_SHOULDER_YEL_R", [-22, 59, -4], [-17, 69, 1], armRGroup, "CORAL_YELLOW");
  makeCube("CORALS_SHOULDER_YEL_TIP_R", [-23, 68, -3], [-18, 73, 2], armRGroup, "CORAL_YELLOW");
  makeCube("CORALS_SHOULDER_RED_R", [-25, 59, 0], [-20, 67, 5.5], armRGroup, "CORAL_RED");
  makeCube("CORALS_SHOULDER_RED_TIP_R", [-26, 66, 1], [-21, 72, 6], armRGroup, "CORAL_RED");

  makeCube("ARM_FOREARM_R", [-25.5, 21, -7.5], [-14, 36, 5.5], forearmRGroup, "COPPER_PATINA");
  makeCube("FOREARM_CUFF_R", [-26.0, 33.5, -8.0], [-13.5, 36.5, 6.0], forearmRGroup, "COPPER_RIVETS");
  makeCube("CANNON_BEZEL_R", [-25.0, 21.5, -9.0], [-14.5, 31, -7.2], forearmRGroup, "COPPER_RIVETS");
  makeCube("CANNON_COLLAR_R", [-23.5, 23.0, -9.5], [-16.0, 29.5, -8.8], forearmRGroup, "STEEL_BOLTS");
  makeCube("CANNON_DETAILS_R_CORE", [-22.5, 24.0, -9.8], [-17.0, 28.5, -8.8], forearmRGroup, "CORE_CYAN");
  makeCube("SHIELD_DETAILS_R", [-26.8, 23, -5.5], [-25.2, 33, 3.5], forearmRGroup, "COPPER_RUNE_DIAMOND");
  makeCube("SHIELD_GEM_R", [-27.3, 26, -2], [-26.5, 30, 0], forearmRGroup, "GEM_CYAN");

  makeCube("CORALS_FOREARM_RED_R", [-24, 34, 2], [-18, 43, 6.5], forearmRGroup, "CORAL_RED");
  makeCube("CORALS_FOREARM_YEL_R", [-25.5, 31, 3], [-21, 39, 7.5], forearmRGroup, "CORAL_YELLOW");
""")

    # 5. TALL & POWERFUL LEGS (45% OF TOTAL HEIGHT, DOMINATING SILHOUETTE!)
    emit("""
  makeCube("HIP_JOINT_L", [5.5, 28.5, -3.0], [9.5, 33.5, 3.0], legLGroup, "STEEL_DISC_JOINT");
  makeCube("LEFT_LEG_LEGS", [4, 21, -4.5], [12, 33, 4.5], legLGroup, "STEEL_PLATES");
  makeCube("THIGH_ARMOR_L", [4.5, 22, -6.0], [11.5, 32, -4.5], legLGroup, "COPPER_RIVETS");
  makeCube("THIGH_PLATE_OUTER_L", [12.0, 23, -3.5], [13.2, 31, 3.5], legLGroup, "COPPER_BAR");

  makeCube("KNEE_LEGS_L", [3.5, 19, -7.5], [12.5, 25, -4.5], calfLGroup, "COPPER_RIVETS");
  makeCube("KNEE_BEVEL_L", [4.5, 24, -7.0], [11.5, 27, -4.8], calfLGroup, "COPPER_BAR");
  makeCube("KNEE_DISC_L", [12.0, 18, -2.5], [13.5, 23, 2.5], calfLGroup, "STEEL_DISC_JOINT", { east: { uv: "STEEL_DISC_JOINT" } });

  makeCube("SHIN_L", [4, 5, -4.5], [12, 20, 4.5], calfLGroup, "STEEL_PLATES");
  makeCube("SHIN_GUARD_L", [4.5, 7, -6.2], [11.5, 19, -4.5], calfLGroup, "COPPER_RUNE_DIAMOND");
  makeCube("SHIN_PISTON_L", [6.5, 6, 4.5], [9.5, 18, 6.2], calfLGroup, "STEEL_VENTS");
  makeCube("ANKLE_COLLAR_L", [3.5, 5, -5.0], [12.5, 8, 5.0], calfLGroup, "COPPER_BAR");

  makeCube("LEFT_LEG_FOOT", [3, 0, -11], [13, 2, 7], footLGroup, "COPPER_PATINA");
  makeCube("FOOT_BODY_L", [3.5, 2, -10], [12.5, 5, 6], footLGroup, "COPPER_RIVETS");
  makeCube("FOOT_TOE_1_L", [4.0, 0, -14], [6.5, 3.5, -11], footLGroup, "COPPER_BAR");
  makeCube("FOOT_TOE_2_L", [7.0, 0, -15], [9.5, 4.0, -11], footLGroup, "COPPER_RIVETS");
  makeCube("FOOT_TOE_3_L", [10.0, 0, -14], [12.5, 3.5, -11], footLGroup, "COPPER_BAR");
  makeCube("FOOT_HEEL_L", [5.5, 1, 6], [10.5, 5.5, 8.5], footLGroup, "STEEL_BOLTS");

  makeCube("HIP_JOINT_R", [-9.5, 28.5, -3.0], [-5.5, 33.5, 3.0], legRGroup, "STEEL_DISC_JOINT");
  makeCube("RIGHT_LEG_LEGS", [-12, 21, -4.5], [-4, 33, 4.5], legRGroup, "STEEL_PLATES");
  makeCube("THIGH_ARMOR_R", [-11.5, 22, -6.0], [-4.5, 32, -4.5], legRGroup, "COPPER_RIVETS");
  makeCube("THIGH_PLATE_OUTER_R", [-13.2, 23, -3.5], [-12.0, 31, 3.5], legRGroup, "COPPER_BAR");

  makeCube("KNEE_LEGS_R", [-12.5, 19, -7.5], [-3.5, 25, -4.5], calfRGroup, "COPPER_RIVETS");
  makeCube("KNEE_BEVEL_R", [-11.5, 24, -7.0], [-4.5, 27, -4.8], calfRGroup, "COPPER_BAR");
  makeCube("KNEE_DISC_R", [-13.5, 18, -2.5], [-12.0, 23, 2.5], calfRGroup, "STEEL_DISC_JOINT", { west: { uv: "STEEL_DISC_JOINT" } });

  makeCube("SHIN_R", [-12, 5, -4.5], [-4, 20, 4.5], calfRGroup, "STEEL_PLATES");
  makeCube("SHIN_GUARD_R", [-11.5, 7, -6.2], [-4.5, 19, -4.5], calfRGroup, "COPPER_RUNE_DIAMOND");
  makeCube("SHIN_PISTON_R", [-9.5, 6, 4.5], [-6.5, 18, 6.2], calfRGroup, "STEEL_VENTS");
  makeCube("ANKLE_COLLAR_R", [-12.5, 5, -5.0], [-3.5, 8, 5.0], calfRGroup, "COPPER_BAR");

  makeCube("RIGHT_LEG_FOOT", [-13, 0, -11], [-3, 2, 7], footRGroup, "COPPER_PATINA");
  makeCube("FOOT_BODY_R", [-12.5, 2, -10], [-3.5, 5, 6], footRGroup, "COPPER_RIVETS");
  makeCube("FOOT_TOE_1_R", [-6.5, 0, -14], [-4.0, 3.5, -11], footRGroup, "COPPER_BAR");
  makeCube("FOOT_TOE_2_R", [-9.5, 0, -15], [-7.0, 4.0, -11], footRGroup, "COPPER_RIVETS");
  makeCube("FOOT_TOE_3_R", [-12.5, 0, -14], [-10.0, 3.5, -11], footRGroup, "COPPER_BAR");
  makeCube("FOOT_HEEL_R", [-10.5, 1, 6], [-5.5, 5.5, 8.5], footRGroup, "STEEL_BOLTS");
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
  bodyIdle.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: 2, y: 0, z: 0 }] });
  bodyIdle.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: 3.5, y: 0, z: 0 }] });
  bodyIdle.addKeyframe({ channel: 'rotation', time: 3.0, data_points: [{ x: 2, y: 0, z: 0 }] });

  const armLIdle = animIdle.getBoneAnimator(armLGroup);
  armLIdle.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -3, y: 0, z: -4 }] });
  armLIdle.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -1, y: 0, z: -2 }] });
  armLIdle.addKeyframe({ channel: 'rotation', time: 3.0, data_points: [{ x: -3, y: 0, z: -4 }] });

  const armRIdle = animIdle.getBoneAnimator(armRGroup);
  armRIdle.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -3, y: 0, z: 4 }] });
  armRIdle.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -1, y: 0, z: 2 }] });
  armRIdle.addKeyframe({ channel: 'rotation', time: 3.0, data_points: [{ x: -3, y: 0, z: 4 }] });

  const animWalk = new Animation({
    name: "walk",
    loop: "loop",
    length: 2.0,
    snapping: 20
  }).add();

  const legLWalk = animWalk.getBoneAnimator(legLGroup);
  legLWalk.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -4, y: 0, z: -3 }] });
  legLWalk.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: -18, y: 0, z: -3 }] });
  legLWalk.addKeyframe({ channel: 'rotation', time: 1.0, data_points: [{ x: -4, y: 0, z: -3 }] });
  legLWalk.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: 12, y: 0, z: -3 }] });
  legLWalk.addKeyframe({ channel: 'rotation', time: 2.0, data_points: [{ x: -4, y: 0, z: -3 }] });

  const legRWalk = animWalk.getBoneAnimator(legRGroup);
  legRWalk.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -4, y: 0, z: 3 }] });
  legRWalk.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: 12, y: 0, z: 3 }] });
  legRWalk.addKeyframe({ channel: 'rotation', time: 1.0, data_points: [{ x: -4, y: 0, z: 3 }] });
  legRWalk.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -18, y: 0, z: 3 }] });
  legRWalk.addKeyframe({ channel: 'rotation', time: 2.0, data_points: [{ x: -4, y: 0, z: 3 }] });

  const animPunch = new Animation({
    name: "attack_punch",
    loop: "once",
    length: 1.0,
    snapping: 20
  }).add();

  const armRPunch = animPunch.getBoneAnimator(armRGroup);
  armRPunch.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -3, y: 0, z: 4 }] });
  armRPunch.addKeyframe({ channel: 'rotation', time: 0.3, data_points: [{ x: -45, y: 15, z: 10 }] });
  armRPunch.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: 35, y: -10, z: -5 }] });
  armRPunch.addKeyframe({ channel: 'rotation', time: 1.0, data_points: [{ x: -3, y: 0, z: 4 }] });

  const forearmRPunch = animPunch.getBoneAnimator(forearmRGroup);
  forearmRPunch.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: 24, y: 6, z: -5 }] });
  forearmRPunch.addKeyframe({ channel: 'rotation', time: 0.3, data_points: [{ x: 60, y: 10, z: -5 }] });
  forearmRPunch.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: 5, y: 0, z: 0 }] });
  forearmRPunch.addKeyframe({ channel: 'rotation', time: 1.0, data_points: [{ x: 24, y: 6, z: -5 }] });

  const animSlam = new Animation({
    name: "attack_slam",
    loop: "once",
    length: 1.5,
    snapping: 20
  }).add();

  const bodySlam = animSlam.getBoneAnimator(bodyGroup);
  bodySlam.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: 2, y: 0, z: 0 }] });
  bodySlam.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: -15, y: 0, z: 0 }] });
  bodySlam.addKeyframe({ channel: 'rotation', time: 0.8, data_points: [{ x: 25, y: 0, z: 0 }] });
  bodySlam.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: 2, y: 0, z: 0 }] });

  const armLSlam = animSlam.getBoneAnimator(armLGroup);
  armLSlam.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -3, y: 0, z: -4 }] });
  armLSlam.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: -75, y: 0, z: -10 }] });
  armLSlam.addKeyframe({ channel: 'rotation', time: 0.8, data_points: [{ x: 45, y: 0, z: 5 }] });
  armLSlam.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -3, y: 0, z: -4 }] });

  const armRSlam = animSlam.getBoneAnimator(armRGroup);
  armRSlam.addKeyframe({ channel: 'rotation', time: 0, data_points: [{ x: -3, y: 0, z: 4 }] });
  armRSlam.addKeyframe({ channel: 'rotation', time: 0.5, data_points: [{ x: -75, y: 0, z: 10 }] });
  armRSlam.addKeyframe({ channel: 'rotation', time: 0.8, data_points: [{ x: 45, y: 0, z: -5 }] });
  armRSlam.addKeyframe({ channel: 'rotation', time: 1.5, data_points: [{ x: -3, y: 0, z: 4 }] });
""")

    emit("  return {")
    emit("    success: true,")
    emit("    cubes: Cube.all.length,")
    emit("    groups: Group.all.length,")
    emit("    animations: Animation.all.map(a => a.name)")
    emit("  };")
    emit("})()")

    return "\n".join(js_lines)

if __name__ == "__main__":
    script_content = generate_script()
    out_file = Path("scratch/coral_colossus/build_model.js")
    out_file.write_text(script_content, encoding="utf-8")
    print(f"Generated {out_file} ({len(script_content)} bytes)")
