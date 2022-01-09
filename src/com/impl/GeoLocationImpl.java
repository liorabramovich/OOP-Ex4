package com.impl;

import com.api.GeoLocation;

public class GeoLocationImpl implements GeoLocation {

    private final double mX, mY, mZ;

    public GeoLocationImpl(String str) {
        String[] splitter = str.split(",");
        mX = Double.parseDouble(splitter[0]);
        mY = Double.parseDouble(splitter[1]);
        mZ = Double.parseDouble(splitter[2]);
    }

    public GeoLocationImpl(double x, double y, double z) {
        this.mX = x;
        this.mY = y;
        this.mZ = z;
    }

    @Override
    public double x() {
        return mX;
    }

    @Override
    public double y() {
        return mY;
    }

    @Override
    public double z() {
        return mZ;
    }

    @Override
    public double distance(GeoLocation g) {
        double x1 = mX - g.x();
        double y1 = mY - g.y();
        double z1 = mZ - g.z();
        return Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
    }
}
