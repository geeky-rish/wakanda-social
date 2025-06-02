package com.microblog.exception;

public class InvalidPostContentException extends RuntimeException {
    public InvalidPostContentException(String message) {
        super(message);
    }
    
    public InvalidPostContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
