package com.greedy.meetlink.meeting.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 시간 범위의 논리적 유효성 검증
 * - timeRangeStart < timeRangeEnd (자정 넘김 불가)
 */
@Documented
@Constraint(validatedBy = ValidTimeRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeRange {

    String message() default "시작 시간은 종료 시간보다 빨라야 합니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
