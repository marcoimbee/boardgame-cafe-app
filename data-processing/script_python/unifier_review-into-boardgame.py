# Aggiunge le review a boardgame

import json

fb = open("boardgames.json", encoding="utf8")
fr = open("reviews.json", encoding="utf8")

dataB = json.load(fb)
dataR = json.load(fr)

for bg in dataB:
    bg["reviews"] = []


i = 0
size = len(dataR)

for rev in dataR:
    for bg in dataB:
        if bg["boardgameName"] == rev["boardgameName"]:
            bg["reviews"].append(rev)
            break
    
    i += 1
    print(str(i) + "/" + str(size))

with open("bg_test.json", "w") as outfile:
    json.dump(dataB, outfile, indent=4)
    
fb.close()
fr.close()