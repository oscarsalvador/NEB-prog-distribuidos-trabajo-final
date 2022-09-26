import sys
import cv2
import numpy as np
import os

print (sys.argv)

img = cv2.imread(sys.argv[1])
dst = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
cv2.imwrite(sys.argv[1],dst)

print("Python: "+sys.argv[1]+" difuminada")
