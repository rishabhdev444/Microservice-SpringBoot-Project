package com.hotel.user.service.controller;

import com.hotel.user.service.entities.User;
import com.hotel.user.service.service.UserService;
import com.hotel.user.service.service.impl.UserServiceImpl;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    private static final Logger LOGGER= LoggerFactory.getLogger(UserController.class);
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        User user1=userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user1);
    }
//    int retryCount=1; //to check the retyrCount after the rating service is down
    @GetMapping("/{userId}")
//    @CircuitBreaker(name="ratingHotelBreaker",fallbackMethod = "ratingHotelFallback")  //is used when a service is down
//    @Retry(name="ratingHotelService",fallbackMethod = "ratingHotelFallback")  // Used for to check the service is up or not, or it is slow
//    @RateLimiter(name="userRateLimiter",fallbackMethod = "ratingHotelFallback")
    public ResponseEntity<User> getUser(@PathVariable String userId){
        LOGGER.info("Get Single User Handler: UserController");
//        LOGGER.info("Retry Count : {}",retryCount);  //RetryCount
//        retryCount++;                                //For Retry Count
        User user=userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    //creating fallback method for circuit breaker and also for Retry
    public ResponseEntity<User> ratingHotelFallback(String userId,Exception ex){
//        LOGGER.info("FallBack is executed because service is down: ",ex.getMessage());

        User user=User.builder()
                .email("dummy@gmail.com")
                .name("Dummy")
                .about("Some service is down thats why this Dummy user is created")
                .userId("124")
                .build();
        return new ResponseEntity<>(user,HttpStatus.OK);
    }
    @GetMapping
    public ResponseEntity<List<User>> getAllUser(){
        List<User> allUser=userService.getAllUser();
        return ResponseEntity.ok(allUser);
    }
}
