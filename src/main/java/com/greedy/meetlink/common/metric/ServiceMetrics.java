package com.greedy.meetlink.common.metric;

import com.greedy.meetlink.availability.entity.TimeAvailabilityType;
import com.greedy.meetlink.candidate.service.PlaceCandidateService;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceMetrics implements MeterBinder {
    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    private final MeetingResultRepository meetingResultRepository;
    private final PlaceCandidateService placeCandidateService;

    @Override
    public void bindTo(MeterRegistry registry) {
        // 모임 관련 메트릭
        Gauge.builder("meetlink.meetings.count", meetingRepository, (r) -> (double) r.count())
                .description("Total number of meetings")
                .register(registry);

        Gauge.builder(
                        "meetlink.meetings.orphan",
                        meetingRepository,
                        (r) -> (double) r.countOrphanMeetings())
                .description("Meetings with 1 or fewer participants")
                .register(registry);

        Gauge.builder(
                        "meetlink.meetings.time_recommendation_enabled",
                        meetingRepository,
                        (r) -> (double) r.countByEnableTimeRecommendationTrue())
                .description("Meetings with time recommendation enabled")
                .register(registry);

        Gauge.builder(
                        "meetlink.meetings.place_recommendation_enabled",
                        meetingRepository,
                        (r) -> (double) r.countByEnablePlaceRecommendationTrue())
                .description("Meetings with place recommendation enabled")
                .register(registry);

        Gauge.builder(
                        "meetlink.meetings.availability_type",
                        meetingRepository,
                        (r) -> (double) r.countByTimeAvailabilityType(TimeAvailabilityType.WEEKLY))
                .description("Number of meetings by availability type")
                .tag("type", "weekly")
                .register(registry);

        Gauge.builder(
                        "meetlink.meetings.availability_type",
                        meetingRepository,
                        (r) ->
                                (double)
                                        r.countByTimeAvailabilityType(
                                                TimeAvailabilityType.SPECIFIC_DATE))
                .description("Number of meetings by availability type")
                .tag("type", "specific_date")
                .register(registry);

        Gauge.builder(
                        "meetlink.meetings.place_calculating",
                        placeCandidateService,
                        (s) -> (double) s.getCalculatingCount())
                .description("Meetings currently running the place recommendation algorithm")
                .register(registry);

        // 참여자 관련 메트릭
        Gauge.builder(
                        "meetlink.participants.count",
                        participantRepository,
                        (r) -> (double) r.count())
                .description("Total number of participants across all meetings")
                .register(registry);

        Gauge.builder(
                        "meetlink.participants.time_submitted",
                        participantRepository,
                        (r) -> (double) r.countByTimeSubmittedAtIsNotNull())
                .description("Participants who submitted time availability")
                .register(registry);

        Gauge.builder(
                        "meetlink.participants.location_submitted",
                        participantRepository,
                        (r) -> (double) r.countByLocationSubmittedAtIsNotNull())
                .description("Participants who submitted location availability")
                .register(registry);

        // 추천 관련 메트릭
        Gauge.builder(
                        "meetlink.meetings.time_confirmed",
                        meetingResultRepository,
                        (r) -> (double) r.countByTimeCandidateIsNotNull())
                .description("Meetings with a confirmed time recommendation")
                .register(registry);

        Gauge.builder(
                        "meetlink.meetings.place_confirmed",
                        meetingResultRepository,
                        (r) -> (double) r.countByPlaceCandidateIsNotNull())
                .description("Meetings with a confirmed place recommendation")
                .register(registry);
    }
}
