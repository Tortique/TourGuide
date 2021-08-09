package com.tourGuide.service;

import com.tourGuide.TourGuideController;
import com.tourGuide.domain.User;
import com.tourGuide.domain.UserAndReward;
import com.tourGuide.domain.UserPreferences;
import com.tourGuide.domain.UserReward;
import com.tourGuide.domain.location.Location;
import com.tourGuide.domain.location.VisitedLocation;
import com.tourGuide.helper.InternalTestHelper;
import com.tourGuide.service.feignClient.GpsFeignClient;
import com.tourGuide.service.feignClient.RewardFeignClient;
import com.tourGuide.tracker.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;

    @Autowired
    private GpsFeignClient gpsFeignClient;

    @Autowired
    private RewardFeignClient rewardFeignClient;

    @Autowired
    private TourGuideController tourGuideController;

    public TourGuideService() {
        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    ExecutorService executorService = Executors.newFixedThreadPool(1000);

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    @Async
    public CompletableFuture<?> trackUserLocation(final User user) {
        return CompletableFuture.supplyAsync(() -> {
            return gpsFeignClient.getUserLocation(user.getUserId());
        }, executorService).thenAccept(visitedLocation -> {
            user.addToVisitedLocations(
                    new VisitedLocation(visitedLocation.userId,
                            new Location(visitedLocation.location.latitude, visitedLocation.location.longitude),
                            visitedLocation.timeVisited));
        }).thenRunAsync(() -> trackUserReward(user));
    }

    @Async
    public CompletableFuture<?> trackUserReward(final User user) {
        return CompletableFuture.supplyAsync(() -> {
            return rewardFeignClient.calculateRewards(new UserAndReward(user.getUserId(), user.getVisitedLocations(), user.getUserRewards()));
        }, executorService).thenAccept(userRewards -> {
            userRewards.stream().forEach(userReward -> user.addUserReward(userReward));
        });
    }

    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public List<VisitedLocation> getAllCurrentLocations() {
        return getAllUsers().stream().map(user ->
        {
            return user.getLastVisitedLocation();
        }).collect(Collectors.toList());
    }

    public boolean updateUserPreferences(String userName, UserPreferences userPreferences) {
        User user = internalUserMap.get(userName);
        if (user == null) {
            return false;
        }
        user.setUserPreferences(userPreferences);
        return true;
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(tracker::stopTracking));
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    public void clearInternalUserMap() {
        internalUserMap.clear();
    }
}
