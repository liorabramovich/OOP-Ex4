package com.impl;

import com.api.EdgeData;

public class EdgeDataImpl implements EdgeData {


    private int src, dest;
    private double w;
    private int mTag = -1;
    public String mInfo = "WHITE";

    public EdgeDataImpl(int src, int dest, double weight) {
        this.src = src;
        this.dest = dest;
        this.w = weight;
    }


    @Override
    public int getSrc() {
        return src;
    }

    @Override
    public int getDest() {
        return dest;
    }

    @Override
    public double getWeight() {
        return w;
    }

    @Override
    public String getInfo() {
        return mInfo;
    }

    @Override
    public void setInfo(String s) {
        mInfo = s;
    }

    @Override
    public int getTag() {
        return mTag;
    }

    @Override
    public void setTag(int t) {
        mTag = t;
    }
}
