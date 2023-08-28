package com.anand.user.service.services.impl;

import com.anand.user.service.entities.Hotel;
import com.anand.user.service.entities.Rating;
import com.anand.user.service.entities.User;
import com.anand.user.service.exceptions.ResourceNotFoundException;
//import com.anand.user.service.external.services.HotelService;
import com.anand.user.service.external.services.HotelService;
import com.anand.user.service.repositories.UserRepository;
import com.anand.user.service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public User saveUser(User user) {
        //generate  unique userid
        String randomUserId = UUID.randomUUID().toString();
        user.setUserId(randomUserId);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        //implement RATING SERVICE CALL: USING REST TEMPLATE
        return userRepository.findAll();
    }

    //get single user
    @Override
    public User getUser(String userId) {
        //get user from database with the help  of user repository
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with given id is not found on server !! : " + userId));
        // fetch rating of the above  user from RATING SERVICE
        //http://localhost:8083/ratings/users/47e38dac-c7d0-4c40-8582-11d15f185fad

//        ArrayList<Rating> ratingsOfUser = restTemplate.getForObject("http://localhost:8083/ratings/users/" + user.getUserId(), ArrayList.class);
//        Rating[] ratingsOfUser = restTemplate.getForObject("http://localhost:8083/ratings/users/" + user.getUserId(), Rating[].class);
        Rating[] ratingsOfUser = restTemplate.getForObject("http://RATING-SERVICE/ratings/users/" + user.getUserId(), Rating[].class);
        logger.info("{} ", ratingsOfUser);
        List<Rating> ratings = Arrays.stream(ratingsOfUser).toList();

//        List<Rating> ratingList = ratingsOfUser.stream().map(rating -> {
        List<Rating> ratingList = ratings.stream().map(rating -> {

            //api call to hotel service to get the hotel
            //http://localhost:8082/hotels/1cbaf36d-0b28-417    3-b5ea-f1cb0bc0a791
//            ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://localhost:8082/hotels/"+rating.getHotelId(), Hotel.class);
//            ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/"+rating.getHotelId(), Hotel.class);
//            Hotel hotel = forEntity.getBody();

            Hotel hotel = hotelService.getHotel(rating.getHotelId());
//             logger.info("response status code: {} ",forEntity.getStatusCode());

             //set the hotel to rating
            rating.setHotel(hotel);

            //return the rating
            return rating;
        }).collect(Collectors.toList());

//        user.setRatings(ratingsOfUser);
        user.setRatings(ratingList);

        return user;
    }
}
