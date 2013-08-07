package com.p1.mobile.p1android.util;

import android.util.Log;
import android.util.SparseArray;

public class PerformanceMeasure {
    public static final String TAG = PerformanceMeasure.class.getSimpleName();

    public static final boolean measuring = true;

    private static int nextMeasureId = 0;

    private static SparseArray<Long> startTimes = new SparseArray<Long>();

    /**
     * 
     * @return the Id with which to end measurement.
     */
    public static int startMeasure() {
        synchronized (startTimes) {
            if (measuring) {
                nextMeasureId++;
                startTimes.put(nextMeasureId, System.nanoTime());
                return nextMeasureId;
            }
            return 0;
        }
    }

    public static void endMeasure(int measureId, String message) {
        synchronized (startTimes) {
            if (measuring) {
                Long startTime = startTimes.get(measureId);
                if (startTime == null) {
                    Log.w(TAG, "measureId " + measureId + "not found");
                }
                long passedTime = System.nanoTime() - startTime;
                startTimes.remove(measureId);
                Log.v(TAG, "Measurement is " + passedTime / 1000000 + "."
                        + (passedTime / 100000) % 10 + "ms ("
                        + message
                        + ")");
            }
        }
    }

}
