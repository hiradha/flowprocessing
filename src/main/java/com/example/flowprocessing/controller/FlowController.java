package com.example.flowprocessing.controller;

import java.util.List;

import com.example.flowprocessing.AggregatedFlow;
import com.example.flowprocessing.FlowService;
import com.example.flowprocessing.RawFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@Validated // Enable when working
public class FlowController {

   @Autowired
    private FlowService flowService;

    @GetMapping("/flows")
    public List<AggregatedFlow> getFlows(@RequestParam(value = "hour") @NotNull int hour) {

        return flowService.getAggregatedFlows(hour);
    }

    /*
    @GetMapping("/flow")
    public RawFlow getFlow(@RequestParam(value = "hour") int hour) {

        return new RawFlow("a","b", "c", 0, 0, 1);
    }
     */

    @PostMapping("/flows")
    public void postRawFlow(@RequestBody @Valid List<@Valid RawFlow> rawFlows) throws Exception {
        flowService.submitRawFlows(rawFlows);
    }
}
