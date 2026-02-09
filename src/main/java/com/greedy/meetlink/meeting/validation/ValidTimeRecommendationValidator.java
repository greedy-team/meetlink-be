package com.greedy.meetlink.meeting.validation;

import com.greedy.meetlink.common.validation.TimeRecommendationProvider;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidTimeRecommendationValidator
        implements ConstraintValidator<ValidTimeRecommendation, TimeRecommendationProvider> {

    @Override
    public boolean isValid(TimeRecommendationProvider value, ConstraintValidatorContext context) {
        if (value == null || !Boolean.TRUE.equals(value.getEnableTimeRecommendation())) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        boolean isValid = true;

        if (value.getTimeAvailabilityType() == null) {
            addViolation(context, "시간 입력 유형은 필수입니다.", "timeAvailabilityType");
            isValid = false;
        }

        if (value.getTimeRangeStart() == null) {
            addViolation(context, "시작 시간은 필수입니다.", "timeRangeStart");
            isValid = false;
        }

        if (value.getTimeRangeEnd() == null) {
            addViolation(context, "종료 시간은 필수입니다.", "timeRangeEnd");
            isValid = false;
        }

        if (!isValid) {
            log.warn(
                    "Time recommendation settings validation failed for object: {}",
                    value.getClass().getSimpleName());
        }

        return isValid;
    }

    private void addViolation(ConstraintValidatorContext context, String message, String property) {
        context.buildConstraintViolationWithTemplate("시간 추천을 사용하는 경우 " + message)
                .addPropertyNode(property)
                .addConstraintViolation();
    }
}
