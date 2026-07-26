#!/bin/sh
# Assemble docs/learning/parts/*.md into the single master document.
# Regenerate after editing any part: sh docs/learning/assemble.sh
set -eu

cd "$(dirname "$0")"
OUT=OpenMinis-Agent-App-Engineering.md
PARTS=parts

[ -d "$PARTS" ] || { echo "missing $PARTS/" >&2; exit 1; }

{
  cat header.md
  echo
  for f in "$PARTS"/*.md; do
    cat "$f"
    echo
    echo
  done
} > "$OUT"

echo "$OUT: $(wc -l < "$OUT") lines, $(wc -c < "$OUT") bytes"
