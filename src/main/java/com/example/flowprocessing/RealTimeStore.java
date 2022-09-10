package com.example.flowprocessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Repository
public class RealTimeStore {

    private final ConcurrentLinkedQueue<RawFlow> rawFlowQueue = new ConcurrentLinkedQueue<RawFlow>();

    @Autowired
    private HistoricalStore historicalStore;

    // TODO Columnar format like Apache Arrow for in memory flows
    //  Queryable Buffer indices like in Druid
    public void addRawFlows(List<RawFlow> rawFlows) throws Exception {
        // TODO Here you amy alternatively make the choice of dividing them into hourly buckets by putting them in a Map with hour as key
        //  It is a tradeoff between raw flow throughput vs aggregation task throughput
        //  In general, a generic ingestionspec can specify what aggregations we would like to perform during ingestion and what would be performed via Periodic RedIndexing or Aggregation tasks
        //  Acheiving this balance is key
        rawFlowQueue.addAll(rawFlows);
    }

    public ConcurrentLinkedQueue<RawFlow> getRawFlows() {
        return rawFlowQueue;
    }

    public void processLatestRawFlowsToSubmitToHistoricals() throws Exception {

        // Synchronization below is unnecessary if there is only one scheduled task performing aggregation but may be necessary if there are more scheduled tasks
        // running on a ForkJoinPool
        Map<Integer, Map<String, AggregatedFlow>> aggFlowsMap = new ConcurrentHashMap<>();
        int i=0;
        RawFlow rawFlow = null;
        // Poll only 100 at a time for now; Obviously this should be an adaptive rate depending on inflow
        while ( (rawFlow = rawFlowQueue.poll())!= null && i<1000)  {
            // Delegate synchronization
            Map<String, AggregatedFlow> hourlyAggFlows = aggFlowsMap.getOrDefault(rawFlow.hour, new ConcurrentHashMap<>());
            // Nothing to synchronize here
            final AggregatedFlow aggFlow = new AggregatedFlow(
                    rawFlow.src_app, rawFlow.dest_app, rawFlow.vpc_id, 0, 0, rawFlow.hour);
            String strFlowHash = computeMD5Hash(aggFlow);
            // Delegate synchronization to atomic computation of HashMap entries for first time and subsequent
            final RawFlow flow = rawFlow;
            hourlyAggFlows.compute(strFlowHash,
                    (key,currentAggFlow)-> {
                        // Perform SafeAdd on value themselves
                        if(currentAggFlow != null)
                            return currentAggFlow.safeAddRxTx(flow.bytes_rx, flow.bytes_tx);
                        else
                            return aggFlow.safeAddRxTx(flow.bytes_rx, flow.bytes_tx);
                    });
            // Delegate synchronization
            aggFlowsMap.putIfAbsent(rawFlow.hour, hourlyAggFlows);
            i++;
        }
        // TODO After initial processing, we are directly submitting data to historicals each index.
        //  In Druid, there are really two submissions - one more frequently to the local disk and another task that merges these indices into a segment
        //  Also instead of directly submitting to Historicals, the segment is submitted to Cheap Storage like S3/Minio from where historicals load Immutable segments
        historicalStore.processLatestAggregatedFlows(aggFlowsMap);
    }



    private String computeMD5Hash(AggregatedFlow aggFlow) throws Exception{
        byte [] msg = aggFlow.toString().getBytes();
        byte[] hash = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(msg);
        }
        catch (NoSuchAlgorithmException e) {
            throw e;
        }
        StringBuilder strBuilder = new StringBuilder();
        for(byte b:hash)
        {
            strBuilder.append(String.format("%02x", b));
        }
        String strHash = strBuilder.toString();
        System.out.println("The MD5 hash: "+strHash);
        return strHash;
    }
}
