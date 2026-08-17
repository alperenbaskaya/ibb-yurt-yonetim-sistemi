package com.ibb.yurtlar.exception;

public class DuplicateKafkaEventException
        extends RuntimeException {

    public DuplicateKafkaEventException(
            String eventId,
            String consumerName
    ) {
        super(
                "Kafka event daha önce işlenmiş."
                        + " eventId=" + eventId
                        + " consumerName=" + consumerName
        );
    }
}