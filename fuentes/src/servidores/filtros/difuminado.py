import sys
import cv2
import numpy as np
import os

print (sys.argv)

img = cv2.imread(sys.argv[1])
kernel = np.ones((5,5),np.float32)/25
dst = cv2.filter2D(img,-1,kernel)
cv2.imwrite(sys.argv[1],dst)

print("Python: "+sys.argv[1]+" difuminada")
