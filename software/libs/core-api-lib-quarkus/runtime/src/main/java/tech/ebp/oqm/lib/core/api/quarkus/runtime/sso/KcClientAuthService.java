package tech.ebp.oqm.lib.core.api.quarkus.runtime.sso;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.quarkus.scheduler.Scheduler;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Singleton
public class KcClientAuthService {
	public static final String SERVICE_ACCOUNT_TOKEN_REFRESH_JOB_NAME = "Service Account Token Refresh";

	@RestClient
	KeycloakRestClient keycloakRestClient;
	
	@Inject
	Scheduler scheduler;

	private final ReentrantLock mutex = new ReentrantLock();
	private String authString = null;

	public ObjectNode getSetNewToken(ScheduledExecution scheduledExecution){
		log.info("Getting new service account token.");
		ObjectNode result = this.keycloakRestClient.getServiceAccountToken();
		String jwt = result.get("access_token").asText();
		String bearer = "Bearer " + jwt;

		try{
			this.mutex.lock();
			this.authString = bearer;
		} finally {
			this.mutex.unlock();
		}
		log.info("Got new service account token.");
		log.debug(
			"New service account auth token: {} / {}",
			jwt,
			JWT.parse(jwt)
		);

		return result;
	}

	@PostConstruct
	public synchronized void setup(){
		log.info("Initializing KC Auth Service.");
		ObjectNode result = this.getSetNewToken(null);

		log.info("Scheduling job to retrieve new tokens before expiration.");
		String refreshAfter = ((result.get("expires_in").asInt() / 4) * 3) + "s";
		this.scheduler.newJob(SERVICE_ACCOUNT_TOKEN_REFRESH_JOB_NAME)
			.setInterval(refreshAfter)
			.setTask(this::getSetNewToken)
			.setConcurrentExecution(Scheduled.ConcurrentExecution.SKIP)
			.schedule();

		log.info("Scheduled job to retrieve new tokens before expiration. Occurs every {}", refreshAfter);
	}
	
	public String getAuthString(){
		try{
			this.mutex.lock();

			return authString;
		} finally {
			this.mutex.unlock();
		}
	}
}
