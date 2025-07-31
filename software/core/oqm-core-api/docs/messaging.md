# Messaging

In order to support asynchronous, downstream, and event-driven practices, this component optionally supports connecting to a [Kafka](https://kafka.apache.org/) instance in order send events and facilitate these patterns.

Future work might include reading events from messaging channels as well.

## History Event Streaming

This service primarily publishes messages on the history queues. This occurs any time the inventory state is changed.

The data sent is the history event object that was recorded for the action. The topic the message is published to is specific to the object type.

TODO:: review and include example topics

## Configuration
