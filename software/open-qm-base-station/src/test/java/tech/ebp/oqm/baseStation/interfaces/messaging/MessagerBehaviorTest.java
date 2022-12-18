package tech.ebp.oqm.baseStation.interfaces.messaging;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
public class MessagerBehaviorTest extends RunningServerTest {
	
	@Broadcast
	@Channel("outgoing")
	Emitter<Integer> quoteRequestEmitter;
	
	private final Random random = new Random();
	
	private final List<Integer> receivedOne = new ArrayList<>(100);
	private final List<Integer> receivedTwo = new ArrayList<>(100);
	
	@Incoming("outgoing")
	@Blocking
	public void consumerOne(Integer num) throws InterruptedException {
		this.receivedOne.add(num);
	}
	@Incoming("outgoing")
	@Blocking
	public void consumerTwo(Integer num) throws InterruptedException {
		this.receivedTwo.add(num);
	}
	
	
	@Test
	public void testIsBroadcast(){
		for(int i = 0; i < 100; i++){
			this.quoteRequestEmitter.send(this.random.nextInt());
		}
		
		assertEquals(receivedOne, receivedTwo);
	}










}
