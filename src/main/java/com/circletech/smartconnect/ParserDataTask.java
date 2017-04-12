package com.circletech.smartconnect;

import java.util.Arrays;
import java.util.UUID;

/**
 * data processing queue object class
 */
public class ParserDataTask {

    private String id;

    private Long type;
    private byte[] data;

    private Long timestamp;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public ParserDataTask(Long type, byte[] data, long timestamp){

        id = UUID.randomUUID().toString();
        this.timestamp = timestamp;//add timestamp

        this.type = type;

        this.data = Arrays.copyOfRange(data, 0, data.length);

    }

    @Override
    public String toString() {
        return "ParserDataTask{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
