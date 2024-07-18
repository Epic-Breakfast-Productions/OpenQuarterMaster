package tech.ebp.oqm.lib.core.api.quarkus.runtime.sso;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.scheduler.Scheduled;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@ApplicationScoped
public class KcClientAuthService {
	@RestClient
	KeycloakRestClient keycloakRestClient;

	private final ReentrantLock mutex = new ReentrantLock();
	private String authString = null;
	private LocalDateTime refreshTime = LocalDateTime.MIN;

//	@PostConstruct
//	@Scheduled
//	public void getNewToken(){
//		//TODO:: get new token here, run as another thread
//	}



	
	public synchronized String getAuthString(){
		try{
			this.mutex.lock();

			//TODO:: move entire if to new thread, mutex only when necessary
			if(
				this.authString == null
					|| this.refreshTime.isBefore(LocalDateTime.now())
			){
				log.info("Getting new service account token.");
				ObjectNode result = this.keycloakRestClient.getServiceAccountToken();
				String jwt = result.get("access_token").asText();
				this.authString = "Bearer " + jwt;
				int refreshAfter = (result.get("expires_in").asInt() / 3) * 2;
				this.refreshTime = LocalDateTime.now().plusSeconds(refreshAfter);
				log.info("Got new service account token, next refresh at {}", this.refreshTime);
				log.debug(
					"New service account auth token: {} / {}",
					jwt,
					JWT.parse(jwt)
				);
			}
			return authString;
		} finally {
			this.mutex.unlock();
		}
	}
}
