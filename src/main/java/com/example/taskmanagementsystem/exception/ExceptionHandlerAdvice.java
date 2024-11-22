package com.example.taskmanagementsystem.exception;

import com.example.taskmanagementsystem.dto.Result;
import com.example.taskmanagementsystem.dto.StatusCode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleEntityNotFoundException(EntityNotFoundException ex) {
        return new Result(false, StatusCode.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalAccessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handleIllegalAccessException(IllegalAccessException ex) {
        return new Result(false, StatusCode.INVALID_ARGUMENT,"Provided arguments are not valid", ex.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        return new Result(false, StatusCode.INVALID_ARGUMENT, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result handleConstraintViolationException(ConstraintViolationException ex) {
        return new Result(false, StatusCode.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handleValidationException(MethodArgumentNotValidException ex){
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        Map<String, String> map = new HashMap<>(errors.size());
        errors.forEach((error) -> {
            String key = ((FieldError) error).getField();
            String val = error.getDefaultMessage();
            map.put(key, val);
        });

        return new Result(false, StatusCode.INVALID_ARGUMENT, "Provided arguments are not valid", map);
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class,  })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handleAuthenticationException(Exception ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "username or password is incorrect", ex.getMessage());
    }

    @ExceptionHandler(AccountStatusException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handleAccountStatusException(AccountStatusException ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "user account is abnormal", ex.getMessage());
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Result handleInvalidBearerTokenException(InvalidBearerTokenException ex) {
        return new Result(false, StatusCode.UNAUTHORIZED, "access token is invalid", ex.getMessage());
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Result handleAccessDeniedException(AccessDeniedException ex) {
        return new Result(false, StatusCode.FORBIDDEN, "No permission", ex.getMessage());
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    Result handleInsufficientAuthenticationException(InsufficientAuthenticationException ex) {
        return new Result(false, StatusCode.FORBIDDEN, "No permission", ex.getMessage());
    }

    @ExceptionHandler(PasswordChangeIllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result handlePasswordChangeIllegalArgumentException(PasswordChangeIllegalArgumentException ex){
        return new Result(false, StatusCode.INVALID_ARGUMENT, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    Result handleOtherException(Exception ex) {
        System.out.println(ex.getClass().getName());
        return new Result(false, StatusCode.INTERNAL_SERVER_ERROR, "Server internal error", ex.getMessage());
    }

}
