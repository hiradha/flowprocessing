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
* A Summary of them are below 
* Common to all:
* Using Kafka or another high throughput message bus to write flow logs to improve durability
* Approach 1: Kafka+Our own Kafka Consumer Group+Delta Lake
  * Using <src_app, dest_app, vpc_id, hour> as partition key and run multiple Kafka Consumers which form a Consumer Group.
  * Then all messages with this particular tuple go to the same kafka consumer which can add them up
  * If at least once smenatics are ok ( I think has some support for exactly once too now I think), then merge counts to a Delta Lake table in S3 partition by <hour> ; You will see hourly buckets in S3.
  * Direct Read API's to go form Delta Lake table
  * If an hourly partition is too big, we can subdivide into hour_<15 min intervals> or we can partition by other dimensions and use that as partition key for Delta Lake table.
  * Investigate Delta Lake and Zorder indexing if you can read from cheap storage directly.
* Approach 2: Kafka+Structured Streaming App+Delta Lake
  * The approach is similar to above with a few differences
  * Structured Streaming supports exactly once semantics using Write Ahead Log and State store
  * The only expectation is that sources are replayable ( which Kafka is) and sinks support idempotent writes.
  * Due to above, we can potentially get real-time fault tolerant aggregates without we doing much work.
  * Structured Streaming supports additional features like 
    * triggers
    * watermarks
    * window sizes
    * late arrival policy
    * allowing processing by event time etc.
* Approach 3: Kafka+Druid 
  * Delivers freshest data and slice and dice OLAP queries at the expense of operational complexity and throughput for batch workloads over large data
  * Druid has multiple components- brokers, real-time nodes, hitorical nods, co ordinations nodes, zookeeper and S3/Deep storage.
  * Using optimized index memory buffers to store raw flows in real time store
  * Periodically persist indices on local storageso that real-time nodes do not run out of memory; Make these indices queryable
  * Have Compaction tasks in background which collate these smaller persisted indices into Immutable segments.
  * Announce to Historical and other querying nodes that you are no longer serving older timelines
  * Using Columnar storage in all places for storage; Investigate Apache Arrow.
  * Use Dictionary encoding to reduce size of large strings src_app, dest_app, vpc_id etc
  * Use bitmap indices to be able to look at historical segments and query the counts for filters.
  * Monitor aggregation/Indexing tasks to make sure they are not falling behind.

* Possible Additional Step in the pipeline:
  * Consider first to process raw flows and enrich them via a Spark Streaming/Structured Streaming app. Helps with
    * Enrichment of the flow with additional information
    * Stream join and correlating two ends of the same flow to created correlated_flow
    * Dedup both ends of the flow within a window


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

