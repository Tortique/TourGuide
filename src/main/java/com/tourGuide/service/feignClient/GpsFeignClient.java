package com.tourGuide.service.feignClient;

import com.tourGuide.domain.NearByAttraction;
import com.tourGuide.domain.location.Attraction;
import com.tourGuide.domain.location.VisitedLocation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "gpsservice", url = "localhost:8082")
public interface GpsFeignClient {
    @GetMapping("/gps/getUserLocation")
    VisitedLocation getUserLocation(@RequestParam UUID uuid);

    @GetMapping("/gps/getAttractions")
    List<Attraction> getAttractions();

    @GetMapping("/gps/nearByAttractions")
    List<NearByAttraction> getNearByAttraction(@RequestParam VisitedLocation visitedLocation);
}
