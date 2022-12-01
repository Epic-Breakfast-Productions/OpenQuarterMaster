package tech.ebp.oqm.baseStation.interfaces.messaging;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Traced
@Slf4j
@ApplicationScoped
public class TestMessagerProducer implements Runnable{
	
	@Inject
	ConnectionFactory connectionFactory;
	
	private final Random random = new Random();
	private final ScheduledExecutorService producerScheduler = Executors.newSingleThreadScheduledExecutor();
	
	void onStart(@Observes StartupEvent ev) {
		producerScheduler.scheduleWithFixedDelay(this, 0L, 30L, TimeUnit.SECONDS);
	}
	
	void onStop(@Observes ShutdownEvent ev) {
		producerScheduler.shutdown();
	}
	
	@Override
	public void run() {
		try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
			context.createProducer().send(context.createQueue("prices"), Integer.toString(random.nextInt(100)));
		}
	}

}
