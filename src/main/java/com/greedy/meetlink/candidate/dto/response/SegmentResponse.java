package com.greedy.meetlink.candidate.dto.response;

import com.greedy.meetlink.common.client.dto.SegmentInfo;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SegmentResponse {
    private final String mode;
    private final List<List<Double>> coordinates;

    public static SegmentResponse from(SegmentInfo segment) {
        return SegmentResponse.builder()
                .mode(segment.mode())
                .coordinates(segment.coordinates())
                .build();
    }
}
