package org.monkey_business.utility_supervisor.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.monkey_business.utility_supervisor.service.ErrorNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ScheduledTaskExceptionAspect {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskExceptionAspect.class);

    private final ErrorNotificationService errorNotificationService;

    public ScheduledTaskExceptionAspect(ErrorNotificationService errorNotificationService) {
        this.errorNotificationService = errorNotificationService;
    }

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object handleScheduledTaskException(ProceedingJoinPoint joinPoint) throws Throwable {
        String taskName = joinPoint.getSignature().toShortString();
        try {
            log.debug("Executing scheduled task: {}", taskName);
            Object result = joinPoint.proceed();
            log.debug("Scheduled task completed successfully: {}", taskName);
            return result;
        } catch (Throwable throwable) {
            log.error("Scheduled task failed: {}", taskName, throwable);
            errorNotificationService.sendScheduledTaskError(taskName, throwable);
            // Re-throw to maintain Spring's scheduling behavior
            throw throwable;
        }
    }
}