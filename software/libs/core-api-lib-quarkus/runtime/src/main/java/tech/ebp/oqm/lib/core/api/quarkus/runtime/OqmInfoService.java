package tech.ebp.oqm.lib.core.api.quarkus.runtime;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.quarkus.scheduler.Scheduled;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

import java.util.Currency;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service to cache oqm db entries.
 *
 * Maybe move to core api extension?
 */
@Named("OqmInfoService")
@Slf4j
@ApplicationScoped
public class OqmInfoService {

	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;

	@Inject
	KcClientAuthService serviceAccountService;

	private final ReentrantLock mutex = new ReentrantLock();
	private volatile Currency currency = null;
	private boolean enabled = false;

	@PostConstruct
	public void setup(){
		if(
			ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.client-id", String.class).isEmpty() ||
			ConfigProvider.getConfig().getOptionalValue("quarkus.oidc.credentials.secret", String.class).isEmpty()
		){
			log.info("No OIDC creds. Disabled.");
			return;
		}
		this.enabled = true;


		this.refreshCache();
	}

	//TODO:: instead of this, watch for message? Both?
	@Scheduled(every = "{" + Constants.CONFIG_ROOT_NAME + ".caching.info.refreshFrequencyEvery}")
	public void refreshCache(){
		if(!enabled){
			return;
		}
		log.info("Refreshing cache of OQM databases.");
		Currency newCurrency = this.oqmCoreApiClientService.getCurrency(this.serviceAccountService.getAuthString()).await().indefinitely();
		log.debug("Got new currency listing: {}", newCurrency);
		try {
			this.mutex.lock();
			this.currency = newCurrency;
		} finally {
			this.mutex.unlock();
		}
	}

	public Currency getCurrency() {
		log.info("Getting cached Currency.");
		return this.currency;
//		try {
//			this.mutex.lock();
//			return this.currency;
//		} finally {
//			this.mutex.unlock();
//		}
	}
}
