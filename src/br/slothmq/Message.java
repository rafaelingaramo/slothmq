package br.slothmq;

public class Message {
    private Object contents;

    private String queueName;

    public Message() {
    }

    public Message(Object contents) {
        this.contents = contents;
    }

    public Object getContents() {
        return contents;
    }

    public void setContents(Object contents) {
        this.contents = contents;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }
}
