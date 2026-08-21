package com.ibb.yurtlar.exception;

public class InvalidKafkaEventPayloadException
        extends RuntimeException {

    public InvalidKafkaEventPayloadException(
            String message
    ) {
        super(message);
    }

    public InvalidKafkaEventPayloadException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
