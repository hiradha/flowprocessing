package com.example.flowprocessing;


import java.util.Objects;

public class AggregatedFlow {

    private String src_app;
    private String dest_app;

    private String vpc_id;
    public long bytes_tx;

    public long bytes_rx;

    private int hour;


    public AggregatedFlow(String src_app, String dest_app, String vpc_id, long bytes_tx, long bytes_rx, int hour) {
        this.src_app = src_app;
        this.dest_app = dest_app;
        this.vpc_id = vpc_id;
        this.bytes_tx = bytes_tx;
        this.bytes_rx = bytes_rx;
        this.hour = hour;
    }

    @Override
    public String toString() {
        return "AggregatedFlow{" +
                "src_app='" + src_app + '\'' +
                ", dest_app='" + dest_app + '\'' +
                ", vpc_id='" + vpc_id + '\'' +
                ", bytes_tx=" + bytes_tx +
                ", bytes_rx=" + bytes_rx +
                ", hour=" + hour +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregatedFlow that = (AggregatedFlow) o;
        return hour == that.hour && src_app.equals(that.src_app) && dest_app.equals(that.dest_app) && vpc_id.equals(that.vpc_id);
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


    public long getBytes_tx() {
        return bytes_tx;
    }

    public void setBytes_tx(long bytes_tx) {
        this.bytes_tx = bytes_tx;
    }

    public long getBytes_rx() {
        return bytes_rx;
    }

    public void setBytes_rx(long bytes_rx) {
        this.bytes_rx = bytes_rx;
    }

    public int getHour() {
        return hour;
    }


    public synchronized AggregatedFlow safeAddRxTx(long bytes_rx, long bytes_tx) {
        this.bytes_rx += bytes_rx;
        this.bytes_tx += bytes_tx;
        return this;
    }


    @Override
    public int hashCode() {
        return Objects.hash(src_app, dest_app, vpc_id, hour);
    }
}