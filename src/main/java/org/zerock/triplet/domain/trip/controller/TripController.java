package org.zerock.triplet.domain.trip.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.triplet.domain.trip.dto.CreateTripRequest;
import org.zerock.triplet.domain.trip.service.TripService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/gathers/{gatherId}/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createTrip(
            @PathVariable Long gatherId,
            @RequestPart("payload") @Validated CreateTripRequest payload,
            @RequestPart(value = "image", required = false)MultipartFile imageFile
            ) throws IOException{
        return ResponseEntity.ok(tripService.createTrip(gatherId,payload,imageFile));
    }
}
