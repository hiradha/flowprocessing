package com.example.flowprocessing;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private RealTimeStore realTimeStore;
    @Autowired
    private HistoricalStore historicalStore;

    @Scheduled(fixedRate = 120000)
    public void reportCurrentTime() {
        log.info("The time is now {}", dateFormat.format(new Date()));
    }

    @Scheduled(fixedRate = 5000, initialDelay = 5000)
    public void processRawFlowsToHistoricalStorage() throws Exception {
        log.info("Started processing Raw Flows to send to Historical Store {}", dateFormat.format(new Date()));
        realTimeStore.processLatestRawFlowsToSubmitToHistoricals();
        log.info("Completed processing Raw Flows to send to Historical Store {}", dateFormat.format(new Date()));


    }

}
