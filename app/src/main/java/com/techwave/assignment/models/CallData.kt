package com.techwave.assignment.models;

public class CallData {

    private String num = "";
    private String type = "";
    private long ts;
    private long dur;

    public CallData() {
    }

    public CallData(String num1, String type1, long ts1, long dur1) {
        this.num = num1;
        this.type = type1;
        this.ts = ts1;
        this.dur = dur1;
    }


    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public long getDur() {
        return dur;
    }

    public void setDur(long dur) {
        this.dur = dur;
    }
}
