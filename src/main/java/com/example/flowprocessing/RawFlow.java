package com.example.flowprocessing;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotEmpty;

public class RawFlow {


    @NotEmpty(message = "src_app must not be empty")
    String src_app;
    @NotEmpty(message = "dest_app must not be empty")
    String dest_app;

    @NotEmpty(message = "vpc_id must not be empty")
    String vpc_id;
    // @NotEmpty(message = "bytes_tx must not be empty")
    long bytes_tx;

    // @NotEmpty(message = "bytes_rx must not be empty")
    long bytes_rx;

    // @NotEmpty(message = "hour must not be empty")
    int hour;

    @JsonIgnore
    int systemTimeStamp;

    public RawFlow(String src_app, String dest_app, String vpc_id, long bytes_tx, long bytes_rx, int hour) {
        this.src_app = src_app;
        this.dest_app = dest_app;
        this.vpc_id = vpc_id;
        this.bytes_tx = bytes_tx;
        this.bytes_rx = bytes_rx;
        this.hour = hour;
    }

    public String getSrc_app() {
        return src_app;
    }


    public String getDest_app() {
        return dest_app;
    }


    public String getVpc_id() {
        return vpc_id;
    }


    public long getBytes_rx() {
        return bytes_rx;
    }

    public void setBytes_rx(long bytes_rx) {
        this.bytes_rx = bytes_rx;
    }

    public long getBytes_tx() {
        return bytes_tx;
    }

    public void setBytes_tx(long bytes_tx) {
        this.bytes_tx = bytes_tx;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getSystemTimeStamp() {
        return systemTimeStamp;
    }

    public void setSystemTimeStamp(int systemTimeStamp) {
        this.systemTimeStamp = systemTimeStamp;
    }
}