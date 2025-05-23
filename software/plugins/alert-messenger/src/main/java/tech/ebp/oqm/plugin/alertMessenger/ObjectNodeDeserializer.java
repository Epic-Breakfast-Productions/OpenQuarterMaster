package tech.ebp.oqm.plugin.alertMessenger;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class ObjectNodeDeserializer extends ObjectMapperDeserializer<ObjectNode> {
	public ObjectNodeDeserializer() {
		super(ObjectNode.class);
	}
}
