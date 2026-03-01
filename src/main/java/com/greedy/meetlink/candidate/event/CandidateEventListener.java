package com.greedy.meetlink.candidate.event;

import com.greedy.meetlink.candidate.service.PlaceCandidateService;
import com.greedy.meetlink.candidate.service.TimeCandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
class CandidateEventListener {
    private final TimeCandidateService timeCandidateService;
    private final PlaceCandidateService placeCandidateService;

    @Async("candidateCalculationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onTimeSubmitted(TimeAvailabilitySubmittedEvent e) {
        log.info("TimeAvailabilitySubmittedEvent received: meeting={}", e.meetingCode());
        timeCandidateService.calculateTimeCandidates(e.meetingCode());
    }

    @Async("candidateCalculationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onLocationSubmitted(LocationAvailabilitySubmittedEvent e) {
        log.info("LocationAvailabilitySubmittedEvent received: meeting={}", e.meetingCode());
        placeCandidateService.calculatePlaceCandidates(e.meetingCode());
    }
}
