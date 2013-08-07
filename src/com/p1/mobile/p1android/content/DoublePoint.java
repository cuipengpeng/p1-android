package com.p1.mobile.p1android.content;

public class DoublePoint {

    private final Double x;
    private final Double y;

    public DoublePoint() {
        x = 0.0;
        y = 0.0;
    }

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;

    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }
}
