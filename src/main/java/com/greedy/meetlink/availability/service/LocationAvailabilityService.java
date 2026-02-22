package com.greedy.meetlink.availability.service;

import com.greedy.meetlink.availability.dto.request.LocationAvailabilityRequest;
import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.availability.repository.LocationAvailabilityRepository;
import com.greedy.meetlink.common.validation.ParticipantValidator;
import com.greedy.meetlink.participant.entity.Participant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationAvailabilityService {
    private final LocationAvailabilityRepository locationAvailabilityRepository;
    private final ParticipantValidator participantValidator;

    @Transactional
    public void submit(String meetingCode, String token, LocationAvailabilityRequest request) {
        Participant participant =
                participantValidator.validateAndGetParticipant(meetingCode, token);

        // 기존 데이터가 있으면 update, 없으면 insert
        LocationAvailability location =
                locationAvailabilityRepository
                        .findByParticipant(participant)
                        .map(
                                (existing) -> {
                                    existing.update(
                                            request.getAddress(),
                                            request.getLatitude(),
                                            request.getLongitude());
                                    return existing;
                                })
                        .orElseGet(
                                () ->
                                        LocationAvailability.create(
                                                participant,
                                                request.getAddress(),
                                                request.getLatitude(),
                                                request.getLongitude()));

        locationAvailabilityRepository.save(location);

        // 장소 제출 여부 체크
        participant.markLocationSubmitted();
    }
}
