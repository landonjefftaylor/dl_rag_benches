import os

models = ["2react", "6react", "8react"]
props = ["s2 = 80", "s5 = 25", "gbg = 50"]

for i in range(3):
    with open("probTemp.java", "r") as infile:
        t = infile.read().replace("_FILEPATH_", "models/" + models[i] + ".sm").replace("_PROPERTY_", props[i])
        with open("src/GetProbability.java", "w") as outfile:
            outfile.write(t)

    os.system("make")
    os.system("time make test")
    os.system("echo ")
    os.system("echo ")