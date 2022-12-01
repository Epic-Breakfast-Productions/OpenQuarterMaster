package tech.ebp.oqm.baseStation.interfaces.messaging;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Traced
@Slf4j
@ApplicationScoped
public class TestMessagerConsumer implements Runnable{
	
	@Inject
	ConnectionFactory connectionFactory;
	
	private final ExecutorService scheduler = Executors.newSingleThreadExecutor();
	
	private volatile String lastPrice;
	
	public String getLastPrice() {
		return lastPrice;
	}
	
	void onStart(@Observes StartupEvent ev) {
		scheduler.submit(this);
	}
	
	void onStop(@Observes ShutdownEvent ev) {
		scheduler.shutdown();
	}
	
	@Override
	public void run() {
		try (JMSContext context = connectionFactory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
			JMSConsumer consumer = context.createConsumer(context.createQueue("prices"));
			while (true) {
				Message message = consumer.receive();
				if (message == null) return;
				lastPrice = message.getBody(String.class);
				log.info("Got message: {}", lastPrice);
			}
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

}
