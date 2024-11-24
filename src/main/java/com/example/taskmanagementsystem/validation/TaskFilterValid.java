package com.example.taskmanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TaskFilterValidValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskFilterValid {
    String message() default "Pagination fields must be specified.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
