package com.greedy.meetlink.common.notification;

import com.greedy.meetlink.candidate.event.PlaceRecommendationReadyEvent;
import com.greedy.meetlink.candidate.event.TimeRecommendationReadyEvent;
import com.greedy.meetlink.meeting.event.MeetingUpdatedEvent;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.event.HostTransferredEvent;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
class PushNotificationEventListener {
    private final ParticipantRepository participantRepository;
    private final MeetingRepository meetingRepository;
    private final PushNotificationService pushNotificationService;

    @Async("candidateCalculationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onMeetingUpdated(MeetingUpdatedEvent event) {
        log.debug("MeetingUpdatedEvent: meeting={}", event.meetingCode());

        meetingRepository
                .findByCode(event.meetingCode())
                .ifPresent(
                        (meeting) -> {
                            List<String> tokens =
                                    participantRepository
                                            .findByMeetingAndIsHostFalseAndFcmTokenIsNotNull(
                                                    meeting)
                                            .stream()
                                            .map(Participant::getFcmToken)
                                            .toList();
                            pushNotificationService.send(tokens, "모임 설정 변경", "모임 설정이 변경되었어요");
                        });
    }

    @Async("candidateCalculationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onHostTransferred(HostTransferredEvent event) {
        log.debug(
                "HostTransferredEvent: meeting={}, newHost={}",
                event.meetingCode(),
                event.newHostNickname());

        meetingRepository
                .findByCode(event.meetingCode())
                .flatMap(participantRepository::findByMeetingAndIsHostTrue)
                .ifPresent(
                        (host) ->
                                pushNotificationService.send(
                                        host.getFcmToken(), "모임장 위임", "모임장이 되었어요"));
    }

    @Async("candidateCalculationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onPlaceRecommendationReady(PlaceRecommendationReadyEvent event) {
        log.debug("PlaceRecommendationReadyEvent: meeting={}", event.meetingCode());
        notifyAllWithToken(event.meetingCode(), "추천 장소 업데이트", "추천 장소가 업데이트되었어요");
    }

    @Async("candidateCalculationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onTimeRecommendationReady(TimeRecommendationReadyEvent event) {
        log.debug("TimeRecommendationReadyEvent: meeting={}", event.meetingCode());
        notifyAllWithToken(event.meetingCode(), "추천 시간 업데이트", "추천 시간이 업데이트되었어요");
    }

    private void notifyAllWithToken(String meetingCode, String title, String body) {
        meetingRepository
                .findByCode(meetingCode)
                .ifPresent(
                        (meeting) -> {
                            List<String> tokens =
                                    participantRepository
                                            .findByMeetingAndFcmTokenIsNotNull(meeting)
                                            .stream()
                                            .map(Participant::getFcmToken)
                                            .toList();
                            pushNotificationService.send(tokens, title, body);
                        });
    }
}
