package com.example.flowprocessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository(value="delayed")
public class DelayedAggregationFlowRepository implements FlowRepository {

    private final Map<Integer, Map<String, AggregatedFlow>> aggFlowsMap = new ConcurrentHashMap<>();

    @Autowired
    RealTimeStore realTimeStore;

    @Autowired
    HistoricalStore historicalStore;
    @Override
    public void addRawFlows(List<RawFlow> rawFlows) throws Exception {
        realTimeStore.addRawFlows(rawFlows);
    }

    @Override
    public List<AggregatedFlow> getAggregatedFlows(int hour) {
        List<AggregatedFlow> aggregatedFlowsFromHistoricalStore = historicalStore.getAggregatedFlows(hour);
        // TODO As of now this currently returns only Historical Store information and the results may be stale subject to the interval of the scheduled task
        //  If we seek even more freshness, we should combine results form RealTime and Historical Stores; In Druid another set of nodes called Broker Nodes do this by querying both Real Time and Historical Nodes and merging results together.
        //  However in our case it requires that the removal of raw flows from real time store and the storage in historical stores happens atomically
        //  Druid acheieves this between middle managers ( real time nodes) and Historicals(Deep Storage) via tasks co-ordinating via ZooKeeper
        return aggregatedFlowsFromHistoricalStore;
    }
}
