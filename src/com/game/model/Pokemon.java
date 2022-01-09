package com.game.model;

import com.api.EdgeData;
import com.impl.GeoLocationImpl;

public class Pokemon {

    private int mValue;
    private int mType;
    private EdgeData mCurrentEdgePlace;
    private double distance;


    public int getValue() {
        return mValue;
    }

    public void setDistanceFromAgentToPokemon(double distance) {
        this.distance = distance;
    }

    public int getType() {
        return mType;
    }

    public GeoLocationImpl getPosition() {
        return mPosition;
    }

    private GeoLocationImpl mPosition;

    public Pokemon(int value, int type, String pos) {
        mValue = value;
        mType = type;
        mPosition = new GeoLocationImpl(pos);
    }

    public void setEdge(EdgeData edgeData) {
        mCurrentEdgePlace = edgeData;
    }

    public EdgeData getCurrentEdgePlace() {
        return mCurrentEdgePlace;
    }

    public double getDistance() {
        return distance;
    }


}
