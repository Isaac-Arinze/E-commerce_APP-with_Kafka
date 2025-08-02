
package com.example.kafkaconsumer.events;


public class ConsumerEvent {
    private String topic;
    private int partition;
    private long offset;
    private String status;

    public ConsumerEvent(String topic, int partition, long offset, String status) {
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.status = status;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ConsumerEvent{" +
                "topic='" + topic + '\'' +
                ", partition=" + partition +
                ", offset=" + offset +
                ", status='" + status + '\'' +
                '}';
    }
}
