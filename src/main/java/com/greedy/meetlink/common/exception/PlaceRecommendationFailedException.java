package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class PlaceRecommendationFailedException extends AppException {
    public PlaceRecommendationFailedException() {
        super(ResponseCode.PLACE_RECOMMENDATION_FAILED);
    }
}
