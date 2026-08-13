package com.etour.aspect;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(1)
public class RequestContextAspect {
    private static final Logger log = LoggerFactory.getLogger(RequestContextAspect.class);
    public static final String REQUEST_ID = "requestId";
    public static final String USER = "user";
    public static final String IP = "ip";
    public static final String ENDPOINT = "endpoint";
    @Pointcut("within(com.etour.controllers..*)")
    public void anyController() {
    }
    @Around("anyController()")
    public Object addContext(ProceedingJoinPoint joinPoint) throws Throwable {
        MDC.put(REQUEST_ID, UUID.randomUUID().toString().substring(0, 8));
        MDC.put(USER, currentUser());
        MDC.put(IP, clientIp());
        MDC.put(ENDPOINT, joinPoint.getSignature().toShortString());
        try {
            return joinPoint.proceed();
        } finally {
            MDC.clear();
        }
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        return auth.getName();
    }
    private String clientIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return "-";
            }
            HttpServletRequest request = attrs.getRequest();

            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception ex) {
            log.trace("Could not resolve client IP: {}", ex.getMessage());
            return "-";
        }
    }
}
