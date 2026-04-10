package com.thv.sport.system.service.impl;

import com.thv.sport.system.common.Constants;
import com.thv.sport.system.dto.request.court.CourtCenterImageRequest;
import com.thv.sport.system.dto.request.court.CourtCenterRegisterRequest;
import com.thv.sport.system.dto.request.court.CourtRequest;
import com.thv.sport.system.dto.request.court.CourtSlotRequest;
import com.thv.sport.system.dto.request.court.PricingRuleRequest;
import com.thv.sport.system.dto.response.courtcenter.CourtCenterResponse;
import com.thv.sport.system.model.Court;
import com.thv.sport.system.model.CourtCenter;
import com.thv.sport.system.model.CourtCenterImage;
import com.thv.sport.system.model.CourtSlot;
import com.thv.sport.system.model.PricingRule;
import com.thv.sport.system.respository.CourtCenterImageRepository;
import com.thv.sport.system.respository.CourtCenterRepository;
import com.thv.sport.system.service.CourtCenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtCenterServiceImpl implements CourtCenterService {
    private final CourtCenterRepository courtCenterRepository;
    private final CourtCenterImageRepository courtCenterImageRepository;

    @Transactional
    @Override
    public CourtCenter registerCourt(CourtCenterRegisterRequest request) {
        List<Court> courts = new ArrayList<>();
        List<CourtSlot> courtSlots = new ArrayList<>();
        List<PricingRule> pricingRules = new ArrayList<>();
        List<CourtCenterImage> courtCenterImages = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        //step 1: create court center
        CourtCenter courtCenter = CourtCenter.builder()
                .name(request.getName())
                .locationDetail(request.getLocationDetail())
                .phoneNumber(request.getPhoneNumber())
                .deleted(Constants.DeleteFlag.FALSE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        courtCenterRepository.save(courtCenter);

        //step 2: create new court
        for (CourtRequest courtRequest : request.getCourts()) {
            Court court = Court.builder()
                    .name(courtRequest.getName())
                    .type(courtRequest.getType())
                    .status(Constants.CourtStatus.ACTIVE)
                    .courtCenter(courtCenter)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            courts.add(court);
        }

        Map<Integer, CourtSlotRequest> courtSlotMap = request.getSlots()
                .stream()
                .collect(Collectors.toMap(CourtSlotRequest::getDayOfWeek,
                        Function.identity())
                );

        if (request.getSlots().size() != courtSlotMap.size()) {
            throw new RuntimeException("court.slot.duplicate.day.of.week");
        }

        //step 3: create slot for days of week
        for (int i = 2; i <= 8; i++) {
            CourtSlotRequest courtSlotRequest = courtSlotMap.get(i);

            LocalTime startTime = (courtSlotRequest != null && !ObjectUtils.isEmpty(courtSlotRequest.getStartTime()))
                    ? courtSlotRequest.getStartTime()
                    : Constants.CourtTimeSlotDefault.START_TIME;

            LocalTime endTime = (courtSlotRequest != null && !ObjectUtils.isEmpty(courtSlotRequest.getEndTime()))
                    ? courtSlotRequest.getEndTime()
                    : Constants.CourtTimeSlotDefault.END_TIME;

            if (startTime.isAfter(endTime)
                    || startTime.equals(endTime)) {
                throw new RuntimeException("court.slot.invalid.time");
            }

            CourtSlot courtSlot = CourtSlot.builder()
                    .dayOfWeek(i)
                    .startTime(startTime)
                    .endTime(endTime)
                    .status(Constants.CourtStatus.ACTIVE)
                    .courtCenter(courtCenter)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            courtSlots.add(courtSlot);
        }

        //step 4: create pricing rule for each day of week

        //4.1 group rule by day
        Map<Integer, List<PricingRuleRequest>> ruleByDay = request.getPricingRules()
                .stream()
                //groupingBy allow duplicate key (group value with the same key)
                .collect(Collectors.groupingBy(PricingRuleRequest::getDayOfWeek));

        //4.2 get court slot by day
        Map<Integer, CourtSlot> slotMap = courtSlots.stream()
                .collect(Collectors.toMap(CourtSlot::getDayOfWeek, Function.identity()));

        //4.3 validate each day
        for (Map.Entry<Integer, List<PricingRuleRequest>> entry : ruleByDay.entrySet()) {
            //get day and rule of day
            Integer day = entry.getKey();
            List<PricingRuleRequest> rules = entry.getValue();

            CourtSlot slot = slotMap.get(day);

            if (ObjectUtils.isEmpty(slot)) {
                throw new RuntimeException("court.slot.not.existed");
            }

            //sort by startTime
            rules.sort(Comparator.comparing(PricingRuleRequest::getStartTime));

            LocalTime expectedStart = slot.getStartTime();

            for (PricingRuleRequest r : rules) {
                //first start time of rule was equal start time of slot
                if (!r.getStartTime().equals(expectedStart)) {
                    throw new RuntimeException("pricing.rule.invalid.start.time");
                }
                //validate if start >= end
                if (!r.getStartTime().isBefore(r.getEndTime())) {
                    throw new RuntimeException("pricing.rule.invalid.time");
                }

                expectedStart = r.getEndTime();
            }
            //last end time of rule was equal end time of slot
            if (!expectedStart.equals(slot.getEndTime())) {
                throw new RuntimeException("pricing.rule.not.cover.full.slot");
            }
        }

        //4.4 create rule
        for (PricingRuleRequest pricingRuleRequest : request.getPricingRules()) {
            PricingRule pricingRule = PricingRule.builder()
                    .dayOfWeek(pricingRuleRequest.getDayOfWeek())
                    .specificDate(pricingRuleRequest.getSpecificDate())
                    .startTime(pricingRuleRequest.getStartTime())
                    .endTime(pricingRuleRequest.getEndTime())
                    .pricePerHour(pricingRuleRequest.getPricePerHour())
                    .ruleType(pricingRuleRequest.getRuleType())
                    .priority(pricingRuleRequest.getPriority())
                    .active(Constants.ActiveStatus.TRUE_VALUE)
                    .courtCenter(courtCenter)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            pricingRules.add(pricingRule);
        }

        for (CourtCenterImageRequest imageRequest : request.getImages()) {
            CourtCenterImage courtCenterImage = CourtCenterImage.builder()
                    .imageUrl(imageRequest.getImageUrl())
                    .courtCenter(courtCenter)
                    .createdAt(now)
                    .isThumbnail(imageRequest.getIsThumbnail())
                    .build();
            courtCenterImages.add(courtCenterImage);
        }

        courtCenter.setCourts(courts);
        courtCenter.setSlots(courtSlots);
        courtCenter.setPricingRules(pricingRules);
        courtCenter.setImages(courtCenterImages);

        return courtCenterRepository.save(courtCenter);
    }

    @Transactional
    @Override
    public CourtCenter updateCourt(CourtCenterRegisterRequest request, Long courtCenterId) {

        List<CourtSlot> courtSlots = new ArrayList<>();

        CourtCenter courtCenter = courtCenterRepository.findById(courtCenterId)
                .orElseThrow(() -> new RuntimeException("court.center.not.existed"));

        LocalDateTime now = LocalDateTime.now();

        // Step 1: update court center
        courtCenter.setName(request.getName());
        courtCenter.setLocationDetail(request.getLocationDetail());
        courtCenter.setPhoneNumber(request.getPhoneNumber());
        courtCenter.setUpdatedAt(now);

        //Step 2: update court
        Map<Long, Court> existingCourtMap = courtCenter.getCourts()
                .stream()
                .collect(Collectors.toMap(Court::getCourtId, court -> court));

//        List<Court> newCourt = new ArrayList<>();

        List<Court> existingCourts = courtCenter.getCourts();

        Set<Long> requestCourtIds = request.getCourts().stream()
                .map(CourtRequest::getCourtId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Court existingCourt : existingCourts) {
            if (existingCourt.getCourtId() != null && !requestCourtIds.contains(existingCourt.getCourtId())) {
                existingCourt.setStatus(Constants.CourtStatus.INACTIVE);
                existingCourt.setUpdatedAt(now);
            }
        }

        //===============
        //ADD LOGIC BOOKING LATER
        //===============
        for (CourtRequest courtRequest : request.getCourts()) {
            //case update
            if (!ObjectUtils.isEmpty(courtRequest.getCourtId())) {
                Court existingCourt = existingCourtMap.get(courtRequest.getCourtId());

                if (ObjectUtils.isEmpty(existingCourt)) {
                    throw new RuntimeException("court.not.existed");
                }

                existingCourt.setName(courtRequest.getName());
                existingCourt.setType(courtRequest.getType());
                existingCourt.setStatus(courtRequest.getStatus());
                existingCourt.setCourtCenter(courtCenter);
                existingCourt.setUpdatedAt(now);

            }
            //case create
            else {
                Court court = Court.builder()
                        .name(courtRequest.getName())
                        .type(courtRequest.getType())
                        .status(Constants.CourtStatus.ACTIVE)
                        .courtCenter(courtCenter)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                existingCourts.add(court);
            }
        }

        //===============
        //ADD LOGIC BOOKING LATER
        //===============
        //Step 3: Update court slot

        if (request.getSlots().size() != 7) {
            throw new RuntimeException("court.slot.must.have.7.days");
        }

        Map<Integer, CourtSlot> existingSlotMap =
                courtCenter.getSlots()
                        .stream()
                        .collect(Collectors.toMap(CourtSlot::getDayOfWeek, slot -> slot));

        //check duplicate day of week in request
        Set<Integer> dayOfWeekSet = new HashSet<>();

        for (CourtSlotRequest requestSlot : request.getSlots()) {
            //method add in Set
            //return true if item not existed in set
            //return false if item was existed in set
            if (!dayOfWeekSet.add(requestSlot.getDayOfWeek())) {
                throw new RuntimeException("court.slot.duplicate.day.of.week");
            }
        }

        List<CourtSlot> updatedSlots = new ArrayList<>();

        for (CourtSlotRequest slotRequest : request.getSlots()) {

            CourtSlot courtSlot = existingSlotMap.get(slotRequest.getDayOfWeek());

            int dayOfWeek = slotRequest.getDayOfWeek();
            if (dayOfWeek < 2 || dayOfWeek > 8) {
                throw new RuntimeException("court.slot.invalid.day.of.week");
            }

            if (ObjectUtils.isEmpty(courtSlot)) {
                throw new RuntimeException("court.slot.not.existed");
            }

            LocalTime startTime = !ObjectUtils.isEmpty(slotRequest)
                    && !ObjectUtils.isEmpty(slotRequest.getStartTime()) ? slotRequest.getStartTime() :
                    Constants.CourtTimeSlotDefault.START_TIME;

            LocalTime endTime = !ObjectUtils.isEmpty(slotRequest)
                    && !ObjectUtils.isEmpty(slotRequest.getEndTime()) ? slotRequest.getEndTime() :
                    Constants.CourtTimeSlotDefault.END_TIME;

            if (!startTime.isBefore(endTime)) {
                throw new RuntimeException("court.slot.invalid.time");
            }

            courtSlot.setStartTime(startTime);
            courtSlot.setEndTime(endTime);
            courtSlot.setUpdatedAt(now);
            courtSlot.setCourtCenter(courtCenter);
            courtSlot.setStatus(slotRequest.getStatus());

            updatedSlots.add(courtSlot);
        }

        //step 4: Update Pricing Rule

        //group rule by day
        Map<Integer, List<PricingRuleRequest>> ruleMap = request.getPricingRules()
                .stream()
                .collect(Collectors.groupingBy(PricingRuleRequest::getDayOfWeek));

        //get court slot by day
        Map<Integer, CourtSlot> courtSlotMap = updatedSlots.stream()
                .collect(Collectors.toMap(CourtSlot::getDayOfWeek,
                        Function.identity()));

        for (Map.Entry<Integer, List<PricingRuleRequest>> entry : ruleMap.entrySet()) {
            Integer dayOfWeek = entry.getKey();
            List<PricingRuleRequest> ruleRequests = entry.getValue();

            CourtSlot courtSlot = courtSlotMap.get(dayOfWeek);

            if (ObjectUtils.isEmpty(courtSlot)) {
                throw new RuntimeException("court.slot.not.existed");
            }

            ruleRequests.sort(Comparator.comparing(PricingRuleRequest::getStartTime));
            LocalTime expectedStart = courtSlot.getStartTime();

            for (PricingRuleRequest r : ruleRequests) {

                //check if user change slot time
                if (r.getStartTime().isBefore(courtSlot.getStartTime()) ||
                        r.getEndTime().isAfter(courtSlot.getEndTime())) {
                    throw new RuntimeException("pricing.rule.out.of.slot.range");
                }

                //first start time of rule was equal start time of slot
                if (!r.getStartTime().equals(expectedStart)) {
                    throw new RuntimeException("pricing.rule.invalid.start.time");
                }
                //validate if start >= end
                if (!r.getStartTime().isBefore(r.getEndTime())) {
                    throw new RuntimeException("pricing.rule.invalid.time");
                }

                expectedStart = r.getEndTime();
            }
            //last end time of rule was equal end time of slot
            if (!expectedStart.equals(courtSlot.getEndTime())) {
                throw new RuntimeException("pricing.rule.not.cover.full.slot");
            }
        }

        List<PricingRule> existingRules = courtCenter.getPricingRules();

        //get rule was existed in system
        Map<Long, PricingRule> existingRuleMap =
                courtCenter.getPricingRules()
                        .stream()
                        .collect(Collectors.toMap(PricingRule::getId, Function.identity()));

        Set<Long> requestRuleIds = request.getPricingRules().stream()
                .map(PricingRuleRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (PricingRule existingRule : existingRules) {
            if (existingRule.getId() != null && !requestRuleIds.contains(existingRule.getId())) {
                existingRule.setActive(Constants.ActiveStatus.FALSE_VALUE);
                existingRule.setUpdatedAt(now);
            }
        }

        List<PricingRule> newRules = new ArrayList<>();

        for (PricingRuleRequest req : request.getPricingRules()) {
            //case update
            if (!ObjectUtils.isEmpty(req.getId())) {
                PricingRule existing = existingRuleMap.get(req.getId());
                if (ObjectUtils.isEmpty(existing)) {
                    throw new RuntimeException("pricing.rule.not.existed");
                }
                existing.setDayOfWeek(req.getDayOfWeek());
                existing.setStartTime(req.getStartTime());
                existing.setEndTime(req.getEndTime());
                existing.setPricePerHour(req.getPricePerHour());
                existing.setPriority(req.getPriority());
                existing.setActive(req.getActive());
                existing.setUpdatedAt(now);
                existing.setCourtCenter(courtCenter);

            }
            //case create
            else {
                PricingRule newRule = PricingRule.builder()
                        .dayOfWeek(req.getDayOfWeek())
                        .specificDate(req.getSpecificDate())
                        .startTime(req.getStartTime())
                        .endTime(req.getEndTime())
                        .pricePerHour(req.getPricePerHour())
                        .ruleType(req.getRuleType())
                        .priority(req.getPriority())
                        .active(req.getActive())
                        .courtCenter(courtCenter)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

                existingRules.add(newRule);
            }
        }

        //step 5: update image

        List<CourtCenterImage> existingImages = courtCenter.getImages();

        Map<Long, CourtCenterImage> existingImageMap = courtCenter.getImages()
                .stream()
                .collect(Collectors.toMap(CourtCenterImage::getImageId, Function.identity()));

        Set<Long> requestImageIds = request.getImages().stream()
                .map(CourtCenterImageRequest::getImageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        existingImages.removeIf(image ->
                image.getImageId() != null && !requestImageIds.contains(image.getImageId())
        );

        for (CourtCenterImageRequest imageRequest : request.getImages()) {
            if (!ObjectUtils.isEmpty(imageRequest.getImageId())) {
                CourtCenterImage existing = existingImageMap.get(imageRequest.getImageId());
                if (ObjectUtils.isEmpty(existing)) {
                    throw new RuntimeException("court.center.image.not.existed");
                }
                existing.setImageUrl(imageRequest.getImageUrl());
                existing.setIsThumbnail(imageRequest.getIsThumbnail());
                existing.setCourtCenter(courtCenter);

            } else {
                CourtCenterImage courtCenterImage = CourtCenterImage.builder()
                        .imageUrl(imageRequest.getImageUrl())
                        .courtCenter(courtCenter)
                        .isThumbnail(imageRequest.getIsThumbnail())
                        .build();
                existingImages.add(courtCenterImage);
            }
        }

        return courtCenterRepository.save(courtCenter);
    }

    @Override
    public CourtCenter getCourtDetails(Long courtCenterId) {
        return courtCenterRepository.findById(courtCenterId)
                .orElseThrow(() -> new RuntimeException("court.center.not.existed"));
    }

    @Override
    public List<CourtCenterResponse> search() {
        List<CourtCenter> courtCenters = courtCenterRepository.findAll();
        List<CourtCenterResponse> courtCenterResponses = new ArrayList<>();

        List<Long> courtCenterIds = courtCenters.stream()
                .map(CourtCenter::getCourtCenterId)
                .toList();

        List<CourtCenterImage> courtCenterImages = courtCenterImageRepository.findListImgByCourCenterId(courtCenterIds);

        Map<Long, List<CourtCenterImage>> courtCenterImageMap = courtCenterImages.stream()
                .collect(Collectors.groupingBy(image -> image.getCourtCenter().getCourtCenterId()));

        if (!ObjectUtils.isEmpty(courtCenterIds)) {

            for (CourtCenter courtCenter : courtCenters) {

                List<CourtCenterImage> imgList = courtCenterImageMap.get(courtCenter.getCourtCenterId());

                if (ObjectUtils.isEmpty(imgList)) {
                    continue;
                }

                String thumbnailUrl = "";
                
                for (CourtCenterImage image : imgList) {
                    if (ObjectUtils.isEmpty(image.getIsThumbnail())) {
                        continue;
                    }
                    if (image.getIsThumbnail()) {
                        thumbnailUrl = image.getImageUrl();
                    }
                }

                CourtCenterResponse courtCenterResponse = CourtCenterResponse.builder()
                        .courtCenterId(courtCenter.getCourtCenterId())
                        .name(courtCenter.getName())
                        .locationDetail(courtCenter.getLocationDetail())
                        .phoneNumber(courtCenter.getPhoneNumber())
                        .imgUrl(thumbnailUrl)
                        .deleted(courtCenter.getDeleted())
                        .createdAt(courtCenter.getCreatedAt())
                        .build();

                if (!courtCenterResponse.getDeleted().equals(Constants.DeleteFlag.FALSE)) {
                    continue;
                }

                courtCenterResponses.add(courtCenterResponse);
            }
        }
        return courtCenterResponses;
    }

    @Override
    public void deleteCourtCenter(List<Long> ids) {

    }
}
