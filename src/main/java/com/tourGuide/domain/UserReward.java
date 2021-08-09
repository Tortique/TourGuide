package com.tourGuide.domain;

import com.tourGuide.domain.location.Attraction;
import com.tourGuide.domain.location.VisitedLocation;

public class UserReward {

	public VisitedLocation visitedLocation;
	public Attraction attraction;
	private int rewardPoints;

	public UserReward(VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
		this.visitedLocation = visitedLocation;
		this.attraction = attraction;
		this.rewardPoints = rewardPoints;
	}
	
	public UserReward(VisitedLocation visitedLocation, Attraction attraction) {
		this.visitedLocation = visitedLocation;
		this.attraction = attraction;
	}

	public UserReward() {}

	public void setRewardPoints(int rewardPoints) {
		this.rewardPoints = rewardPoints;
	}
	
	public int getRewardPoints() {
		return rewardPoints;
	}

	public VisitedLocation getVisitedLocation() {
		return visitedLocation;
	}

	public void setVisitedLocation(VisitedLocation visitedLocation) {
		this.visitedLocation = visitedLocation;
	}

	public Attraction getAttraction() {
		return attraction;
	}

	public void setAttraction(Attraction attraction) {
		this.attraction = attraction;
	}
}
