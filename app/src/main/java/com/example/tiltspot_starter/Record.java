package com.example.tiltspot_starter;

public class Record {
    String recordId, pitch, roll, timestamp;

    public Record() {
    }

    public Record(String recordId, String pitch, String roll, String timestamp) {
        this.recordId = recordId;
        this.pitch = pitch;
        this.roll = roll;
        this.timestamp = timestamp;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPitch() {
        return pitch;
    }

    public void setPitch(String pitch) {
        this.pitch = pitch;
    }

    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}




