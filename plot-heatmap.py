from matplotlib import pyplot as pp
import os

os.chdir(r'C:\Users\collin\Projects\Capstone\plankton')

cm = [[0 for x in range(121)] for i in range(121)]
for i in range(5):
    with open('doc\\results\\experiment4-lr\\split' + str(i) + '.csv') as f:
        lines = [l.strip() for l in f.readlines()]
        del lines[0]
        
        for y in range(121):
            spl = lines[y].split(',')[1:]
            for x in range(121):
                cm[y][x] += float(spl[x])

with open("data\\all-classes.txt") as f:
    labels = [l.strip() for l in f.readlines()]
    
fig, ax = pp.subplots()
heatmap = ax.pcolor(cm, cmap=pp.cm.Blues)
# heatmap = ax.pcolor(cm, cmap=pp.cm.gist_stern)
pp.xlim(0,121)
pp.ylim(0, 121)
ax.xaxis.tick_top()
ax.xaxis.set_label_position('top')
ax.invert_yaxis()
ax.set_xlabel("Predictions")
ax.set_ylabel("Actual")
ax = pp.gca()
fig.set_size_inches(12,12)
pp.savefig('blue.png', dpi=400, format='png', bbox_inches="tight")
pp.savefig('stern.png', dpi=400, format='png', bbox_inches="tight")