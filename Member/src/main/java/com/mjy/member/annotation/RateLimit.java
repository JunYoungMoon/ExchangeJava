package com.mjy.member.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String key() default ""; // Bucket 구분을 위한 키
    long limit() default 3; // 초당 허용 요청 수, 기본값은 3
    long period() default 1000; // 제한 주기 (밀리초), 기본값은 1000
}
