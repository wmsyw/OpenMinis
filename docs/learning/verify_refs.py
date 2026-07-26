#!/usr/bin/env python3
"""Verify every repo path cited in docs/learning/parts/*.md actually exists.

Scans inline-code spans for repository-path-looking strings, strips an
optional `:LINE` / `:START-END` selector, and checks the working tree.

Three classes are tolerated and reported separately rather than failed:

  * ABBREV   — `src/android/.../foo.kt` elision of the long Java package
               path. Resolved by globbing; only fails if the glob is empty
               or ambiguous.
  * ARTIFACT — build outputs that only exist after `deps/*.sh` runs
               (deps/libs, deps/frameworks, generated assets, …). These are
               cited on purpose; their absence in a clean tree is correct.
  * GENERIC  — filenames that name a convention, not a repo file
               (SKILL.md, DECISIONS.md).

Anything else that does not exist is a real error.

Usage: python3 docs/learning/verify_refs.py
Exit 1 if any cited path is a real error.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
PARTS = ROOT / "docs" / "learning" / "parts"

CODE = re.compile(r"`([^`\n]+)`")
PATHY = re.compile(
    r"^(?:src|deps|scripts|docs|assets)/[\w./+-]+$"
    r"|^[A-Z][A-Za-z_]*\.md$"
)
SELECTOR = re.compile(r":\d+(?:-\d+)?$")
ELIDED = re.compile(r"/\.\.\./")
# `Foo.swift.someSymbol` — a symbol appended to a real source path.
SYMBOL_SUFFIX = re.compile(r"^(?P<file>.+\.(?:swift|kt|m|h|c|cpp|py|sh|json))\.\w+$")

# Build outputs produced by deps/*.sh and scripts/*.sh — absent in a clean
# checkout by design (BUILDING.md:3-8 explains why they are not committed).
ARTIFACT_PREFIXES = (
    "deps/libs",
    "deps/include",
    "deps/resources",
    "deps/frameworks",
    "deps/lame-build",
    "deps/ffmpeg-",
    "deps/alpine",
    "deps/.cache",
    "deps/ish/build",
    "deps/proot/src",
    "assets/proot-",
    "src/android/app/src/main/assets/alpine-minirootfs",
    "src/android/app/src/main/assets/bashism",
    "src/ios/Generated/",
)
# Bare relative fragments that only make sense inside a module context.
ARTIFACT_EXACT = {
    "src/main/assets",
    "src/debug/assets",
    "src/main/cpp/",
    "src/main/cpp",
    "src/androidTest",
    "src/test",
    "assets/default_mount",
}
# Filenames that name a runtime convention, not a file in this repo.
# SOUL.md / GLOBAL.md / SKILL.md live in per-session sandbox storage at
# runtime; scripts/*.py under a skill folder are illustrative examples.
GENERIC = {
    "SKILL.md", "SOUL.md", "GLOBAL.md", "AGENTS.md", "CLAUDE.md",
    "DECISIONS.md", "README.md", "CONTRIBUTING.md",
    "scripts/report.py", "scripts/foo.py",
}
def classify(bare: str):
    """Return (status, detail). status in ok|abbrev|artifact|generic|MISSING."""
    if bare in GENERIC and not (ROOT / bare).exists():
        return "generic", ""
    if ELIDED.search(bare):
        head, tail = bare.split("/.../", 1)
        matches = sorted(ROOT.glob(f"{head}/**/{tail.rstrip('/')}"))
        if len(matches) == 1:
            return "abbrev", str(matches[0].relative_to(ROOT))
        if len(matches) > 1:
            return "abbrev", f"{len(matches)} matches"
        return "MISSING", "elided path resolves to nothing"
    if (ROOT / bare.rstrip("/")).exists():
        return "ok", ""
    sym = SYMBOL_SUFFIX.match(bare)
    if sym and (ROOT / sym.group("file")).exists():
        return "ok", ""
    if bare.rstrip("/") in ARTIFACT_EXACT or bare.startswith(ARTIFACT_PREFIXES):
        return "artifact", ""
    return "MISSING", ""


def main() -> int:
    if not PARTS.is_dir():
        print(f"missing {PARTS}", file=sys.stderr)
        return 1

    errors: list[tuple[str, int, str, str]] = []
    totals = {"ok": 0, "abbrev": 0, "artifact": 0, "generic": 0, "MISSING": 0}

    hdr = f"{'chapter':<42} {'lines':>6} {'refs':>5} {'ok':>4} {'abbr':>5} {'art':>4} {'bad':>4} {'mmd':>4}"
    print(hdr)
    print("-" * len(hdr))

    for md in sorted(PARTS.glob("*.md")):
        text = md.read_text(encoding="utf-8")
        per = {"ok": 0, "abbrev": 0, "artifact": 0, "generic": 0, "MISSING": 0}
        for lineno, line in enumerate(text.splitlines(), 1):
            for span in CODE.findall(line):
                raw = span.strip()
                bare = SELECTOR.sub("", raw)
                if not PATHY.match(bare.replace("/.../", "/x/")):
                    continue
                status, detail = classify(bare)
                per[status] += 1
                totals[status] += 1
                if status == "MISSING":
                    errors.append((md.name, lineno, raw, detail))
        nrefs = sum(per.values())
        fences = len(re.findall(r"^```mermaid\s*$", text, re.M))
        nlines = len(text.splitlines())
        print(
            f"{md.name:<42} {nlines:>6} {nrefs:>5} {per['ok']:>4} "
            f"{per['abbrev']:>5} {per['artifact']:>4} {per['MISSING']:>4} {fences:>4}"
        )

    print("-" * len(hdr))
    print(
        f"{'TOTAL':<42} {'':>6} {sum(totals.values()):>5} {totals['ok']:>4} "
        f"{totals['abbrev']:>5} {totals['artifact']:>4} {totals['MISSING']:>4}"
    )

    if errors:
        print("\nREAL ERRORS (path does not exist and is not a build artifact):")
        for name, lineno, raw, detail in errors:
            suffix = f"  — {detail}" if detail else ""
            print(f"  {name}:{lineno}  `{raw}`{suffix}")
        return 1
    print("\nAll cited paths resolve.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
