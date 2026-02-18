package com.carplatform.gateway.aspect;

import com.carplatform.gateway.util.TraceIdManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Service Client Logging Aspect
 *
 *
 * AOP aspect that intercepts all service client calls and logs:
 * - Service name and method being called
 * - Request parameters
 * - Response status/success
 * - Call latency (duration in milliseconds)
 * - Exceptions (if any)
 * - Trace ID correlation
 *
 * Captures calls to:
 * - CatalogServiceClient
 * - InventoryServiceClient
 *
 * Example Log Output:
 * [correlation_id=550e8400-e29b-41d4-a716-446655440000]
 * ServiceClient.getCarById(uuid) → SUCCESS in 245ms
 * [correlation_id=550e8400-e29b-41d4-a716-446655440000]
 * ServiceClient.checkAvailability(uuid) → FAILED in 3000ms: timeout
 */
@Slf4j
@Aspect
@Component
public class ServiceClientLoggingAspect {

    /**
     * Pointcut: All methods in service client classes
     */
    @Pointcut("execution(* com.carplatform.gateway.client.*ServiceClient.*(..))")
    public void serviceClientMethods() {
    }

    /**
     * Around advice: Log all service client method calls
     *
     * @param joinPoint Method execution context
     * @return Method return value
     * @throws Throwable If method throws exception
     */
    @Around("serviceClientMethods()")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String serviceName = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String traceId = TraceIdManager.get();

        // Log request start
        log.debug(
                "ServiceClient Call Start: {}.{}({}) [traceId={}]",
                serviceName,
                methodName,
                formatArgs(args),
                traceId);

        long startTime = System.currentTimeMillis();

        try {
            // Execute the actual service call
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;

            // Log success
            log.info(
                    "ServiceClient Call Success: {}.{}() → completed in {}ms [traceId={}]",
                    serviceName,
                    methodName,
                    duration,
                    traceId);

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            // Log failure with exception details
            log.warn(
                    "ServiceClient Call Failed: {}.{}() → {} in {}ms | Exception: {} [traceId={}]",
                    serviceName,
                    methodName,
                    e.getClass().getSimpleName(),
                    duration,
                    e.getMessage(),
                    traceId);

            throw e;
        }
    }

    /**
     * Format method arguments for logging (truncated if too long)
     *
     * @param args Method arguments
     * @return Formatted argument string
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                sb.append(", ");

            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else {
                String argStr = arg.toString();
                // Truncate long argument strings
                if (argStr.length() > 100) {
                    sb.append(argStr, 0, 100).append("...");
                } else {
                    sb.append(argStr);
                }
            }
        }

        return sb.toString();
    }
}
