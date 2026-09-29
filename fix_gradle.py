import os

with open('gradle.properties', 'a') as f:
    f.write('\nandroid.builtInKotlin=false\nandroid.newDsl=false\n')

print("done")
