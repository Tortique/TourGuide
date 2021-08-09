package com.tourGuide.domain;

import com.tourGuide.domain.location.VisitedLocation;

import java.util.*;
import java.util.UUID;

public class UserAndReward {

    private UUID userId;
    private List<VisitedLocation> visitedLocations = new ArrayList<>();
    private List<UserReward> userRewards = new ArrayList<>();

    public UserAndReward(UUID userId, List<VisitedLocation> visitedLocations, List<UserReward> userRewards) {
        super();
        this.userId = userId;
        this.visitedLocations = visitedLocations;
        this.userRewards = userRewards;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setVisitedLocations(List<VisitedLocation> visitedLocations) {
        this.visitedLocations = visitedLocations;
    }

    public void setUserRewards(List<UserReward> userRewards) {
        this.userRewards = userRewards;
    }

    public void addToVisitedLocations(VisitedLocation visitedLocation) {
        visitedLocations.add(visitedLocation);
    }

    public List<VisitedLocation> getVisitedLocations() {
        return visitedLocations;
    }

    public void clearVisitedLocations() {
        visitedLocations.clear();
    }

    public void addUserReward(UserReward userReward) {
        userRewards.add(userReward);
    }

    public List<UserReward> getUserRewards() {
        return userRewards;
    }
}
