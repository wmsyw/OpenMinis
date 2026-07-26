#!/usr/bin/env python3
"""Check that every TOC link in the assembled master document resolves.

Reproduces GitHub's slugger (lowercase, drop non-word/space/hyphen chars,
spaces to hyphens) and compares each `[text](#anchor)` against the set of
`## ` headings.

Usage: python3 docs/learning/verify_toc.py
Exit 1 on any broken anchor.
"""
import difflib
import pathlib
import re
import sys

DOC = pathlib.Path(__file__).resolve().parent / "OpenMinis-Agent-App-Engineering.md"


def slug(heading: str) -> str:
    s = heading.strip().lower()
    s = re.sub(r"[^\w\s-]", "", s, flags=re.UNICODE)
    return re.sub(r"\s+", "-", s)


def main() -> int:
    if not DOC.is_file():
        print(f"missing {DOC.name} — run assemble.sh first", file=sys.stderr)
        return 1

    text = DOC.read_text(encoding="utf-8")
    headings = re.findall(r"^## (.+)$", text, re.M)
    anchors = {slug(h) for h in headings}
    links = re.findall(r"\[([^\]]+)\]\(#([^)]+)\)", text)

    bad = [(t, a) for t, a in links if a not in anchors]
    print(f"headings: {len(headings)}  toc links: {len(links)}  broken: {len(bad)}")

    if bad:
        print("\nBROKEN ANCHORS:")
        for t, a in bad:
            print(f"  [{t}](#{a})")
            near = difflib.get_close_matches(a, anchors, n=1, cutoff=0.5)
            if near:
                print(f"      did you mean #{near[0]} ?")
        return 1

    print("All TOC anchors resolve.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
