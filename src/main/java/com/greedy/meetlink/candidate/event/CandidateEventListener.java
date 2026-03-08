package com.greedy.meetlink.candidate.event;

import com.greedy.meetlink.candidate.service.PlaceCandidateService;
import com.greedy.meetlink.candidate.service.TimeCandidateService;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
class CandidateEventListener {
    private static final long DEBOUNCE_DELAY_SECONDS = 3;

    private final TimeCandidateService timeCandidateService;
    private final PlaceCandidateService placeCandidateService;
    private final TaskScheduler locationCandidateScheduler;

    private final ConcurrentHashMap<String, ScheduledFuture<?>> pendingLocationTasks =
            new ConcurrentHashMap<>();

    @Async("candidateCalculationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onTimeSubmitted(TimeAvailabilitySubmittedEvent e) {
        log.info("TimeAvailabilitySubmittedEvent received: meeting={}", e.meetingCode());
        timeCandidateService.calculateTimeCandidates(e.meetingCode());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onLocationSubmitted(LocationAvailabilitySubmittedEvent e) {
        log.info("LocationAvailabilitySubmittedEvent received: meeting={}", e.meetingCode());
        pendingLocationTasks.compute(
                e.meetingCode(),
                (key, existing) -> {
                    if (existing != null && !existing.isDone()) {
                        existing.cancel(false);
                        log.debug("Location calculation debounced: meeting={}", key);
                    }
                    return locationCandidateScheduler.schedule(
                            () -> {
                                pendingLocationTasks.remove(key);
                                placeCandidateService.calculatePlaceCandidates(key);
                            },
                            Instant.now().plusSeconds(DEBOUNCE_DELAY_SECONDS));
                });
    }

    @Async("candidateCalculationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onParticipantLeft(ParticipantLeftEvent e) {
        log.info("ParticipantLeftEvent received: meeting={}", e.meetingCode());
        placeCandidateService.recalculateOnLeave(e.meetingCode());
    }
}
