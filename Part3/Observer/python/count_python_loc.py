from pathlib import Path
import sys

if len(sys.argv) != 2:
    raise SystemExit("Usage: python count_python_loc.py <production_file.py>")

source = Path(sys.argv[1])
if not source.is_file():
    raise SystemExit(f"File not found: {source}")

lines = source.read_text(encoding="utf-8").splitlines()
loc = sum(
    1 for line in lines
    if line.strip() and not line.lstrip().startswith("#")
)
print(f"File: {source}")
print(f"Total physical lines: {len(lines)}")
print(f"Production LOC (nonblank, excluding comment-only lines): {loc}")
