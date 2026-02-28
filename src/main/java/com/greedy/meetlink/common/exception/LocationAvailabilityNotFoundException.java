package com.greedy.meetlink.common.exception;

import com.greedy.meetlink.common.ResponseCode;

public class LocationAvailabilityNotFoundException extends AppException {
    public LocationAvailabilityNotFoundException() {
        super(ResponseCode.LOCATION_NOT_SUBMITTED);
    }
}
