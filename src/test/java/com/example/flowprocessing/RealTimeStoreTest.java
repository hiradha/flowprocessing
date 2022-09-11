package com.example.flowprocessing;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

public class RealTimeStoreTest {

    // Class Under Test
    RealTimeStore realTimeStore;


    // Mocks
    HistoricalStore historicalStore;

    @Before
    public void setUp() throws Exception {
        historicalStore = mock(HistoricalStore.class);
        // Default mock common to most tests
        doNothing().when(historicalStore).processLatestAggregatedFlows(any());
        Properties prop = new Properties();
        // prop.load(RecommendationServiceImplTest.class.getResourceAsStream("/test-recommendation.properties"));
        realTimeStore = new RealTimeStore(historicalStore);
    }


    @After
    public void tearDown() throws Exception {
        reset(historicalStore);

    }

    @Test
    public void testRealTimeStoreProcessesLatestFlowsAndSubmitsAggregatesToHistoricalStore() throws Exception {
        RawFlow flow1 = new RawFlow("foo", "bar", "vpc-0", 100, 300, 1);
        RawFlow flow2 = new RawFlow("foo", "bar", "vpc-0", 200, 600, 1);
        RawFlow flow3 = new RawFlow("baz", "qux", "vpc-0", 100, 500, 1);
        RawFlow flow4 = new RawFlow("baz", "qux", "vpc-0", 100, 500, 2);
        RawFlow flow5 = new RawFlow("baz", "qux", "vpc-1", 100, 500, 2);

        realTimeStore.addRawFlows(Arrays.asList(flow1, flow2,flow3, flow4, flow5));
        ArgumentCaptor<Map> latestAggFlowsCaptor = ArgumentCaptor.forClass(Map.class);
        realTimeStore.processLatestRawFlowsToSubmitToHistoricals();

        verify(historicalStore).processLatestAggregatedFlows(latestAggFlowsCaptor.capture());
        Map<Integer, Map<String, AggregatedFlow>> aggFlowsMap =  latestAggFlowsCaptor.getValue();

        assertEquals("Aggregated Flows Map should have size 2",2, aggFlowsMap.size());
        Map<String, AggregatedFlow> hour1Map = aggFlowsMap.get(1);
        assertEquals("hour1 Map size should be 2 ",2, hour1Map.size());

        for(AggregatedFlow aggFlow : hour1Map.values()) {
            if(aggFlow.getSrc_app().equals(flow1.src_app) && aggFlow.getDest_app().equals(flow1.dest_app) && aggFlow.getVpc_id().equals(flow1.vpc_id)) {
                assertEquals("hour 1 tx bytes should match", 300, aggFlow.getBytes_tx());
                assertEquals("hour 1 tx bytes should match", 900, aggFlow.getBytes_rx());
            }
            if(aggFlow.getSrc_app().equals(flow3.src_app) && aggFlow.getDest_app().equals(flow3.dest_app) && aggFlow.getVpc_id().equals(flow3.vpc_id)) {
                assertEquals("hour 1 tx bytes should match", 100, aggFlow.getBytes_tx());
                assertEquals("hour 1 tx bytes should match", 500, aggFlow.getBytes_rx());
            }

        }

        // TODO Do similar for Hour 2

    }


}
