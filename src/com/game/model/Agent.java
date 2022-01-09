package com.game.model;

import com.impl.GeoLocationImpl;

public class Agent {

    private int mId;
    private int mSrc;
    private int mDest;
    private GeoLocationImpl mPosition;
    private int mSpeed;
    private int mValue;

    public Agent(int id, int src, int dest, String pos, int speed, int value) {
        mValue = 0;
        mSpeed = 0;
        mId = id;
        mSrc = src;
        mDest = dest;
        mPosition = new GeoLocationImpl(pos);
        mSpeed = speed;
        mValue = value;
    }

    public int getId() {
        return mId;
    }

    public int getSrc() {
        return mSrc;
    }

    public int getDest() {
        return mDest;
    }

    public GeoLocationImpl getPosition() {
        return mPosition;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public int getValue() {
        return mValue;
    }


}
