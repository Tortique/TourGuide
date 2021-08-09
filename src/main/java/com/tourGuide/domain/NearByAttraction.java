package com.tourGuide.domain;

import com.tourGuide.domain.location.Location;

public class NearByAttraction {
    private String name;
    private Location attractionLocation;
    private Location userLocation;
    private double distance;
    private int rewardPoints;

    public NearByAttraction(String name, Location attractionLocation, Location userLocation, double distance, int rewardPoints) {
        this.name = name;
        this.attractionLocation = attractionLocation;
        this.userLocation = userLocation;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

    public NearByAttraction() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(Location attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}
