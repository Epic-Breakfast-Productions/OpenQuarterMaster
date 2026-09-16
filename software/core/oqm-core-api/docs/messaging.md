# Messaging

In order to support asynchronous, downstream, and event-driven practices, this component optionally supports connecting to a [Kafka](https://kafka.apache.org/) instance in order send events and facilitate these patterns.

Future work might include reading events from messaging channels as well.

## History Event Streaming

This service primarily publishes messages on the history queues. This occurs any time the inventory state is changed.

The data sent is the history event object that was recorded for the action. The topic the message is published to is specific to the object type.

TODO:: review and include example topics

## Configuration

If not using the messaging tooling, you can simply provide the following, which gracefully disables the event handlers:

`mp.messaging.outgoing.events-outgoing.enabled=false`

Otherwise, an example configuration of connecting to a Kafka instance would be:

```properties
mp.messaging.outgoing.events-outgoing.enabled=true
mp.messaging.outgoing.events-outgoing.bootstrap.servers=OUTSIDE://{infra.kafka.host}:{infra.kafka.port}
```

Further configuration reference can be found here: https://quarkus.io/guides/kafka#outgoing-channel-configuration-writing-to-kafka
