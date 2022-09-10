package com.example.flowprocessing;

import java.util.List;

public interface FlowRepository {
    void addRawFlows(List<RawFlow> rawFlows) throws Exception;

    List<AggregatedFlow> getAggregatedFlows(int hour);
}
