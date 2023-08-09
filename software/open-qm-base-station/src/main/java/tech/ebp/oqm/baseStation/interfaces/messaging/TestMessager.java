package tech.ebp.oqm.baseStation.interfaces.messaging;

import io.smallrye.reactive.messaging.annotations.Blocking;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Random;

@Slf4j
@ApplicationScoped
public class TestMessager {
	
	@Channel("randNumbers")
	Emitter<Integer> quoteRequestEmitter;
	
	private final Random random = new Random();
	
//	@Scheduled(every = "5s")
	public void sendNum(){
		quoteRequestEmitter.send(random.nextInt());
	}
	
	@Incoming("randNumbers")
	@Outgoing("oddOrEven")
	@Blocking
	public String processNums(Integer num) throws InterruptedException {
		return num + (num % 2 == 0 ? " is Even" : " is Odd");
	}
	
	@Incoming("oddOrEven")
	@Blocking
	public void processOddEven(String oddOrEven) throws InterruptedException {
		log.info("Odd or even? {}", oddOrEven);
	}

}
