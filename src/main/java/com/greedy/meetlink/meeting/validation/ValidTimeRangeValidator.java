package com.greedy.meetlink.meeting.validation;

import com.greedy.meetlink.meeting.dto.request.MeetingCreateRequest;
import com.greedy.meetlink.meeting.dto.request.MeetingUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;

/**
 * 시간 범위 논리 검증
 * - timeRangeStart < timeRangeEnd 확인
 * - null인 경우는 통과 (존재 여부는 다른 Validator가 검증)
 */
public class ValidTimeRangeValidator implements ConstraintValidator<ValidTimeRange, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        LocalTime startTime = null;
        LocalTime endTime = null;

        if (value instanceof MeetingCreateRequest request) {
            startTime = request.getTimeRangeStart();
            endTime = request.getTimeRangeEnd();
        } else if (value instanceof MeetingUpdateRequest request) {
            startTime = request.getTimeRangeStart();
            endTime = request.getTimeRangeEnd();
        }

        // 둘 다 null이면 검증 통과 (다른 Validator가 처리)
        if (startTime == null || endTime == null) {
            return true;
        }

        // 시작 시간이 종료 시간보다 늦으면 검증 실패
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "시작 시간(" + startTime + ")은 종료 시간(" + endTime + ")보다 빨라야 합니다."
                    )
                    .addPropertyNode("timeRangeStart")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
