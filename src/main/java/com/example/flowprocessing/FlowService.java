package com.example.flowprocessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlowService {

    @Autowired
    @Qualifier("delayed")
    FlowRepository flowRepository;
    public List<AggregatedFlow> getAggregatedFlows(int hour) {
        return flowRepository.getAggregatedFlows(hour);
    }

    public void submitRawFlows(List<RawFlow> rawFlows) throws Exception {
        flowRepository.addRawFlows(rawFlows);
    }
}
