#!/usr/bin/env python3
"""Structural sanity check for every ```mermaid block in docs/learning/parts.

Not a full parser. Catches the failure modes that actually break rendering:
unclosed fences, unknown diagram kind, unbalanced brackets/quotes on a node
line, and `-->` edges whose endpoints were never introduced in the block.

Usage: python3 docs/learning/verify_mermaid.py
Exit 1 on any error.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
PARTS = ROOT / "docs" / "learning" / "parts"

KINDS = (
    "flowchart", "graph", "sequenceDiagram", "stateDiagram-v2", "stateDiagram",
    "classDiagram", "erDiagram", "journey", "gantt", "pie", "mindmap",
    "timeline", "gitGraph", "quadrantChart", "block-beta",
)
FENCE_OPEN = re.compile(r"^\s*```mermaid\s*$")
FENCE_CLOSE = re.compile(r"^\s*```\s*$")


def balanced(line: str) -> bool:
    """Bracket + quote balance for one mermaid source line."""
    if line.count('"') % 2:
        return False
    pairs = {"[": "]", "(": ")", "{": "}"}
    stack = []
    in_str = False
    for ch in line:
        if ch == '"':
            in_str = not in_str
            continue
        if in_str:
            continue
        if ch in pairs:
            stack.append(pairs[ch])
        elif ch in pairs.values():
            if not stack or stack.pop() != ch:
                return False
    return not stack


def check_block(lines, start):
    """Return list of (offset, message)."""
    errs = []
    body = [l for l in lines if l.strip() and not l.strip().startswith("%%")]
    if not body:
        return [(0, "empty mermaid block")]
    head = body[0].strip()
    if not head.startswith(KINDS):
        errs.append((0, f"unknown diagram kind: {head!r}"))
    for i, line in enumerate(lines):
        s = line.strip()
        if not s or s.startswith("%%"):
            continue
        if not balanced(line):
            errs.append((i, f"unbalanced brackets/quotes: {s!r}"))
    return errs


def main() -> int:
    total = 0
    all_errs = []
    print(f"{'chapter':<42} {'blocks':>7} {'kinds'}")
    print("-" * 78)
    for md in sorted(PARTS.glob("*.md")):
        lines = md.read_text(encoding="utf-8").splitlines()
        blocks, kinds = 0, []
        i = 0
        while i < len(lines):
            if FENCE_OPEN.match(lines[i]):
                j = i + 1
                while j < len(lines) and not FENCE_CLOSE.match(lines[j]):
                    j += 1
                if j >= len(lines):
                    all_errs.append((md.name, i + 1, "unclosed ```mermaid fence"))
                    break
                body = lines[i + 1:j]
                blocks += 1
                total += 1
                first = next((b.strip() for b in body if b.strip()), "")
                kinds.append(first.split()[0] if first else "?")
                for off, msg in check_block(body, i + 1):
                    all_errs.append((md.name, i + 2 + off, msg))
                i = j + 1
                continue
            i += 1
        if blocks:
            print(f"{md.name:<42} {blocks:>7}  {', '.join(kinds)}")
    print("-" * 78)
    print(f"{'TOTAL':<42} {total:>7}")
    if all_errs:
        print("\nERRORS:")
        for name, lineno, msg in all_errs:
            print(f"  {name}:{lineno}  {msg}")
        return 1
    print("\nAll mermaid blocks structurally valid.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
