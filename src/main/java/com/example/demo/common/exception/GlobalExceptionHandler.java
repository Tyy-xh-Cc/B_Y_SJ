package com.example.demo.common.exception;

import com.example.demo.common.Result;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;



@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== 参数校验异常 ==========
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleValidationException(Exception e) {
        String message = e instanceof BindException
                ? ((BindException) e).getFieldError().getDefaultMessage()
                : ((MethodArgumentNotValidException) e).getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return Result.error("参数校验失败: " + message);
    }

    // 处理 @RequestParam 校验 (需要 spring-boot-starter-validation)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("参数不合法");
        log.warn("参数校验失败: {}", message);
        return Result.error("参数校验失败: " + message);
    }

    // 处理请求参数缺失、类型不匹配
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少必要参数: {}", e.getParameterName());
        return Result.error("缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {} 需要 {}", e.getName(), e.getRequiredType());
        return Result.error("参数类型错误: " + e.getName());
    }

    // ========== 请求格式异常 ==========
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体不可读: {}", e.getMessage());
        // 常见于 JSON 格式错误、日期格式错误
        if (e.getCause() instanceof InvalidFormatException) {
            return Result.error("字段类型格式不正确");
        }
        return Result.error("请求体格式错误");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<String> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return Result.error("不支持的媒体类型");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error("请求方法不支持: " + e.getMethod());
    }

    // 文件上传大小超限
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Result<String> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return Result.error("上传文件大小超过限制");
    }

    // ========== 认证授权异常 ==========
    // 注意：若 SecurityConfig 中已用 entryPoint/accessDeniedHandler 定制了 JSON 响应，这两个可能不会进入此 Handler
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("登录失败: {}", e.getMessage());
        return Result.error("用户名或密码错误");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<String> handleAuthenticationException(AuthenticationException e) {
        log.warn("未登录或登录已过期: {}", e.getMessage());
        return Result.error("请先登录");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error("权限不足，无法访问");
    }

    // ========== 业务异常 ==========
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 可根据业务需要设为不同状态码
    public Result<String> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    // ========== 数据库异常 (生产环境不应暴露细节) ==========
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleDataAccessException(DataAccessException e) {
        log.error("数据库操作异常: ", e);
        return Result.error("数据访问异常，请稍后重试");
    }

    // ========== 兜底异常 ==========
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception e) {
        log.error("系统未知异常: ", e);
        // 绝对不要把 e.getMessage() 直接返回给前端，可能泄露敏感信息
        return Result.error("系统内部错误，请联系管理员");
    }
}