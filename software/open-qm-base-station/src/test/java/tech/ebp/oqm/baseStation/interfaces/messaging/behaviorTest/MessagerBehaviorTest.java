package tech.ebp.oqm.baseStation.interfaces.messaging.behaviorTest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;

import javax.inject.Inject;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class MessagerBehaviorTest extends RunningServerTest {
	
	public static final int NUM_MESSAGES = 100;
	
	@Broadcast
	@Channel("outgoingTest")
	Emitter<Integer> quoteRequestEmitter;
	
	@Inject
	BehaviorTestConsumers consumers;
	
//	@ConfigProperty(name = "mp.messaging.outgoing.outgoingTest.connector")
//	String configConnector;
//	@ConfigProperty(name = "amqp-host")
//	String configHost;
//	@ConfigProperty(name = "amqp-port")
//	String configPort;
//	@ConfigProperty(name = "amqp-username", defaultValue = "")
//	String configUsername;
//	@ConfigProperty(name = "amqp-password", defaultValue = "")
//	String configPassword;
	
	private final Random random = new Random();
	
	@Test
	public void testIsBroadcast() throws ExecutionException, InterruptedException {
//		log.info("Start sending messages. Config: {}", this.configConnector);
//		log.debug(
//			"Host: {}, Port: {}",// User: {}, Pass: {}",
//			this.configHost,
//			this.configPort
////			this.configUsername,
////			this.configPassword
//		);
		for (int i = 1; i <= NUM_MESSAGES; i++) {
			log.debug("Sending {}", i);
			this.quoteRequestEmitter.send(this.random.nextInt()).toCompletableFuture().get();
			log.debug("Sent {}", i);
		}
		log.info("END sending messages.");
		
		assertEquals(NUM_MESSAGES, this.consumers.getReceivedOne().size());
		assertEquals(NUM_MESSAGES, this.consumers.getReceivedTwo().size());
		
		assertEquals(this.consumers.getReceivedOne(), this.consumers.getReceivedTwo());
		log.info("End result: {}", this.consumers.getReceivedOne());
	}
}
