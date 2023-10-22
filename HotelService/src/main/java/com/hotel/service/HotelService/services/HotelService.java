package com.hotel.service.HotelService.services;

import com.hotel.service.HotelService.entity.Hotel;

import java.util.List;

public interface HotelService {

    Hotel createHotel(Hotel hotel);
    List<Hotel> getAllHotel();
    Hotel getHotel(String id);
}
