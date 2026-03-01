package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class PlaceCandidateCalculationFailedException extends AppException {
    public PlaceCandidateCalculationFailedException() {
        super(ResponseCode.PLACE_CANDIDATE_CALCULATION_FAILED);
    }
}
