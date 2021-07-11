This is a sample kafka project using plain vanilla Kafka Streams

**What is does**

There are three input streams
* trade data
* account data
* company data

There is one output stream
* trade summary data , which aggregates data account and company wise and publish the summary data to a Kafka topic.

**Concepts demoed**

* Joining 2 Kafka streams
* Creating global K table and materializing it
* Aggregation using Kstreams

