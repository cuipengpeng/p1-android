#!/usr/bin/python
# -*- coding: utf-8 -*-
import os
import string
import shutil
from PIL import Image
from PIL import ImageColor

"""

Prerequest:

    Python

    Python Imaging Library
            http://www.pythonware.com/products/pil/
        Windows:
            http://www.lfd.uci.edu/~gohlke/pythonlibs/#pil

    Tested under Python 2.7 and Windows 7 x64

        Mac
            # download
            curl -O -L http://effbot.org/media/downloads/Imaging-1.1.7.tar.gz
            # extract
            tar -xzf Imaging-1.1.7.tar.gz
            cd Imaging-1.1.7
            # build and install
            python setup.py build
            sudo python setup.py install
            # or install it for just you without requiring admin permissions:
            # python setup.py install --user

Usage:

    put a drawable in one of drawable-*?*dpi folder

    load python script
        python -i tools.py

        resizeDrawable('res', 'image.9.png')

        resizeIOSBtn('res', 'btn_blue.9.png')

        # source dpi folder, only downscale, and if is treated as an iOS button
        resizeDrawable0('res', 'image.9.png', 'xhdpi', False)

Result:
    
    resized for mdpi to the source dpi(only downscale, no upscale), including 9 patch

    if iOS button parameter is explicitly set to True or the filename
    starts with 'ibtn', button resources is created

        for example, `ibtn_blue.9.png` generate `ibtn_blue_pressed.9.png`
        and `ibtn_blue_xml.xml`
"""

def changeTheme(dir):
    for (path, st, names)in os.walk(dir):
        for name in names:
            longName = os.path.join(path,name)
            shutil.copyfile(longName, longName.replace(dir,'res'))
                    
def lightTheme():
    changeTheme('res_light')
    
def darkTheme():
    changeTheme('res_dark')
    
DRAWABLE_DIRS = ['drawable-xxhdpi', 'drawable-xhdpi', 'drawable-hdpi', 'drawable-mdpi']
DRAWABLE_SIZES = [144.0, 96.0, 72.0, 48.0]

CD = os.path.dirname(os.path.abspath(__file__))

def diffDrawable(res):
    os.chdir(res)
    for dir in DRAWABLE_DIRS:
        for file in os.listdir(dir):
            for dir2 in DRAWABLE_DIRS:
                if not os.path.exists(os.path.join(dir2, file)):
                    print(dir2+" "+file)
    os.chdir(CD)
    
def moveDrawable(fromDir, toDir, fro, to):
    for dir in DRAWABLE_DIRS:
        shutil.copyfile(os.path.join(fromDir,dir,fro),os.path.join(toDir,dir,to))
    
def moveLightDrawable(fro, to):
    moveDrawable(CD+'/res_iconsrc/holo_light',CD+'/res_light',fro,to)
    

def moveDarkDrawable(fro, to):
    moveDrawable(CD+'/res_iconsrc/holo_dark',CD+'/res_dark',fro,to)

#def moveSdkDrawable(fro, to):
#    moveDrawable('D:\\App\\adt-bundle-windows-x86_64\\sdk\\platforms\\android-17\\data\\res',CD+'\\res',fro,to)


def premultiply(im):
    pixels = im.load()
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = pixels[x, y]
            if a != 255:
                r = r * a // 255
                g = g * a // 255
                b = b * a // 255
                pixels[x, y] = (r, g, b, a)
    return im

def unmultiply(im):
    pixels = im.load()
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = pixels[x, y]
            if a != 255 and a != 0:
                r = 255 if r >= a else 255 * r // a
                g = 255 if g >= a else 255 * g // a
                b = 255 if b >= a else 255 * b // a
                pixels[x, y] = (r, g, b, a)
    return im

ALPHA = 0.6

def resizeIOSBtn(res, fro):
    resizeDrawable0(res, fro, 'xxhdpi', True)

def resizeDrawable(res, fro):
    resizeDrawable0(res, fro, 'xxhdpi', fro.startswith('ibtn'))

def resizeDrawable0(res, fro, fromStr, iosbtn):
        os.chdir(CD+'/'+res)
        fromIndex = 0
        fromStr = 'drawable-' + fromStr
        for dirIndex in range(0, len(DRAWABLE_DIRS)):
            if DRAWABLE_DIRS[dirIndex] == fromStr:
                fromIndex = dirIndex
                break
        image = Image.open(DRAWABLE_DIRS[fromIndex]+'/'+fro)
        width, height = image.size
        if fro.endswith('9.png'):
            inner = image.crop((1, 1, width-1, height-1))
            left = image.crop((0, 1, 1, height-1))
            top = image.crop((1, 0, width-1, 1))
            right = image.crop((width-1, 1, width, height-1))
            bottom = image.crop((1, height-1, width-1, height))
            innerPre = premultiply(inner)
            for i in range(fromIndex, len(DRAWABLE_DIRS)):
                w = int((width-2) * DRAWABLE_SIZES[i] / DRAWABLE_SIZES[fromIndex])
                h = int((height-2) * DRAWABLE_SIZES[i] / DRAWABLE_SIZES[fromIndex])
                innerAft = unmultiply(innerPre.resize((w, h),Image.ANTIALIAS))
                newImage = Image.new('RGBA', (w+2, h+2))
                newImage.paste(innerAft, (1, 1, w+1, h+1))
                li = left.resize((1, h),Image.ANTIALIAS)
                ri = right.resize((1, h),Image.ANTIALIAS)
                ti = top.resize((w, 1),Image.ANTIALIAS)
                bi = bottom.resize((w, 1),Image.ANTIALIAS)
                pixels = newImage.load()
                pixels[0, 0] = (0, 0, 0, 0)
                pixels[w+1, 0] = (0, 0, 0, 0)
                pixels[0, h+1] = (0, 0, 0, 0)
                pixels[w+1, h+1] = (0, 0, 0, 0)
                bodPixs = li.load()
                for ii in range(0, li.size[1]):
                    r, g, b, a = bodPixs[0, ii]
                    if a * 2 > 255:
                        pixels[0, ii+1] = (0, 0, 0, 255)
                    else:
                        pixels[0, ii+1] = (0, 0, 0 ,0)
                bodPixs = ri.load()
                for ii in range(0, ri.size[1]):
                    r, g, b, a = bodPixs[0, ii]
                    if a * 2 > 255:
                        pixels[w+1, ii+1] = (0, 0, 0, 255)
                    else:
                        pixels[w+1, ii+1] = (0, 0, 0 ,0)
                bodPixs = ti.load()
                for ii in range(0, ti.size[0]):
                    r, g, b, a = bodPixs[ii, 0]
                    if a * 2 > 255:
                        pixels[ii+1, 0] = (0, 0, 0, 255)
                    else:
                        pixels[ii+1, 0] = (0, 0, 0 ,0)
                bodPixs = bi.load()
                for ii in range(0, bi.size[0]):
                    r, g, b, a = bodPixs[ii, 0]
                    if a * 2 > 255:
                        pixels[ii+1, h+1] = (0, 0, 0, 255)
                    else:
                        pixels[ii+1, h+1]  = (0, 0, 0 ,0)
              #  if i != fromIndex:
                newImage.save(DRAWABLE_DIRS[i]+ '/'+fro)
                # mask a shadow
                if iosbtn:
                    for bii in range(1, w+1):
                        for bjj in range(1, h+1):
                            r, g, b, a = pixels[bii, bjj]
                            # TODO incorrect!!!
                            r = int(r * ALPHA)
                            g = int(g * ALPHA)
                            b = int(b * ALPHA)
                            pixels[bii, bjj] = (r, g, b, a)
                    newImage.save(DRAWABLE_DIRS[i]+ '/'+fro[0:len(fro)-6]+'_pressed.9.png')
        else:
            im = premultiply(image)
            for i in range(fromIndex, len(DRAWABLE_DIRS)):
             #   if i != fromIndex:
                w = int(width * DRAWABLE_SIZES[i] / DRAWABLE_SIZES[fromIndex])
                h = int(height * DRAWABLE_SIZES[i] / DRAWABLE_SIZES[fromIndex])
                unImage = unmultiply(im.resize((w, h),Image.ANTIALIAS))
                unImage.save(DRAWABLE_DIRS[i]+ '/'+fro)
                pixels = unImage.load()
                if iosbtn:
                    for bii in range(0, w):
                        for bjj in range(0, h):
                            r, g, b, a = pixels[bii, bjj]
                            # TODO incorrect!!!
                            r = int(r * ALPHA)
                            g = int(g * ALPHA)
                            b = int(b * ALPHA)
                            pixels[bii, bjj] = (r, g, b, a)
                    unImage.save(DRAWABLE_DIRS[i]+ '/'+fro[0:len(fro)-4]+'_pressed.png')

        if iosbtn:
            preName = ""
            if fro.endswith('9.png'):
                preName = fro[0:len(fro)-6]
            else:
                preName = fro[0:len(fro)-4]
            f = open(CD+'/'+res+'/drawable/'+preName+'_xml.xml', 'w+')
            f.write("""<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android" >
    
    <item android:drawable="@drawable/"""+preName+"""_pressed" android:state_pressed="true"/>
    <item android:drawable="@drawable/"""+preName+"""" android:state_pressed="false"/>

</selector>
""")
            f.close()
        os.chdir(CD)

def resizeAllDrawable(res):
        for fl in os.listdir(res+'/drawable/'):
                resizeDrawable(res, fl, pre)


def renameDrawable(fro, to):
    os.chdir('res')
    for dir in os.listdir('.'):
        if dir.startswith('drawable'):
            for file in os.listdir(dir):
                if (fro in file):
                    print(file)
                    os.rename(os.path.join(dir,file),os.path.join(dir,file.replace(fro,to)))



def extractAlpha(imageName, bgColor):
        image = Image.open(imageName)
        pixels = image.load()
        for y in range(image.size[1]):
            for x in range(image.size[0]):
                r, g, b, a = pixels[x, y]
                if r != 0:
                    a0 = int(255 * (1.0-1.0 * r/bgColor))
                    if a0 > 0 and a0 < 255:
                        pixels[x, y] = (0, 0, 0, a0)
        image.save('result_'+imageName)
