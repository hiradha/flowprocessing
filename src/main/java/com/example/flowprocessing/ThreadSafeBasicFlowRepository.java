package com.example.flowprocessing;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Repository(value = "threadsafebasic")
public class ThreadSafeBasicFlowRepository implements FlowRepository {

    // Map<Long, RawFlow> rawFlows = new TreeMap<>();
    Map<Integer, List<AggregatedFlow>> aggFlows = new TreeMap<>();

    // TODO Change from List to Map<md5hash, AggregatedFlow); DONE
    // TODO Add synchronization
    // TODO Add Bloom Filter for faster lookup
    // TODO Add Roaring Bit Map index or other techniques Druid uses
    // TODO Add data skipping and ZORDER indices that Delta Lake uses

    @Override
    public void addRawFlows(List<RawFlow> rawFlows) {
        // rawFlows.put(System.currentTimeMillis(),rawFlow);
        for (RawFlow rawFlow : rawFlows) {
            List<AggregatedFlow> hourlyAggFlows = new ArrayList<>();
            synchronized (this) {
                hourlyAggFlows = aggFlows.getOrDefault(rawFlow.hour, new ArrayList<>());
            }
            AggregatedFlow aggFlow = new AggregatedFlow(
                    rawFlow.src_app, rawFlow.dest_app, rawFlow.vpc_id, 0, 0, rawFlow.hour);
            // TODO Think how to reduce contention later
            synchronized (this) {
                if (hourlyAggFlows.contains(aggFlow)) {
                    AggregatedFlow currentAggFlow = hourlyAggFlows.get(hourlyAggFlows.indexOf(aggFlow));
                    currentAggFlow.bytes_rx += rawFlow.bytes_rx;
                    currentAggFlow.bytes_tx += rawFlow.bytes_tx;
                } else {
                    aggFlow.bytes_rx += rawFlow.bytes_rx;
                    aggFlow.bytes_tx += rawFlow.bytes_tx;
                    hourlyAggFlows.add(aggFlow);
                    aggFlows.put(rawFlow.hour, hourlyAggFlows);
                }
            }
        }
    }

    @Override
    public List<AggregatedFlow> getAggregatedFlows(int hour) {
        return aggFlows.get(hour);
    }
}
