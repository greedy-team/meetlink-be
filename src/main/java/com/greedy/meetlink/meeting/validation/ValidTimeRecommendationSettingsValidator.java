package com.greedy.meetlink.meeting.validation;

import com.greedy.meetlink.meeting.dto.MeetingCreateRequest;
import com.greedy.meetlink.meeting.dto.MeetingUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ValidTimeRecommendationSettings 어노테이션의 실제 검증 로직
 */
public class ValidTimeRecommendationSettingsValidator
        implements ConstraintValidator<ValidTimeRecommendationSettings, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Boolean enableTimeRecommendation;
        Object timeAvailabilityType;
        Object timeRangeStart;
        Object timeRangeEnd;

        // MeetingCreateRequest와 MeetingUpdateRequest 모두 지원
        if (value instanceof MeetingCreateRequest request) {
            enableTimeRecommendation = request.getEnableTimeRecommendation();
            timeAvailabilityType = request.getTimeAvailabilityType();
            timeRangeStart = request.getTimeRangeStart();
            timeRangeEnd = request.getTimeRangeEnd();
        } else if (value instanceof MeetingUpdateRequest request) {
            enableTimeRecommendation = request.getEnableTimeRecommendation();
            timeAvailabilityType = request.getTimeAvailabilityType();
            timeRangeStart = request.getTimeRangeStart();
            timeRangeEnd = request.getTimeRangeEnd();
        } else {
            return true;
        }

        // 시간 추천을 사용하지 않으면 검증 통과
        if (!Boolean.TRUE.equals(enableTimeRecommendation)) {
            return true;
        }

        // 시간 추천을 사용하는 경우 필수 필드 검증
        context.disableDefaultConstraintViolation();

        boolean isValid = true;

        if (timeAvailabilityType == null) {
            context.buildConstraintViolationWithTemplate("시간 추천을 사용하는 경우 시간 입력 유형은 필수입니다.")
                    .addPropertyNode("timeAvailabilityType")
                    .addConstraintViolation();
            isValid = false;
        }

        if (timeRangeStart == null) {
            context.buildConstraintViolationWithTemplate("시간 추천을 사용하는 경우 시작 시간은 필수입니다.")
                    .addPropertyNode("timeRangeStart")
                    .addConstraintViolation();
            isValid = false;
        }

        if (timeRangeEnd == null) {
            context.buildConstraintViolationWithTemplate("시간 추천을 사용하는 경우 종료 시간은 필수입니다.")
                    .addPropertyNode("timeRangeEnd")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
