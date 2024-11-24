package com.example.taskmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * This class defines the schema of the response. It is used to encapsulate data prepared by
 * the server side, this object will be serialized to JSON before sent back to the client end.
 *     Instant timestamp
 *     boolean flag - Two values: true means success, false means not success
 *     Integer code - Status code. e.g., 200
 *     String message - Response message
 *     Object data - The response payload
 */

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {


    public Result(boolean flag, Integer code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    public Result(boolean flag, Integer code, String message, Object data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private Instant timestamp = Instant.now();

    private boolean flag;

    private Integer code;

    private String message;

    private Object data;


}
