import os
import re
import xml.etree.ElementTree as ET

EN_PATH = 'app/src/main/res/values/strings.xml'
tree_en = ET.parse(EN_PATH)
root_en = tree_en.getroot()

en_strings = {}
for s in root_en.findall('string'):
    name = s.get('name')
    text = s.text or ''
    # extract specifiers like %1$s, %d, %1$.1f
    specifiers = re.findall(r'%(?:\d+\$)?[0-9.]*[a-zA-Z]', text)
    en_strings[name] = {
        'attrib': s.attrib,
        'text': text,
        'specifiers': specifiers
    }

print(f"Loaded {len(en_strings)} English reference strings.")

def verify_lang_file(path):
    if not os.path.exists(path):
        return False, f"File {path} does not exist"
    try:
        tree = ET.parse(path)
        root = tree.getroot()
    except Exception as e:
        return False, f"XML Parse error in {path}: {e}"

    lang_keys = {}
    for s in root.findall('string'):
        name = s.get('name')
        text = s.text or ''
        lang_keys[name] = text

    missing = set(en_strings.keys()) - set(lang_keys.keys())
    if missing:
        return False, f"Missing {len(missing)} keys in {path}: {list(missing)[:5]}..."

    # check format specifiers
    format_mismatches = []
    for name, data in en_strings.items():
        lang_text = lang_keys[name]
        for spec in data['specifiers']:
            if spec not in lang_text:
                format_mismatches.append((name, spec, data['text'], lang_text))

    if format_mismatches:
        return False, f"Format specifier mismatch in {path}: {format_mismatches[:3]}"

    return True, f"OK ({len(lang_keys)} strings verified)"

if __name__ == '__main__':
    import sys
    if len(sys.argv) > 1:
        ok, msg = verify_lang_file(sys.argv[1])
        print(sys.argv[1], ":", msg)
        sys.exit(0 if ok else 1)
    else:
        import glob
        for f in sorted(glob.glob('app/src/main/res/values-*/strings.xml')):
            ok, msg = verify_lang_file(f)
            print(f, ":", msg)
