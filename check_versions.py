import zipfile, os

paths = [
    (r'c:\Users\xieto\Desktop\AquaTech\server\mods\casesmod-1.0.0.jar', 'casesmod SERVER'),
    (r'C:\Users\xieto\curseforge\minecraft\Instances\AquaTech\mods\casesmod-1.0.0.jar', 'casesmod CLIENT'),
]

for path, label in paths:
    if not os.path.exists(path):
        print(f'{label}: FILE NOT FOUND')
        continue
    with zipfile.ZipFile(path, 'r') as z:
        for name in z.namelist():
            if 'NetworkHandler' in name and name.endswith('.class'):
                data = z.read(name)
                for ver in [b'7', b'6', b'5', b'4', b'3']:
                    tag = b'"' + ver + b'"'
                    if tag in data:
                        print(f'{label}: PROTOCOL_VERSION = {ver.decode()}')
                        break
                print(f'{label}: jar timestamp = {os.path.getmtime(path):.0f}')
