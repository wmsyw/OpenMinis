#!/usr/bin/env python3
"""Verify the readable index and downloadable archive navigation.

Checks:
- every local Markdown link in the readable index resolves;
- the index links every `parts/*.md` chapter exactly once or more;
- the readable index stays below 100 KiB and each chapter below 200 KiB;
- every internal TOC anchor in the generated full archive resolves.

Usage: python3 docs/learning/verify_toc.py
Run `sh docs/learning/assemble.sh` first.
Exit 1 on any broken link, missing chapter, or render-size regression.
"""
import difflib
import pathlib
import re
import sys
from urllib.parse import unquote

BASE = pathlib.Path(__file__).resolve().parent
INDEX = BASE / "OpenMinis-Agent-App-Engineering.md"
ARCHIVE = BASE / "OpenMinis-Agent-App-Engineering-full.md"
PARTS = BASE / "parts"
LINK = re.compile(r"\[([^\]]+)\]\(([^)]+)\)")
HEADING = re.compile(r"^#{1,6}\s+(.+)$", re.M)


def slug(heading: str) -> str:
    s = heading.strip().lower()
    s = re.sub(r"[^\w\s-]", "", s, flags=re.UNICODE)
    return re.sub(r"\s+", "-", s)


def local_target(raw: str):
    """Return (path, anchor), or None for web/mail links."""
    target = raw.strip().strip("<>")
    if re.match(r"^[a-z][a-z0-9+.-]*://", target, re.I) or target.startswith("mailto:"):
        return None
    path_text, _, anchor = target.partition("#")
    path = INDEX if not path_text else (BASE / unquote(path_text)).resolve()
    return path, unquote(anchor)


def heading_anchors(path: pathlib.Path):
    return {slug(h) for h in HEADING.findall(path.read_text(encoding="utf-8"))}


def main() -> int:
    errors = []
    if not INDEX.is_file():
        print(f"missing {INDEX.name}", file=sys.stderr)
        return 1
    if not ARCHIVE.is_file():
        print(f"missing {ARCHIVE.name} — run assemble.sh first", file=sys.stderr)
        return 1

    index_text = INDEX.read_text(encoding="utf-8")
    links = LINK.findall(index_text)
    linked_parts = set()
    local_count = 0

    for label, raw in links:
        parsed = local_target(raw)
        if parsed is None:
            continue
        local_count += 1
        path, anchor = parsed
        if not path.exists():
            errors.append(f"index: [{label}]({raw}) — target does not exist")
            continue
        try:
            rel = path.relative_to(PARTS.resolve())
            if path.suffix == ".md":
                linked_parts.add(path)
        except ValueError:
            pass
        if anchor and path.is_file():
            anchors = heading_anchors(path)
            if anchor not in anchors:
                near = difflib.get_close_matches(anchor, anchors, n=1, cutoff=0.5)
                hint = f"; did you mean #{near[0]}" if near else ""
                errors.append(f"index: [{label}]({raw}) — missing anchor{hint}")

    expected_parts = set(PARTS.glob("*.md"))
    missing_parts = expected_parts - linked_parts
    extra_parts = linked_parts - expected_parts
    for path in sorted(missing_parts):
        errors.append(f"index does not link chapter {path.name}")
    for path in sorted(extra_parts):
        errors.append(f"index links unexpected chapter {path.name}")

    index_size = INDEX.stat().st_size
    if index_size > 100 * 1024:
        errors.append(f"readable index is {index_size} bytes; must stay below 100 KiB")
    oversized_parts = [(p, p.stat().st_size) for p in sorted(expected_parts) if p.stat().st_size > 200 * 1024]
    for path, size in oversized_parts:
        errors.append(f"chapter {path.name} is {size} bytes; split it below 200 KiB")

    archive_text = ARCHIVE.read_text(encoding="utf-8")
    archive_anchors = {slug(h) for h in HEADING.findall(archive_text)}
    archive_links = [(label, raw) for label, raw in LINK.findall(archive_text) if raw.startswith("#")]
    for label, raw in archive_links:
        anchor = raw[1:]
        if anchor not in archive_anchors:
            near = difflib.get_close_matches(anchor, archive_anchors, n=1, cutoff=0.5)
            hint = f"; did you mean #{near[0]}" if near else ""
            errors.append(f"archive: [{label}]({raw}) — missing anchor{hint}")

    print(f"index: {index_size} bytes, {local_count} local links")
    print(f"chapters: {len(expected_parts)} total, {len(linked_parts)} linked, {len(oversized_parts)} oversized")
    print(f"archive: {ARCHIVE.stat().st_size} bytes, {len(archive_links)} internal TOC links")
    print(f"broken: {len(errors)}")

    if errors:
        print("\nERRORS:")
        for error in errors:
            print(f"  {error}")
        return 1

    print("All chapter links and archive anchors resolve.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
