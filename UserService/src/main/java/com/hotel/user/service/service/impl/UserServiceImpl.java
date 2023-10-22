package com.hotel.user.service.service.impl;

import com.hotel.user.service.entities.Hotel;
import com.hotel.user.service.entities.Rating;
import com.hotel.user.service.entities.User;
import com.hotel.user.service.exception.ResourceNotFoundException;
import com.hotel.user.service.external.service.HotelService;
import com.hotel.user.service.repository.UserRepository;
import com.hotel.user.service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
   @Autowired
    private UserRepository userRepository;
   @Autowired
   private RestTemplate restTemplate;
   @Autowired
   private HotelService hotelService;

   private static final Logger LOGGER= LoggerFactory.getLogger(UserServiceImpl.class);
    @Override
    public User saveUser(User user) {
        String randomUserId=UUID.randomUUID().toString();
        user.setUserId(randomUserId);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        List<User> user=userRepository.findAll();
        user.stream().map(user1->{
            Rating[] ratingOfUser=restTemplate.getForObject("http://RATING-SERVICE/ratings/users/"+user1.getUserId(), Rating[].class);

            List<Rating> ratings=Arrays.stream(ratingOfUser).toList();
            List<Rating> ratingList=ratings.stream().map(rating->{
                ResponseEntity<Hotel> forEntity=restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/"+rating.getHotelId(), Hotel.class);
                Hotel hotel=forEntity.getBody();
                LOGGER.info(String.format("Hotel Response Status Code -> %s",forEntity.getStatusCode()));

                rating.setHotel(hotel);
                return rating;}).collect(Collectors.toList());
            user1.setRatings(ratingList);
            return user1;
        }).collect(Collectors.toList());

        return user;
    }

    //Using Rest Template

//    @Override
//    public User getUser(String userId) {
//        User user= userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User with given id is not valid!! : "+userId));
//        Rating[] ratingOfUser=restTemplate.getForObject("http://RATING-SERVICE/ratings/users/"+user.getUserId(), Rating[].class);
//
//        List<Rating> ratings=Arrays.stream(ratingOfUser).toList();
//        LOGGER.info("{}",ratings);
//        List<Rating> ratingList=ratings.stream().map(rating->{
//            ResponseEntity<Hotel> forEntity=restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/"+rating.getHotelId(), Hotel.class);
//            Hotel hotel=forEntity.getBody();
//            LOGGER.info(String.format("Hotel Response Status Code -> %s",forEntity.getStatusCode()));
//
//            rating.setHotel(hotel);
//            return rating;}).collect(Collectors.toList());
//        user.setRatings(ratingList);
//        return user;
//    }


    //Using FeignClient
    @Override
    public User getUser(String userId) {
        User user= userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User with given id is not valid!! : "+userId));
        Rating[] ratingOfUser=restTemplate.getForObject("http://RATING-SERVICE/ratings/users/"+user.getUserId(), Rating[].class);

        List<Rating> ratings=Arrays.stream(ratingOfUser).toList();
        LOGGER.info("{}",ratings);
        List<Rating> ratingList=ratings.stream().map(rating->{
//            ResponseEntity<Hotel> forEntity=restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/"+rating.getHotelId(), Hotel.class);
            Hotel hotel=hotelService.getHotel(rating.getHotelId());
            rating.setHotel(hotel);
            return rating;}).collect(Collectors.toList());
        user.setRatings(ratingList);
        return user;
    }
}
