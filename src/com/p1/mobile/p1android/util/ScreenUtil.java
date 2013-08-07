package com.p1.mobile.p1android.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtil {

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static int getScreenWidth(Context c) {
        if (c == null)
            return 0;
        WindowManager wm = (WindowManager) c
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Utils.hasHoneycombMR2()) {
            Point size = new Point();
            display.getSize(size);
            return size.x;
        } else {
            return display.getWidth();
        }
    }
}
