package com.tourGuide.domain.location;

import java.util.UUID;

public class Attraction extends Location {
    public final String attractionName;
    public final String city;
    public final String state;
    public final UUID attractionId;

    public Attraction(String attractionName, String city, String state, double latitude, double longitude) {
        super(latitude, longitude);
        this.attractionName = attractionName;
        this.city = city;
        this.state = state;
        this.attractionId = UUID.randomUUID();
    }
}
