import sys
import cv2
import numpy as np
import os

print("boñlasfkj",file=sys.stderr)
print (sys.argv)

img = cv2.imread(sys.argv[1])
dst = cv2.stylization(img, sigma_s=60, sigma_r=0.6)
cv2.imwrite(sys.argv[1],dst)

print("Python: "+sys.argv[1]+" difuminada")
