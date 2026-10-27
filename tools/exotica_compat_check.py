"""Checks every Exotica Technologies method the compiled mod calls (directly, or through methods it
inherits from Exotica), every Exotica method it overrides, and every abstract one it must implement,
against several Exotica jars. Players run different Exotica versions, and a call missing from one of
them crashes the game (OCE 1.1.0 with Exotica 2.0.x).

    python tools/exotica_compat_check.py <javap> build/classes/java/main <Exotica-Technologies.jar>...

Get the jars from https://github.com/RkShaRkz/ExoticaTechnologies/releases (2.0.2, 2.0.3, 2.1.0)
plus any newer copy, into a scratch folder outside the repository. The game and LazyLib paths below
are this machine's.
"""
import re, subprocess, sys, os
javap, classes, jars = sys.argv[1], sys.argv[2], sys.argv[3:]
coredir = r'D:/Games/StarSector/starsector-core'
core = [os.path.join(coredir, f) for f in os.listdir(coredir) if f.endswith('.jar')]
kt = r'D:/Games/StarSector/mods/LazyLib-3.0.0/jars/internal/Kotlin-Runtime.jar'

def jp(cp, *args):
    return subprocess.run([javap, '-cp', cp] + list(args), capture_output=True, text=True).stdout

def parse(out, cls):
    """{(name, descriptor): abstract?} and superclass, from javap -s -p output. Private methods go
    into PRIVATE as well: they are helpers, never overrides."""
    simple = cls.split('.')[-1]
    res = {}; parent = None; cur = None; curabs = False
    hm = re.search(r'(?:class|interface) \S+ extends (\S+?)(?: implements|\s*\{)', out)
    if hm: parent = hm.group(1)
    for line in out.splitlines():
        dm = re.match(r'\s+descriptor: (\S+)', line)
        if dm:
            if cur: res[(cur, dm.group(1))] = curabs
            cur = None; continue
        mm = re.match(r'\s+[^(]*?([\w$]+)\((.*)\)( throws .*)?;$', line)
        if mm:
            cur = mm.group(1)
            if line.strip().startswith('private '): PRIVATE.add((cls, cur))
            if cur == simple: cur = '<init>'
            curabs = ' abstract ' in ' ' + line + ' '
            continue
        fm = re.match(r'\s+[^(=]*?([\w$]+);$', line)
        if fm: cur = fm.group(1); curabs = False
    return res, parent

PRIVATE = set()
mine = []
for root, _, files in os.walk(classes):
    for f in files:
        if f.endswith('.class'):
            mine.append(os.path.relpath(os.path.join(root, f), classes)[:-6].replace(os.sep, '.'))

own, sup, refs = {}, {}, set()
for c in mine:
    res, parent = parse(jp(classes, '-s', '-p', c), c)
    own[c] = res; sup[c] = parent
    out = jp(classes, '-v', '-p', c)
    for m in re.finditer(r'(?:Methodref|InterfaceMethodref|Fieldref)\s+#\S+\s+// ([\w/$]+)\.("?[\w<>$]+"?):(\S+)', out):
        refs.add((m.group(1).replace('/', '.'), m.group(2).strip('"'), m.group(3)))

cache = {}
def members(cp, cls):
    """Everything cls declares or inherits from Exotica classes: {(name, desc): abstract?}."""
    if (cp, cls) in cache: return cache[(cp, cls)]
    out = jp(cp, '-s', '-p', cls)
    if 'Compiled from' not in out and 'class ' not in out: cache[(cp, cls)] = None; return None
    res, parent = parse(out, cls)
    if parent and parent.startswith('exoticatechnologies.'):
        inherited = members(cp, parent) or {}
        for k, a in inherited.items():
            if k not in res: res[k] = a
    cache[(cp, cls)] = res
    return res

def exotica_owner(cls):
    """The first Exotica class above one of ours, or the class itself if it is Exotica's."""
    while cls in own:
        cls = sup[cls]
        if cls is None: return None
    return cls if cls and cls.startswith('exoticatechnologies.') else None

bad = 0
for jar in jars:
    cp = ';'.join([jar, kt] + core)
    label = os.path.basename(os.path.dirname(os.path.dirname(jar))) or jar
    problems, checked = [], 0
    for owner, name, desc in sorted(refs):
        if owner in own:
            if (name, desc) in own[owner]: continue
            target = exotica_owner(owner)
        elif owner.startswith('exoticatechnologies.'):
            target = owner
        else:
            continue
        if target is None: continue
        checked += 1
        res = members(cp, target)
        if res is None: problems.append(f'missing class {target}')
        elif (name, desc) not in res: problems.append(f'missing {target}.{name}{desc} (called from {owner})')
    for c in mine:
        base = exotica_owner(c) if sup[c] and sup[c].startswith('exoticatechnologies.') else None
        if not base: continue
        res = members(cp, base)
        if res is None: problems.append(f'missing superclass {base}'); continue
        for (name, desc) in sorted(own[c]):
            if name.startswith('lambda$') or name == '<clinit>' or not desc.startswith('('): continue
            if (c, name) in PRIVATE: continue
            checked += 1
            if (name, desc) not in res and name != '<init>':
                problems.append(f'{c}.{name}{desc} overrides nothing in {base}')
        for (name, desc), abstract in sorted(res.items()):
            if abstract and (name, desc) not in own[c] and name not in ('getIcon', 'setIcon'):
                problems.append(f'{c} does not implement abstract {name}{desc}')
    print(f'== {label}: {checked} checks, ' + ('OK' if not problems else f'{len(problems)} problems'))
    for p in problems: print('   ' + p)
    bad += len(problems)
sys.exit(1 if bad else 0)
