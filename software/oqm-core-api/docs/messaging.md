# Messaging

In order to support asynchronous, downstream, and event-driven practices, this component optionally supports connecting to a [Kafka](https://kafka.apache.org/) instance in order send events and facilitate these patterns.

Future work might include reading events from messaging channels as well.

## History Event Streaming

This service primarily publishes messages on the history queues. This occurs any time the inventory state is changed.

The data sent is the history event object that was recorded for the action. The topic the message is published to is specific to the object type.

Topics follow the pattern `oqm-core-<database-id>-<object>-<event>`.
For example `oqm-core-12345-InventoryItem-ADD`. Each event is also published on `oqm-core-all-events`.

## Configuration

Messaging is disabled by default. Enable it by providing the Kafka server address and turning on the outgoing channel:

```properties
mp.messaging.outgoing.events-outgoing.enabled=true
mp.messaging.outgoing.events-outgoing.bootstrap.servers=OUTSIDE://localhost:9092
```

`events-outgoing` matches the channel used by the service. Adjust the server address for your deployment.
