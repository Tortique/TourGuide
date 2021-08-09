package com.tourGuide;

import com.jsoniter.output.JsonStream;
import com.tourGuide.domain.User;
import com.tourGuide.domain.UserPreferences;
import com.tourGuide.domain.location.VisitedLocation;
import com.tourGuide.service.TourGuideService;
import com.tourGuide.service.feignClient.GpsFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;

    @Autowired
    GpsFeignClient gpsFeignClient;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName) {
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }

    @RequestMapping("/addUser")
    public void addUser(@RequestBody User user) {
        tourGuideService.addUser(user);
    }

    @GetMapping("/getUserLocation")
    public VisitedLocation getUserLocation(@RequestParam UUID uuid) {
        return gpsFeignClient.getUserLocation(uuid);
    }

    @GetMapping("/trackUser")
    public VisitedLocation trackUser(@RequestBody User user) {
        return gpsFeignClient.getUserLocation(user.getUserId());
    }

    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
    }

    @RequestMapping("/updateUserPreferences")
    public String updateUserPreferences(@RequestParam String userName, @RequestBody UserPreferences userPreferences) throws Exception {
        if (!tourGuideService.updateUserPreferences(userName,userPreferences)) {
            throw new Exception("User not found" + userName);
        }
        return "User preferences updated";
    }
}