package org.zerock.triplet.domain.trip.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.triplet.domain.trip.dto.CreateTripRequest;
import org.zerock.triplet.domain.trip.entity.Cost;
import org.zerock.triplet.domain.trip.entity.Trip;
import org.zerock.triplet.domain.trip.repository.CostRepository;
import org.zerock.triplet.domain.trip.repository.TripRepository;
import org.zerock.triplet.domain.trip.storage.ImageStorage;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Long.parseLong;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepo;
    private final CostRepository costRepo;
    private final ImageStorage imageStorage;

    @Transactional
    public Map<String, Object> createTrip(Long pathGatherId,
                                          CreateTripRequest req,
                                          @Nullable MultipartFile imageFile) throws IOException{

        // 숙박, 보험. 기본 값 파싱
        Long stay = parseLong(req.getBudgets().getStay());
        Long insurance = parseLong(req.getBudgets().getInsurance());

        // Trip 생성
        Trip trip = new Trip();
        trip.setName(req.getTitle());
        trip.setStartDate(Instant.ofEpochMilli(req.getStartMs()).atZone(ZoneId.systemDefault()).toLocalDateTime());
        trip.setEndDate(Instant.ofEpochMilli(req.getEndMs()).atZone(ZoneId.systemDefault()).toLocalDateTime());
        trip.setGatherId(pathGatherId);
        trip.setTheme(req.getTheme());
        trip.setStayCost(stay);
        trip.setInsuranceCost(insurance);

        String key;
        if(imageFile != null && !imageFile.isEmpty()) key = imageStorage.store(imageFile);
        else key = Optional.ofNullable(req.getPresetImage()).orElse("trip_cover1");
        trip.setTripImg(key);

        long daysSum = 0;
        List<Cost> rows = new ArrayList<>();
        if(req.getDays() != null){
            List<Integer> ordered = req.getDays().keySet().stream().map(Integer::valueOf).sorted().toList();
            for(Integer d : ordered){
                var plan = req.getDays().get(String.valueOf(d));
                boolean hasPlan = !Boolean.TRUE.equals(plan.getNoSchedule());

                long food = hasPlan ? parseLong(plan.getAmounts().getFood()) : 0;
                long transport = hasPlan ? parseLong(plan.getAmounts().getTransport()) : 0;
                long leisure = hasPlan ? parseLong(plan.getAmounts().getLeisure()) : 0;
                long etc = hasPlan ? parseLong(plan.getAmounts().getEtc()) : 0;
                daysSum += food + transport + leisure + etc;

                Cost c = new Cost();
                c.setTrip(trip);
                c.setDay(d);
                c.setFood(food);
                c.setTransport(transport);
                c.setLeisure(leisure);
                c.setEtc(etc);
                c.setCheckPlan(hasPlan);
                rows.add(c);
            }
        }
        trip.setTotalCost(stay + insurance + daysSum);

        Trip saved = tripRepo.save(trip);
        rows.forEach(r -> r.setTrip(saved));
        costRepo.saveAll(rows);

        String imageUrl = key.startsWith("trip_cover")
                ? "/assets/presets/" + key + ".png"
                : imageStorage.publicUrl(key);

        return Map.of("tripId", saved.getId(), "imageKey", key, "imageUrl", imageUrl);

    }
}
