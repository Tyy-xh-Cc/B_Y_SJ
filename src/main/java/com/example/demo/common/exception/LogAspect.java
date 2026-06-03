package com.example.demo.common.exception;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Pointcut("execution(* com.example.demo.controller.*.*(..))")
    public void pointcut() {}

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        log.info("请求接口: {}", joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "pointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        log.info("接口返回: {}", result);
    }
}