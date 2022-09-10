package com.example.flowprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
@Repository
public class HistoricalStore {

    private final Map<Integer, Map<String, AggregatedFlow>> aggFlowsMap = new ConcurrentHashMap<>();

    private AtomicLong lastFlowProcessTimeStamp = new AtomicLong(0);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private static final Logger log = LoggerFactory.getLogger(HistoricalStore.class);

    // TODO Bloom Filter on segments like Druid to filter data in segments?
    // Dictionary encoding on Strings to save space
    // Columnar encoding for more efficient CPU  usage

    public AtomicLong getLastFlowProcessTimeStamp() {
        return lastFlowProcessTimeStamp;
    }

    public void setLastFlowProcessTimeStamp(AtomicLong lastFlowProcessTimeStamp) {
        this.lastFlowProcessTimeStamp = lastFlowProcessTimeStamp;
    }


    public void processLatestAggregatedFlows(Map<Integer, Map<String, AggregatedFlow>> latestAggFlowsMap) throws Exception {
        // TODO Also Historical is performing mutation on the Deep Storage here - that is prone to contention.
        //  In Druid, these latestAggFlows that RealTime Store sent to Historical
        //  are persisted as Immutable segments and data is collected form multiple segments for Read Queries.
        //  Druid Compaction Tasks keep the number of Immutable segments under check in the background
        for(Map.Entry<Integer, Map<String, AggregatedFlow>>  entry:latestAggFlowsMap.entrySet()) {
            int hour = entry.getKey();
            Map<String, AggregatedFlow> hourlyFlowMap = entry.getValue();
            log.info("Processing Latest Aggregated Flows submitted by real time store fpr hour {} at {}", hour, dateFormat.format(new Date()));

            for (String flowHash : hourlyFlowMap.keySet()) {
                // Delegate synchronization
                Map<String, AggregatedFlow> hourlyAggFlows = aggFlowsMap.getOrDefault(hour, new ConcurrentHashMap<>());
                AggregatedFlow latestAggFlow = hourlyFlowMap.get(flowHash);
                // Delegate synchronization to atomic computation of HashMap entries for first time and subsequent
                hourlyAggFlows.compute(flowHash,
                        (key, currentAggFlow) -> {
                            // Perform SafeAdd on value themselves
                            if (currentAggFlow != null)
                                return currentAggFlow.safeAddRxTx(latestAggFlow.getBytes_rx(), latestAggFlow.getBytes_tx());
                            else
                                return latestAggFlow;
                        });
                // Delegate synchronization
                aggFlowsMap.putIfAbsent(hour, hourlyAggFlows);
            }
        }

    }


    public List<AggregatedFlow> getAggregatedFlows(int hour) {
        return aggFlowsMap.get(hour) != null?
                aggFlowsMap.get(hour).values().stream().collect(Collectors.toList()) : new ArrayList<>();
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
