package com.example.taskmanagementsystem.dto;
/**
 * This class defines the schema of StatusCode
 * SUCCESS = 200; // Success
 * INVALID_ARGUMENT = 400; // Bad request, e.g., invalid parameters
 * UNAUTHORIZED = 401; // Username or password incorrect
 * FORBIDDEN = 403; // No permission
 * NOT_FOUND = 404; // Not found
 * INTERNAL_SERVER_ERROR = 500; // Server internal error
 */
public class StatusCode {

    public static final int SUCCESS = 200;

    public static final int INVALID_ARGUMENT = 400;

    public static final int UNAUTHORIZED = 401;

    public static final int FORBIDDEN = 403;

    public static final int NOT_FOUND = 404;

    public static final int INTERNAL_SERVER_ERROR = 500;

}
