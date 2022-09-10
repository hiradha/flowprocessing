# Radha Popuri Sample

Provides an example application that provides API's for submitting network flows and view aggregate results

## Description



Given limited time, this project showcases the following

* API
  * Raw Flow Submission
  * Hourly Aggregates per src_app, dest_app, vpc_id
* Design
  * Spring MVC based API REST API Design
  * Various Repository implementations to serve the API's. The first four perform aggregation on ingestion while the last one performs Delayed aggregation
    * Basic Flow Repository - no synchronization/no optimiziation
    * Hash Based Flow Repository - uses MD5 hash for efficient retreival
    * ThreadSafeBasicFlowRepository - adds synchronization to 1
    * ThreadsafeHashFlowRepository - Threadsafe version of 2 but also improves concurrency by delegating to ConcurrentHashMap
    * DelayedAggregationFlowRepository
      * This is what is wired in the application currently. Performs Delayed aggregation on ingestion side to improve throughput at the cost of somewhat stale results on the aggregation api.
        * Also TODO demonstrates Real Time Store and HistoricalStore concepts and how freshness of aggregates can be improved
* TODO in code in various places discusses the improvements and compares with using a time series database like Druid

## Limitations
* No pagination and cursoring on flows API
* No Validation on API - Need to enable @Validation beans and wire MethodAnnotationProcessors to invoke Hibernate Bean Validation
* Only in memory persistence
* Single Node only
* No database indices for faster lookup for both historical
* No tests for each component. Would use Mockito to unit test each component.
* No durability of flow logs

## Challenges I faced
For a while, my endpoints seemed to have not mapped by Spring MVC. Had to examine bean wiring.

# Scalability Limitations and Future Improvements
* The system can serve up to 500 requests/sec I think; The aggregation task processes aggregations every 5 seconds and I think can perform aggregation for 2500 raw flows in a couple of seconds.
* If it keeps falling behind, ForkJoinPool tasks can be created to process per hour aggregations separately.
* The main scaling limits in the system arise from the need to maintain high ingestion rate while serving results with reasonable freshness.
* Ingestion throughput and Aggregation Tasks collide for the same CPU cores. It would be better if these tasks are performed on different machines.

*Various Improvements are listed in the code in TODO.
* A Summary of them are
  * Using Kafka or another high throughput message bus to write flow logs to improve durability
* Using optimized index memory buffers to store raw flows in real time store
* Periodically persist indices on local storageso that real-time nodes do not run out of memory; Make these indices queryable
* Have Compaction tasks in background which collate these smaller persisted indices into Immutable segments.
* Announce to Historical and other querying nodes that you are no longer serving older timelines
* Using Columnar storage in all places for storage; Investigate Apache Arrow.
* Use Dictionary encoding to reduce size of large strings src_app, dest_app, vpc_id etc
* Use bitmap indices to be able to look at historical segments and query the counts for filters.
* Monitor aggregation/Indexing takss to make sure they are not falling behind.
* Investigate Delta Lake and Zorder indexing if you can read from cheap storage directly.
* Consider first to process raw flows and enrich them via a Spark Streaming/Structured Streaming app. Helps with
  * Enrichment of the flow with additional information
  * Stream join and correlating two ends of the same flow to created correlated_flow
  * Dealing with flows that may appear long after the time has elapsed ; For example hour 4 flow coming at hour 20.
  * Structured Streaming can be even better. As long as we read replayable sources like Kafka and submit aggregates to idempotent sinks, we can potentially get real-time fault tolerant aggregates without we doing much work.
  * Checkpointing and State Storage is taken care of by Structured Streaming.
  * Fault Tolerance is taken care of by Structured Streaming


## Getting Started


### Dependencies



### Installing


### Executing program

* How to run the program
* Step-by-step bullets
```
./mvnw clean install
```
```
./mvnw spring-boot:run
```

Then send curl commands in docs/sample_curl.txt ; You may want to use bash shell for running curl commands



## Authors

Contributors names and contact info

Radha Popuri

## Version History





## Acknowledgments

