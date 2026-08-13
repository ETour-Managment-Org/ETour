package com.etour.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(2)
public class PerformanceAspect {
    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);
    private final long slowMillis;
    public PerformanceAspect(@Value("${etour.logging.slow-ms:400}") long slowMillis) {
        this.slowMillis = slowMillis;
    }
    @Pointcut("within(com.etour.services..*)")
    public void anyService() {
    }

    @Pointcut("execution(* com.etour.repositories..*.*(..))")
    public void anyRepository() {
    }

    @Around("anyService() || anyRepository()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        String target = joinPoint.getSignature().toShortString();
        try {
            Object result = joinPoint.proceed();
            record(target, start, null);
            return result;
        } catch (Throwable ex) {
            record(target, start, ex);
            throw ex;
        }
    }
    private void record(String target, long startNanos, Throwable ex) {
        long ms = (System.nanoTime() - startNanos) / 1_000_000;
        if (ex != null) {
            log.warn("{} failed after {} ms: {}: {}",
                    target, ms, ex.getClass().getSimpleName(), ex.getMessage());

        } else if (ms >= slowMillis) {
            log.warn("SLOW {} took {} ms (threshold {} ms)", target, ms, slowMillis);

        } else {
            log.debug("{} took {} ms", target, ms);
        }
    }
}
