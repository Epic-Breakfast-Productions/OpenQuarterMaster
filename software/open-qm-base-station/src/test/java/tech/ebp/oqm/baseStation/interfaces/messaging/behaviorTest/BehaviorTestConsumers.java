package tech.ebp.oqm.baseStation.interfaces.messaging.behaviorTest;

import io.smallrye.reactive.messaging.annotations.Blocking;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Needs to be its own class, due to object raisins.
 */
@Slf4j
@ApplicationScoped
public class BehaviorTestConsumers {
	
	@Getter
	private List<Integer> receivedOne = new CopyOnWriteArrayList<>();
	@Getter
	private  List<Integer> receivedTwo = new CopyOnWriteArrayList<>();
	
	@Incoming("outgoingTest")
	@Blocking
	public void consumerOne(Integer num) {
		log.info("Received One {} - {} ({})", num, this.receivedOne.size() + 1, System.identityHashCode(this.receivedOne));
		this.receivedOne.add(num);
	}
	
	@Incoming("outgoingTest")
	@Blocking
	public void consumerTwo(Integer num) {
		log.info("Received Two {} - {}", num, this.receivedOne.size() + 1);
		this.receivedTwo.add(num);
	}
}
