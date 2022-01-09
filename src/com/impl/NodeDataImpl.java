package com.impl;

import com.api.GeoLocation;
import com.api.NodeData;

public class NodeDataImpl implements NodeData, Comparable<NodeDataImpl> {
    public static final String WHITE_COLOR = "white";
    public static final String BLACK_COLOR = "black";
    public static final String RED_COLOR = "red";

    //UsedForJson
    private int mKey;
    private int mTag = -1;
    private String mInfo = WHITE_COLOR;
    private double mNodeWeight = -1;
    private GeoLocation mGeoLocation;

    public NodeDataImpl(int key, String location) {
        this.mKey = key;
        this.mGeoLocation = new GeoLocationImpl(location);
    }

    public NodeDataImpl(int key, int tag, String info, double nodeWeight, GeoLocation geoLocation) {
        this.mKey = key;
        this.mGeoLocation = geoLocation;
        mTag = tag;
        mInfo = info;
        mNodeWeight = nodeWeight;
    }

    @Override
    public int getKey() {
        return mKey;
    }

    @Override
    public GeoLocation getLocation() {
        return mGeoLocation;
    }

    @Override
    public void setLocation(GeoLocation p) {
        mGeoLocation = p;
    }

    @Override
    public double getWeight() {
        return mNodeWeight;
    }

    @Override
    public void setWeight(double w) {
        mNodeWeight = w;
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


    @Override
    public int compareTo(NodeDataImpl o) {
        if (getWeight() - o.getWeight() > 0) {
            return 1;
        } else if (this.getWeight() - o.getWeight() < 0) {
            return -1;
        }
        return 0;
    }
}
