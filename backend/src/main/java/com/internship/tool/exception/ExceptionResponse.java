package com.internship.tool.exception;

import java.time.LocalDateTime;

public class ExceptionResponse {

    private String message;
    private int status;
    private LocalDateTime timestamp;

    public ExceptionResponse(
            String message,
            int status,
            LocalDateTime timestamp
    ) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}