#!/usr/bin/env python3
"""Cross-check line counts claimed in each chapter's key-file index table.

Chapters end with a `| path | lines | role |` table. A stale or invented
line count is the easiest way for a claim to drift from the source, so this
compares every numeric second column against `wc -l` of the cited file.

Tolerance: exact match required for counts under 100, else +/-1% to absorb
trailing-newline differences.

Usage: python3 docs/learning/verify_linecounts.py
Exit 1 if any claimed count is wrong.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
PARTS = ROOT / "docs" / "learning" / "parts"

# | `path` | 1,234 | role |   (count may carry thousands separators)
ROW = re.compile(
    r"^\|\s*`(?P<path>[^`]+)`\s*\|\s*(?P<count>[\d,]+)\s*\|"
)
SELECTOR = re.compile(r":\d+(?:-\d+)?$")


def wc(path: pathlib.Path) -> int:
    with path.open("rb") as fh:
        return sum(1 for _ in fh)


def main() -> int:
    bad = []
    checked = 0
    print(f"{'chapter':<42} {'rows':>5} {'checked':>8} {'wrong':>6}")
    print("-" * 64)

    for md in sorted(PARTS.glob("*.md")):
        rows = n = wrong = 0
        for lineno, line in enumerate(md.read_text(encoding="utf-8").splitlines(), 1):
            m = ROW.match(line)
            if not m:
                continue
            rows += 1
            raw = m.group("path").strip()
            bare = SELECTOR.sub("", raw)
            if "/.../" in bare:
                continue
            target = ROOT / bare
            if not target.is_file():
                continue
            claimed = int(m.group("count").replace(",", ""))
            actual = wc(target)
            n += 1
            checked += 1
            tol = 0 if actual < 100 else max(1, actual // 100)
            if abs(claimed - actual) > tol:
                wrong += 1
                bad.append((md.name, lineno, bare, claimed, actual))
        print(f"{md.name:<42} {rows:>5} {n:>8} {wrong:>6}")

    print("-" * 64)
    print(f"{'TOTAL':<42} {'':>5} {checked:>8} {len(bad):>6}")

    if bad:
        print("\nLINE-COUNT MISMATCHES (claimed vs actual):")
        for name, lineno, path, claimed, actual in bad:
            print(f"  {name}:{lineno}  {path}  claimed {claimed}, actual {actual}")
        return 1
    print("\nAll claimed line counts match the working tree.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
