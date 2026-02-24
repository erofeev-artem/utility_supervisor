package org.monkey_business.utility_supervisor.exception;

public class KoltushiClientException extends RuntimeException {
    public KoltushiClientException(String message) {
        super(message);
    }

    public KoltushiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}