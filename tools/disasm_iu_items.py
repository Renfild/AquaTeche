import zipfile, subprocess, tempfile, os

jar_path = 'c:/Users/xieto/Desktop/AquaTech/mods/IndustrialUpgrade-1.20.1-3.4.0.11.jar'
with tempfile.TemporaryDirectory() as tmp:
    with zipfile.ZipFile(jar_path, 'r') as z:
        for name in ['com/denfop/IUItem.class', 'com/denfop/recipes/ItemStackHelper.class', 'com/denfop/dataregistry/DataItem.class', 'com/denfop/dataregistry/DataBlock.class', 'com/denfop/blocks/mechanism/BlockBaseMachine3Entity.class', 'com/denfop/blocks/mechanism/BlockBaseMachineEntity.class', 'com/denfop/blocks/mechanism/BlockBaseMachine1Entity.class', 'com/denfop/blocks/mechanism/BlockBaseMachine2Entity.class', 'com/denfop/blocks/mechanism/BlockBaseMachine4Entity.class', 'com/denfop/blocks/mechanism/BlockBaseMachine5Entity.class']:
            if name in z.namelist():
                z.extract(name, tmp)
                cls_path = os.path.join(tmp, name)
                res = subprocess.run(['javap', '-p', '-c', cls_path], capture_output=True, text=True, encoding='utf-8', errors='ignore')
                out_name = os.path.basename(name).replace('.class', '.txt')
                with open(out_name, 'w', encoding='utf-8') as out:
                    out.write(res.stdout)
                print(f"Disassembled {name} -> {out_name}")
