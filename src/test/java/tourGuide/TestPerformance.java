package tourGuide;

import com.tourGuide.Application;
import com.tourGuide.domain.User;
import com.tourGuide.domain.location.Attraction;
import com.tourGuide.domain.location.Location;
import com.tourGuide.domain.location.VisitedLocation;
import com.tourGuide.helper.InternalTestHelper;
import com.tourGuide.service.TourGuideService;
import com.tourGuide.service.feignClient.GpsFeignClient;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = Application.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class TestPerformance {
    @Autowired
    private ThreadPoolTaskExecutor executor;

    @Autowired
    private TourGuideService tourGuideService;

    @Autowired
    private GpsFeignClient gpsFeignClient;

    /*
     * A note on performance improvements:
     *
     *     The number of users generated for the high volume tests can be easily adjusted via this method:
     *
     *     		InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     *     These tests can be modified to suit new solutions, just as long as the performance metrics
     *     at the end of the tests remains consistent.
     *
     *     These are performance metrics that we are trying to hit:
     *
     *     highVolumeTrackLocation: 100,000 users within 15 minutes:
     *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
     *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

    @Test
    public void highVolumeTrackLocation() throws InterruptedException {
        // Users should be incremented up to 100,000, and test finishes within 15 minutes
        InternalTestHelper.setInternalUserNumber(100000);

        int rewardsCount = 0;

        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        Attraction attraction = gpsFeignClient.getAttractions().get(0);
        allUsers.forEach(user -> {
            user.clearVisitedLocations();
            user.getUserRewards().clear();
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), (new Location(attraction.latitude, attraction.longitude)), new Date()));
        });

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        allUsers.forEach(user -> {
            tourGuideService.trackUserLocation(user);
        });

        while(rewardsCount < allUsers.size()) {
            rewardsCount = 0;
            rewardsCount += allUsers.stream().filter(user -> user.getUserRewards().size() >= 1).count();
        }

        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
        allUsers.forEach(user -> assertEquals(2, user.getVisitedLocations().size()));
        allUsers.forEach(user -> assertEquals(1, user.getUserRewards().size()));
    }

    @Test
    public void highVolumeGetRewards() throws InterruptedException {
        // Users should be incremented up to 100,000, and test finishes within 20 minutes
        InternalTestHelper.setInternalUserNumber(100000);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int rewardsCount = 0;

        Attraction attraction = gpsFeignClient.getAttractions().get(0);
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();
        allUsers.forEach(user -> {
            user.clearVisitedLocations();
            user.getUserRewards().clear();
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), (new Location(attraction.latitude, attraction.longitude)), new Date()));
        });

        allUsers.forEach(user -> {
            tourGuideService.trackUserReward(user);
        });

        while(rewardsCount < allUsers.size()) {
            rewardsCount = 0;
            rewardsCount += allUsers.stream().filter(user -> user.getUserRewards().size() >= 1).count();
        }

        stopWatch.stop();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
        allUsers.forEach(user -> assertEquals(1, user.getUserRewards().size()));
    }

}
