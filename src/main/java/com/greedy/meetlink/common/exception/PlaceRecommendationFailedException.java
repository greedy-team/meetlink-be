package com.greedy.meetlink.common.exception;

public class PlaceRecommendationFailedException extends RuntimeException {
    public PlaceRecommendationFailedException() {
        super("추천 가능한 장소가 없습니다.");
    }
}
