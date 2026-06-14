package com.example.base_spring_boot.aspect;

import com.example.base_spring_boot.models.entities.PerformanceLog;
import com.example.base_spring_boot.models.repositories.IPerformanceLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PerformanceLoggingAspect {

    private final IPerformanceLogRepository performanceLogRepository;

    @Around("execution(* com.example.base_spring_boot.models.services.impl..*(..))")
    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        Object result = joinPoint.proceed();
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        String methodName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
        
        log.info("[PERFORMANCE]");
        log.info("Method: {}", methodName);
        log.info("Execution Time: {} ms", executionTime);
        
        PerformanceLog performanceLog = PerformanceLog.builder()
                .methodName(methodName)
                .executionTime(executionTime)
                .createdAt(LocalDateTime.now())
                .build();
        
        performanceLogRepository.save(performanceLog);
        
        return result;
    }
}
