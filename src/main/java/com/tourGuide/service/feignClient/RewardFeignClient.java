package com.tourGuide.service.feignClient;

import com.tourGuide.domain.User;
import com.tourGuide.domain.UserAndReward;
import com.tourGuide.domain.UserReward;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "rewardservice", url = "localhost:8081")
public interface RewardFeignClient {

    @RequestMapping("/calculateRewards")
    List<UserReward> calculateRewards(@RequestBody UserAndReward user);
}
