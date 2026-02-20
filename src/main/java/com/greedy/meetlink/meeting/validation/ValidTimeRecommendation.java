package com.greedy.meetlink.meeting.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidTimeRecommendationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeRecommendation {
    String message() default "시간 추천을 사용하는 경우 시간 설정은 필수입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
