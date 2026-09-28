import json, sys
from collections import Counter

path = 'app/src/main/assets/catalog.json'
data = json.load(open(path, encoding='utf-8'))
ids = [c['id'] for c in data['concepts']]
errors=[]
if len(ids) != len(set(ids)):
    errors.append('duplicate concept IDs')
idset=set(ids)
for c in data['concepts']:
    for key in ('prerequisites','nextSteps'):
        for ref in c.get(key,[]):
            if ref not in idset:
                errors.append(f"{c['id']}: missing {key} ref {ref}")
    for key in ('summary','whyItMatters','handsOn','checkQuestion','checkAnswer'):
        if len(c.get(key,'').strip()) < 20:
            errors.append(f"{c['id']}: thin {key}")
    if len(c.get('learnSteps',[])) < 5:
        errors.append(f"{c['id']}: fewer than 5 learning steps")
for p in data['projects']:
    for ref in p['conceptIds']:
        if ref not in idset:
            errors.append(f"{p['id']}: missing project ref {ref}")

print(f"categories={len(data['categories'])} concepts={len(data['concepts'])} projects={len(data['projects'])}")
print('by_category=', dict(Counter(c['categoryId'] for c in data['concepts'])))
if errors:
    print('\n'.join(errors))
    sys.exit(1)
print('PASS: catalog integrity checks passed')
