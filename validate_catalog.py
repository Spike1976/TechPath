import base64
import gzip
import json
import sys
from collections import Counter
from pathlib import Path

root = Path("app/src/main/assets/catalog")
encoded = "".join(
    (root / f"catalog.part{i}.b64").read_text(encoding="utf-8").strip()
    for i in range(1, 4)
)

try:
    raw = gzip.decompress(base64.b64decode(encoded)).decode("utf-8")
    data = json.loads(raw)
except Exception as exc:
    print(f"FAIL: curriculum asset could not be decoded: {exc}")
    sys.exit(1)

ids = [c["id"] for c in data["concepts"]]
errors = []

if len(ids) != len(set(ids)):
    errors.append("duplicate concept IDs")

idset = set(ids)
category_ids = {c["id"] for c in data["categories"]}

for c in data["concepts"]:
    if c.get("categoryId") not in category_ids:
        errors.append(f"{c['id']}: missing category {c.get('categoryId')}")

    for key in ("prerequisites", "nextSteps"):
        for ref in c.get(key, []):
            if ref not in idset:
                errors.append(f"{c['id']}: missing {key} ref {ref}")

    for key in ("summary", "whyItMatters", "handsOn", "checkQuestion", "checkAnswer"):
        if len(c.get(key, "").strip()) < 20:
            errors.append(f"{c['id']}: thin {key}")

    if len(c.get("learnSteps", [])) < 5:
        errors.append(f"{c['id']}: fewer than 5 learning steps")

    if not c.get("realWorldUses"):
        errors.append(f"{c['id']}: no real-world uses")

for project in data["projects"]:
    if not project.get("conceptIds"):
        errors.append(f"{project['id']}: empty project path")
    for ref in project.get("conceptIds", []):
        if ref not in idset:
            errors.append(f"{project['id']}: missing project ref {ref}")

print(
    f"categories={len(data['categories'])} "
    f"concepts={len(data['concepts'])} "
    f"projects={len(data['projects'])}"
)
print("by_category=", dict(Counter(c["categoryId"] for c in data["concepts"])))

if errors:
    print("\n".join(errors))
    sys.exit(1)

print("PASS: compressed curriculum and knowledge graph integrity checks passed")
