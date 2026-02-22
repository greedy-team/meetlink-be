package com.greedy.meetlink.place.algorithm;

import com.greedy.meetlink.place.algorithm.CandidateFilter.FilteredCandidate;
import com.greedy.meetlink.place.algorithm.CandidateFilter.ParticipantTravelTime;
import com.greedy.meetlink.place.algorithm.ScoreCalculator.ScoreResult;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 후보 좌표 점수 산정
 *
 * <p>✅ 리팩토링: 기존에 가중치(avg=0.4, max=0.4, stddev=0.2)를 이 클래스에서 직접 관리하던 방식에서 ScoreCalculator에 위임하는 방식으로
 * 변경.
 *
 * <p>[변경 이유] - CandidateScorer와 ScoreCalculator가 동일한 점수 계산 로직을 각자 다른 가중치로 갖고 있어 어떤 클래스가 실제 호출되느냐에
 * 따라 결과가 달라지는 잠재적 버그가 있었음. - 가중치는 ScoreCalculator 한 곳에서만 관리하도록 단일 책임 원칙(SRP) 적용.
 *
 * <p>score(P) = W_AVG × T_avg + W_MAX × T_max + W_STDDEV × T_stddev (가중치는 ScoreCalculator 참조) 점수가
 * 낮을수록 좋음 (이동시간 최소화 목표)
 */
@Component
@RequiredArgsConstructor
public class CandidateScorer {

    // ✅ 리팩토링: 직접 계산 대신 ScoreCalculator에 위임
    private final ScoreCalculator scoreCalculator;

    /**
     * 후보 목록을 점수 기준으로 정렬하여 반환 점수 낮은 순(이동시간 합이 최소) → rank 1이 가장 좋음
     *
     * @param candidates 2차 필터 통과 후보 목록
     * @param topK 반환할 상위 후보 수
     * @return 점수 + 순위가 계산된 후보 목록
     */
    public List<ScoredCandidate> score(List<FilteredCandidate> candidates, int topK) {
        List<ScoredCandidate> scored =
                candidates.stream()
                        .map(this::calculateScore)
                        .sorted(Comparator.comparingDouble(ScoredCandidate::score))
                        .limit(topK)
                        .collect(Collectors.toList());

        // 순위 부여 (1부터 시작)
        for (int i = 0; i < scored.size(); i++) {
            scored.set(i, scored.get(i).withRank(i + 1));
        }

        return scored;
    }

    /** ✅ 리팩토링: ScoreCalculator.calculate()에 위임하여 가중치 이원화 문제 해소 */
    private ScoredCandidate calculateScore(FilteredCandidate candidate) {
        List<Double> times =
                candidate.participantTravelTimes().stream()
                        .map(ParticipantTravelTime::travelTimeMinutes)
                        .collect(Collectors.toList());

        ScoreResult result = scoreCalculator.calculate(times);

        return new ScoredCandidate(
                candidate,
                result.avg(),
                result.max(),
                result.stddev(),
                result.score(),
                0 // rank는 score() 호출 후 withRank()로 부여
                );
    }

    /** 점수가 산정된 후보 */
    public record ScoredCandidate(
            FilteredCandidate filteredCandidate,
            double avgTravelTime,
            double maxTravelTime,
            double stddevTravelTime,
            double score,
            int rank) {
        public ScoredCandidate withRank(int rank) {
            return new ScoredCandidate(
                    filteredCandidate, avgTravelTime, maxTravelTime, stddevTravelTime, score, rank);
        }
    }
}
