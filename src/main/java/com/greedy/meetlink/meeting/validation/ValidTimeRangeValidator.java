package com.greedy.meetlink.meeting.validation;

import com.greedy.meetlink.meeting.validation.provider.TimeRangeProvider;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalTime;

public class ValidTimeRangeValidator
        implements ConstraintValidator<ValidTimeRange, TimeRangeProvider> {
    @Override
    public boolean isValid(TimeRangeProvider value, ConstraintValidatorContext context) {
        if (value == null) return true;

        LocalTime startTime = value.getTimeRangeStart();
        LocalTime endTime = value.getTimeRangeEnd();

        // 둘 다 안 들어온 경우 skip
        if (startTime == null && endTime == null) return true;

        // 하나만 들어온 경우 잘못된 요청
        if (startTime == null || endTime == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "timeRangeStart와 timeRangeEnd는 함께 제공되어야 합니다.")
                    .addConstraintViolation();

            return false;
        }

        // 정상 범위 검증
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "시작 시간(" + startTime + ")은 종료 시간(" + endTime + ")보다 빨라야 합니다.")
                    .addPropertyNode("timeRangeStart")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
