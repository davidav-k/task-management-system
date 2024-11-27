package com.example.taskmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Generic API response wrapper")
public class Result {

    /**
     * Creates a new Result object with the given status, code, and message.
     *
     * @param flag    whether the operation was successful
     * @param code    the HTTP status code
     * @param message a descriptive message about the result
     */
    public Result(boolean flag, Integer code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }
    /**
     * Creates a new Result object with the given status, code, message, and data payload.
     *
     * @param flag    whether the operation was successful
     * @param code    the HTTP status code
     * @param message a descriptive message about the result
     * @param data    the optional payload containing additional information
     */
    public Result(boolean flag, Integer code, String message, Object data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = data;
    }
    @Schema(description = "The timestamp when the response was created", example = "2023-11-21T12:00:00Z")
    private Instant timestamp = Instant.now();
    @Schema(description = "Indicates whether the operation was successful", example = "true")
    private boolean flag;
    @Schema(description = "The HTTP status code of the response", example = "200")
    private Integer code;
    @Schema(description = "A message providing details about the result", example = "Operation successful")
    private String message;
    @Schema(description = "Optional data payload containing additional information")
    private Object data;


}
