"""Tiny writer for FTB Quests chapter files (SNBT) used by gen_bee_quests.py and gen_draconic_quests.py.

IDs are derived from the chapter file name and the quest key, so regenerating a chapter keeps its ids stable.
Files are written with CRLF, like the rest of config/ftbquests in this repo.
"""
import glob
import hashlib
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CHAPTER_DIRS = [ROOT / "config" / "ftbquests" / "quests" / "chapters", ROOT / "server" / "config" / "ftbquests" / "quests" / "chapters"]

GROUP_MODS = "4BCD563D0A6CE61C"
GROUP_FINAL = "2AC058F49B3BBFEF"

T = "\t"


def sid(*parts):
    return hashlib.md5(":".join(parts).encode("utf-8")).hexdigest()[:16].upper()


def esc(s):
    return s.replace("\\", "\\\\").replace('"', '\\"')


def item_names():
    """Every item id that has a model in the installed jars (same check the recipe generator uses)."""
    names = set()
    jars = glob.glob(str(ROOT / "server" / "mods" / "*.jar")) + [str(Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar")]
    for jar in jars:
        try:
            z = zipfile.ZipFile(jar)
        except Exception:
            continue
        for n in z.namelist():
            m = re.match(r"assets/([a-z0-9_]+)/models/item/(.+)\.json$", n)
            if m:
                names.add(m.group(1) + ":" + m.group(2))
    return names


class Quest:
    def __init__(self, key, title, icon, x, y, desc, task, deps=(), rewards=()):
        self.key, self.title, self.icon, self.x, self.y = key, title, icon, float(x), float(y)
        self.desc, self.task, self.deps, self.rewards = list(desc), task, list(deps), list(rewards)


def xp(n):
    return ("xp", n)


def item(i, n=1):
    return ("item", i, n)


def coins(n):
    return ("coins", n)


def lines(*parts):
    return "\n".join(parts)


class Chapter:
    def __init__(self, filename, title, icon, group, order_index):
        self.filename, self.title, self.icon, self.group, self.order_index = filename, title, icon, group, order_index
        self.quests = []

    def add(self, *args, **kwargs):
        q = Quest(*args, **kwargs)
        self.quests.append(q)
        return q

    # -- ids ------------------------------------------------------------------------------------------------------
    def qid(self, key):
        return sid(self.filename, "quest", key)

    def resolve(self, dep):
        keys = {q.key for q in self.quests}
        return self.qid(dep) if dep in keys else dep  # raw 16-hex ids point to quests of other chapters

    # -- checks ---------------------------------------------------------------------------------------------------
    def validate(self, names, external_ids):
        problems = []
        keys = [q.key for q in self.quests]
        if len(keys) != len(set(keys)):
            problems.append("duplicate quest keys")
        by_key = {q.key: q for q in self.quests}
        positions = {}
        for q in self.quests:
            if not q.title or not q.desc:
                problems.append(f"{q.key}: empty title/description")
            if (q.x, q.y) in positions:
                problems.append(f"{q.key}: same position as {positions[(q.x, q.y)]}")
            positions[(q.x, q.y)] = q.key
            for d in q.deps:
                if d not in by_key and d not in external_ids:
                    problems.append(f"{q.key}: unknown dependency {d}")
            icons = [q.icon] + ([q.task[1]] if q.task[0] == "item" else []) + [r[1] for r in q.rewards if r[0] == "item"]
            for i in icons:
                if i not in names:
                    problems.append(f"{q.key}: unknown item {i}")
        state = {}

        def visit(k, trail):
            if state.get(k) == 2:
                return
            if state.get(k) == 1:
                problems.append("dependency cycle: " + " -> ".join(trail + [k]))
                return
            state[k] = 1
            for d in by_key[k].deps:
                if d in by_key:
                    visit(d, trail + [k])
            state[k] = 2

        for k in by_key:
            visit(k, [])
        return problems

    # -- output ---------------------------------------------------------------------------------------------------
    def _task(self, q):
        tid = sid(self.filename, "task", q.key)
        if q.task[0] == "item":
            _, item_id, count = q.task
            body = []
            if count > 1:
                body.append(f"{T * 4}count: {count}L")
            body.append(f'{T * 4}id: "{tid}"')
            body.append(f'{T * 4}item: "{item_id}"' if count == 1 else f'{T * 4}item: {{ Count: {count}, id: "{item_id}" }}')
            body.append(f'{T * 4}type: "item"')
            return f"{T * 3}tasks: [{{\n" + "\n".join(body) + f"\n{T * 3}}}]"
        title = q.task[1]
        return (f'{T * 3}tasks: [{{\n{T * 4}id: "{tid}"\n{T * 4}title: "{esc(title)}"\n{T * 4}type: "checkmark"\n{T * 3}}}]')

    def _reward(self, q, index, r):
        rid = sid(self.filename, "reward", q.key, str(index))
        if r[0] == "xp":
            return f'{T * 4}{{\n{T * 5}id: "{rid}"\n{T * 5}type: "xp"\n{T * 5}xp: {r[1]}\n{T * 4}}}'
        if r[0] == "item":
            body = []
            if r[2] > 1:
                body.append(f"{T * 5}count: {r[2]}")
            body += [f'{T * 5}id: "{rid}"', f'{T * 5}item: "{r[1]}"', f'{T * 5}type: "item"']
            return f"{T * 4}{{\n" + "\n".join(body) + f"\n{T * 4}}}"
        n = r[1]
        title = f"{n:,}".replace(",", " ") + " монет"
        body = [
            f'{T * 5}command: "/quest_reward coins {{p}} {n}"',
            f'{T * 5}icon: "minecraft:gold_nugget"',
            f'{T * 5}id: "{rid}"',
            f'{T * 5}title: "{title}"',
            f'{T * 5}type: "command"',
        ]
        return f"{T * 4}{{\n" + "\n".join(body) + f"\n{T * 4}}}"

    def _quest(self, q):
        out = [f"{T * 2}{{"]
        if q.deps:
            deps = [self.resolve(d) for d in q.deps]
            if len(deps) == 1:
                out.append(f'{T * 3}dependencies: ["{deps[0]}"]')
            else:
                out.append(f"{T * 3}dependencies: [\n" + "\n".join(f'{T * 4}"{d}"' for d in deps) + f"\n{T * 3}]")
        out.append(f"{T * 3}description: [\n" + "\n".join(f'{T * 4}"{esc(line)}"' for line in q.desc) + f"\n{T * 3}]")
        out.append(f"{T * 3}entity_vis_size: 1.0f")
        out.append(f'{T * 3}icon: "{q.icon}"')
        out.append(f'{T * 3}id: "{self.qid(q.key)}"')
        if q.rewards:
            out.append(f"{T * 3}rewards: [\n" + "\n".join(self._reward(q, i, r) for i, r in enumerate(q.rewards)) + f"\n{T * 3}]")
        out.append(self._task(q))
        out.append(f'{T * 3}title: "{esc(q.title)}"')
        out.append(f"{T * 3}x: {q.x}d")
        out.append(f"{T * 3}y: {q.y}d")
        out.append(f"{T * 2}}}")
        return "\n".join(out)

    def render(self):
        head = (
            "{\n"
            f"{T}default_hide_dependency_lines: false\n"
            f'{T}default_quest_shape: ""\n'
            f'{T}filename: "{self.filename}"\n'
            f'{T}group: "{self.group}"\n'
            f'{T}icon: "{self.icon}"\n'
            f'{T}id: "{sid(self.filename, "chapter")}"\n'
            f"{T}order_index: {self.order_index}\n"
            f"{T}quest_links: [ ]\n"
            f"{T}quests: [\n"
        )
        body = "\n".join(self._quest(q) for q in self.quests)
        return head + body + f'\n{T}]\n{T}title: "{esc(self.title)}"\n}}\n'

    def write(self):
        text = self.render().replace("\n", "\r\n")
        for d in CHAPTER_DIRS:
            (d / f"{self.filename}.snbt").write_text(text, encoding="utf-8", newline="")
        return text


def existing_quest_ids():
    ids = set()
    d = CHAPTER_DIRS[0]
    for f in d.glob("*.snbt"):
        ids |= set(re.findall(r'\n\t\t\tid: "([0-9A-F]{16})"', f.read_text(encoding="utf-8").replace("\r", "")))
    return ids
