package com.example.flowprocessing;

import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository(value="hash")
public class HashFlowRepository implements FlowRepository {

    Map<Integer, Map<String, AggregatedFlow>> aggFlowsMap = new ConcurrentHashMap<>();
    @Override
    public void addRawFlows(List<RawFlow> rawFlows) throws Exception {
        for(RawFlow rawFlow: rawFlows) {
            Map<String, AggregatedFlow> hourlyAggFlows = aggFlowsMap.getOrDefault(rawFlow.hour, new ConcurrentHashMap<>());
            AggregatedFlow aggFlow = new AggregatedFlow(
                    rawFlow.src_app, rawFlow.dest_app, rawFlow.vpc_id, 0, 0, rawFlow.hour);
            String strFlowHash = computeMD5Hash(aggFlow);
            if (hourlyAggFlows.containsKey(strFlowHash)) {
                AggregatedFlow currentAggFlow = hourlyAggFlows.get(strFlowHash);
                currentAggFlow.bytes_rx += rawFlow.bytes_rx;
                currentAggFlow.bytes_tx += rawFlow.bytes_tx;
            } else {
                aggFlow.bytes_rx += rawFlow.bytes_rx;
                aggFlow.bytes_tx += rawFlow.bytes_tx;
                hourlyAggFlows.put(strFlowHash,aggFlow);
                aggFlowsMap.put(rawFlow.hour, hourlyAggFlows);
            }
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
