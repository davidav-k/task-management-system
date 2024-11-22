package com.example.taskmanagementsystem.validation;

import com.example.taskmanagementsystem.dto.task.TaskFilter;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TaskFilterValidValidator implements ConstraintValidator<TaskFilterValid, TaskFilter> {
    @Override
    public boolean isValid(TaskFilter value, ConstraintValidatorContext context) {

        return value.getPageNumber() != null && value.getPageSize() != null;
    }
}
