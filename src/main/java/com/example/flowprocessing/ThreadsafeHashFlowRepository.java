package com.example.flowprocessing;

import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository(value="threadsafehash")
public class ThreadsafeHashFlowRepository implements FlowRepository {

    private final Map<Integer, Map<String, AggregatedFlow>> aggFlowsMap = new ConcurrentHashMap<>();
    @Override
    public void addRawFlows(List<RawFlow> rawFlows) throws Exception {
        for(RawFlow rawFlow: rawFlows) {
            // Delegate synchronization
            Map<String, AggregatedFlow> hourlyAggFlows = aggFlowsMap.getOrDefault(rawFlow.hour, new ConcurrentHashMap<>());
            // Nothing to synchronize here
            final AggregatedFlow aggFlow = new AggregatedFlow(
                    rawFlow.src_app, rawFlow.dest_app, rawFlow.vpc_id, 0, 0, rawFlow.hour);
            String strFlowHash = computeMD5Hash(aggFlow);
            // Delegate synchronization to atomic computation of HashMap entries for first time and subsequent
            hourlyAggFlows.compute(strFlowHash,
                    (key,currentAggFlow)-> {
                    // Perform SafeAdd on value themselves
                    if(currentAggFlow != null)
                        return currentAggFlow.safeAddRxTx(rawFlow.bytes_rx, rawFlow.bytes_tx);
                    else
                        return aggFlow.safeAddRxTx(rawFlow.bytes_rx, rawFlow.bytes_tx);
                    });
            // Delegate synchronization
            aggFlowsMap.putIfAbsent(rawFlow.hour, hourlyAggFlows);
        }
    }

    @Override
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
