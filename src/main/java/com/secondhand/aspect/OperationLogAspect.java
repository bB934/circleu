package com.secondhand.aspect;

import com.secondhand.annotation.OperationLog;
import com.secondhand.entity.OperationLogEntity;
import com.secondhand.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Aspect
@Component
@Order(1)
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        Long userId = request != null ? (Long) request.getAttribute("userId") : null;
        String username = request != null ? (String) request.getAttribute("username") : null;
        String ip = request != null ? getClientIp(request) : null;
        String url = request != null ? request.getRequestURI() : null;
        String method = request != null ? request.getMethod() : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String params = safeParams(joinPoint);

        Object result = null;
        String errorMsg = null;
        int status = 1;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            int duration = (int) (System.currentTimeMillis() - startTime);
            String finalError = errorMsg;
            int finalStatus = status;
            Object finalResult = result;
            CompletableFuture.runAsync(() -> {
                try {
                    OperationLogEntity logEntity = new OperationLogEntity();
                    logEntity.setUserId(userId);
                    logEntity.setUsername(username);
                    logEntity.setOperation(operationLog.value());
                    logEntity.setDescription(operationLog.description());
                    logEntity.setMethod(method);
                    logEntity.setUrl(url);
                    logEntity.setIp(ip);
                    logEntity.setUserAgent(userAgent);
                    logEntity.setRequestParams(params);
                    logEntity.setResponseData(finalResult != null ? truncate(finalResult.toString()) : null);
                    logEntity.setDuration(duration);
                    logEntity.setStatus(finalStatus);
                    logEntity.setErrorMsg(finalError);
                    logEntity.setCreateTime(LocalDateTime.now());
                    operationLogService.save(logEntity);
                } catch (Exception ex) {
                    log.warn("保存操作日志失败: {}", ex.getMessage());
                }
            });
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String safeParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            return truncate(Arrays.toString(args));
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        return s.length() > 2000 ? s.substring(0, 2000) : s;
    }
}
