package com.mjy.exchange.aop;

import com.mjy.exchange.component.APIRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static com.mjy.exchange.util.CommonUtil.getClientIp;

@Aspect
@Component
public class RateLimitAspect {

    private final APIRateLimiter apiRateLimiter;

    private final MessageSourceAccessor messageSourceAccessor;

    public RateLimitAspect(APIRateLimiter apiRateLimiter, MessageSourceAccessor messageSourceAccessor) {
        this.apiRateLimiter = apiRateLimiter;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String apiKey = rateLimit.key() + "|" + getClientIp(request);
        long limit = rateLimit.limit();
        long period = rateLimit.period();

        if (apiRateLimiter.tryConsume(apiKey, limit, period)) {
            return joinPoint.proceed();
        } else {
            String successMessage = messageSourceAccessor.getMessage("rateLimit.msessage", new Object[]{apiKey});

            throw new RuntimeException(successMessage);
        }
    }
}