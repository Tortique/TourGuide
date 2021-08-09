package tourGuide;

import com.tourGuide.Application;
import com.tourGuide.domain.User;
import com.tourGuide.domain.UserAndReward;
import com.tourGuide.domain.UserPreferences;
import com.tourGuide.domain.UserReward;
import com.tourGuide.domain.location.Attraction;
import com.tourGuide.domain.location.Location;
import com.tourGuide.domain.location.VisitedLocation;
import com.tourGuide.helper.InternalTestHelper;
import com.tourGuide.service.TourGuideService;
import com.tourGuide.service.feignClient.GpsFeignClient;
import com.tourGuide.service.feignClient.RewardFeignClient;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import tripPricer.Provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = Application.class)
public class TestTourGuideService {

    @MockBean
    private GpsFeignClient gpsFeignClient;

    @MockBean
    private RewardFeignClient rewardFeignClient;

    @Autowired
    private TourGuideService tourGuideService;

    @BeforeEach
    public void SetUpPerTest() {
        tourGuideService.clearInternalUserMap();
    }

    @Test
    public void addUser() {
        InternalTestHelper.setInternalUserNumber(0);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        User retrievedUser = tourGuideService.getUser(user.getUserName());
        User retrievedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrievedUser);
        assertEquals(user2, retrievedUser2);
    }

    @Test
    public void getAllUsers() {
        InternalTestHelper.setInternalUserNumber(0);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        Assertions.assertTrue(allUsers.contains(user));
        Assertions.assertTrue(allUsers.contains(user2));
    }

    @Test
    public void getTripDeals() {
        InternalTestHelper.setInternalUserNumber(0);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, providers.size());
    }

    @Test
    public void trackUserLocationTest() throws InterruptedException {
        UUID uuid = UUID.randomUUID();
        Location location = new Location(48.858331, 2.294481);
        InternalTestHelper.setInternalUserNumber(0);
        Attraction attraction = new Attraction("TourEiffel", "Paris", "France", 48.858331, 2.294481);
        User user = new User(uuid, "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);

        VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), location, new Date());
        List<VisitedLocation> visitedLocations = new ArrayList<>();
        visitedLocations.add(visitedLocation);
        UserReward userReward = new UserReward(visitedLocation, attraction, 2);
        List<UserReward> userRewards = new ArrayList<>();
        userRewards.add(userReward);
        UserAndReward userAndReward = new UserAndReward(uuid, visitedLocations, userRewards);

        when(gpsFeignClient.getUserLocation(user.getUserId())).thenReturn(visitedLocation);
        when(rewardFeignClient.calculateRewards(any())).thenReturn(userRewards);

        Assertions.assertEquals(0, user.getVisitedLocations().size());

        tourGuideService.trackUserLocation(user);
        Thread.sleep(1000);

        Assertions.assertEquals(1, user.getVisitedLocations().size());
        Assertions.assertEquals(1, user.getUserRewards().size());
    }

    @Test
    public void getAllCurrentLocations() {
        UUID uuid = UUID.randomUUID();
        InternalTestHelper.setInternalUserNumber(0);
        Location location = new Location(48.858331, 2.294481);
        User user = new User(uuid, "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);

        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), location, new Date()));

        List<VisitedLocation> result = tourGuideService.getAllCurrentLocations();

        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void updateUserPreferences() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGudie.com");
        InternalTestHelper.setInternalUserNumber(0);

        tourGuideService.addUser(user);
        UserPreferences userPreferences = new UserPreferences(7, 5, 3, 2);

        boolean result = tourGuideService.updateUserPreferences("jon", userPreferences);

        assertTrue(result);
        Assertions.assertEquals(7, user.getUserPreferences().getTripDuration());
        Assertions.assertEquals(5, user.getUserPreferences().getTicketQuantity());
        Assertions.assertEquals(3, user.getUserPreferences().getNumberOfAdults());
        Assertions.assertEquals(2, user.getUserPreferences().getNumberOfChildren());
    }
}
