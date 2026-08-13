package com.etour.aspect;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.File;

import com.etour.dto.auth.AuthResponse;
import com.etour.dto.auth.LoginRequest;
import com.etour.dto.auth.RegisterRequest;
import com.etour.dto.booking.BookingResponseDTO;
import com.etour.dto.booking.CancellationResponseDTO;

@Aspect
@Component
public class UserActivityAspect {
    private static final Logger activityLog = LoggerFactory.getLogger("USER_ACTIVITY");

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String NA = "-";

    private static final Logger log = LoggerFactory.getLogger(UserActivityAspect.class);

    @PostConstruct
    public void announce() {
        File logFile = new File("logs/application-activity.log");
        log.info("UserActivityAspect ACTIVE - auditing 7 action groups. "
               + "Activity log resolves to: {}", logFile.getAbsolutePath());
        activityLog.info(String.join(" | ",
                LocalDateTime.now().format(TS), "-", "system", "startup", "-",
                "Audit logging initialised"));
    }
    @Pointcut("execution(* com.etour.services.AuthService.login(..))")
    public void loginAction() {
    }
    @Pointcut("execution(* com.etour.services.AuthService.register(..))")
    public void registerAction() {
    }
    @Pointcut("execution(* com.etour.services.BookingService.placeBooking(..))")
    public void bookingAction() {
    }
    @Pointcut("execution(* com.etour.services.CancellationService.cancelBooking(..))")
    public void cancellationAction() {
    }

    @Pointcut("execution(* com.etour.services.AuthService.changePassword(..)) "
            + "|| execution(* com.etour.services.AuthService.resetPassword(..))")
    public void passwordAction() {
    }
    @Pointcut("execution(* com.etour.services.AdminService.createTour(..)) "
            + "|| execution(* com.etour.services.AdminService.updateTour(..)) "
            + "|| execution(* com.etour.services.AdminService.deleteTour(..)) "
            + "|| execution(* com.etour.services.AdminService.setUserActive(..))")
    public void adminAction() {
    }
    @Pointcut("execution(* com.etour.services.ReviewService.submitReview(..))")
    public void reviewAction() {
    }
    @Around("loginAction() || registerAction() || bookingAction() || cancellationAction() "
          + "|| passwordAction() || adminAction() || reviewAction()")
    public Object logActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        String action = joinPoint.getSignature().getName();
        try {
            Object result = joinPoint.proceed();
            write(action, result, joinPoint.getArgs(), "SUCCESS", null);
            return result;
        } catch (Throwable ex) {
            write(action, null, joinPoint.getArgs(), "FAILED", ex.getMessage());
            throw ex;
        }
    }
    private void write(String action, Object result, Object[] args,
                       String status, String reason) {
        String userId = NA;
        String userName = NA;
        String tourName = NA;
        if (result instanceof AuthResponse auth) {
            userId = String.valueOf(auth.getUserId());
            userName = auth.getUsername();

        } else if (result instanceof BookingResponseDTO booking) {
            userId = String.valueOf(booking.getCustomerId());
            userName = booking.getCustomerName();
            tourName = booking.getTourName();
        } else if (result instanceof CancellationResponseDTO cancellation) {
            tourName = "Booking " + cancellation.getBookingId();
        }
        if (NA.equals(userName)) {
            userName = usernameFromArgs(args);
        }
        String line = String.join(" | ",
                LocalDateTime.now().format(TS),
                userId,
                safe(userName),
                action,
                safe(tourName),
                reason == null ? status : status + ": " + reason);
        activityLog.info(line);
    }
    private String usernameFromArgs(Object[] args) {
        if (args == null) {
            return NA;
        }
        for (Object arg : args) {
            if (arg instanceof LoginRequest login) {
                return login.getUsername();
            }
            if (arg instanceof RegisterRequest register) {
                return register.getUsername();
            }
            if (arg instanceof String value && !value.isBlank()) {
                return value;
            }
        }
        return NA;
    }
    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return NA;
        }
        return value.replace("|", "/").trim();
    }
}
