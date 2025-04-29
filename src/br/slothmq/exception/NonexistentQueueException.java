package br.slothmq.exception;

public class NonexistentQueueException extends RuntimeException {
    public NonexistentQueueException(String queueName) {
        super("Nonexistent queue: " + queueName);
    }
}
