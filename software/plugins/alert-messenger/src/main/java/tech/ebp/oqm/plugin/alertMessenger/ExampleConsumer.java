// package tech.ebp.oqm.plugin.alertMessenger;

// import com.fasterxml.jackson.databind.node.ObjectNode;
// import jakarta.annotation.PostConstruct;
// import jakarta.enterprise.context.ApplicationScoped;
// import lombok.Getter;
// import lombok.extern.slf4j.Slf4j;
// import org.eclipse.microprofile.reactive.messaging.Incoming;
// import org.eclipse.microprofile.reactive.messaging.Message;

// import java.util.concurrent.CompletionStage;

// @Slf4j
// @ApplicationScoped
// public class ExampleConsumer {

// 	@Getter
// 	private boolean received = false;

// 	@PostConstruct
// 	public void init() {
// 		log.info("Starting ExampleConsumer");
// 	}

// 	@Incoming("oqm-core-all-events")
// 	public CompletionStage<Void> receive(Message<ObjectNode> message) {
// 		log.info("Received message {}", message.getPayload());
// 		this.received = true;
// 		return message.ack();
// 	}
// }
