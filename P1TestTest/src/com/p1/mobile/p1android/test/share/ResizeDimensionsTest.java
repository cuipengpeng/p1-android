package com.p1.mobile.p1android.test.share;

import junit.framework.TestCase;
import android.graphics.Point;

import com.p1.mobile.p1android.util.BitmapUtils;

public class ResizeDimensionsTest extends TestCase {


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testLarge43Image(){
        Point p = BitmapUtils.determineSaveSize((int) (960 * 1.7),
                (int) (1280 * 1.7));
        assertEquals(p, new Point(960, 1280));
    }

    public void testSmall43Image() {
        Point p = BitmapUtils.determineSaveSize((int) (960 * 0.6),
                (int) (1280 * 0.6));
        assertEquals(p, new Point((int) (960 * 0.6), (int) (1280 * 0.6)));
    }

    public void testLargeWideImage() {
        Point p = BitmapUtils.determineSaveSize((int) (1280 * 1.6),
                (int) (960 * 1.6));
        assertEquals(p, new Point((int) (1280), (int) (960)));
    }

    public void testSmallWideImage() {
        Point p = BitmapUtils.determineSaveSize((int) (1280 * 0.6),
                (int) (960 * 0.6));
        assertEquals(p, new Point((int) (1280 * 0.6), (int) (960 * 0.6)));
    }

    public void testLargeTallImage() {
        Point p = BitmapUtils.determineSaveSize((int) (500 * 1.7),
                (int) (1280 * 1.7));
        assertEquals(p, new Point(500, 1280));
    }

    public void testSmallTallImage() {
        Point p = BitmapUtils.determineSaveSize((int) (960 * 0.6),
                (int) (1500 * 0.6));
        assertEquals(p, new Point((int) (960 * 0.6), (int) (1500 * 0.6)));
    }

}
