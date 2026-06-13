package com.rice.trade.controller;

import com.rice.trade.dto.LocationAddressResponse;
import com.rice.trade.service.LocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/reverse-geocode")
    public LocationAddressResponse reverseGeocode(
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        return locationService.reverseGeocode(latitude, longitude);
    }
}
