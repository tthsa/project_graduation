package com.javaevaluation.exception;

import com.javaevaluation.common.ErrorCode;
import com.javaevaluation.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("⚠️ 业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验/绑定异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(Exception e) {
        String message;
        if (e instanceof MethodArgumentNotValidException ex) {
            message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        } else {
            message = ((BindException) e).getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }

        log.warn("⚠️ 参数校验失败: {}", message);
        return Result.fail(ErrorCode.BAD_REQUEST, message);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("⚠️ 非法参数: {}", e.getMessage());
        return Result.fail(ErrorCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理数据库数据完整性违反异常（外键约束、唯一约束等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String causeMsg = e.getMostSpecificCause().getMessage();
        log.warn("数据完整性违反: {}", causeMsg);

        String msg;
        if (causeMsg != null && causeMsg.contains("student_class_id_fkey")) {
            msg = "班级不存在，请先创建班级";
        } else if (causeMsg != null && causeMsg.contains("student_student_no_key")) {
            msg = "学号已存在";
        } else if (causeMsg != null && causeMsg.contains("teacher_teacher_no_key")) {
            msg = "工号已存在";
        } else {
            msg = "数据关联错误或重复";
        }
        return Result.fail(ErrorCode.BAD_REQUEST, msg);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("❌ 系统异常: {}", e.getMessage(), e);
        return Result.fail(ErrorCode.INTERNAL_ERROR, "系统异常，请稍后重试");
    }
}