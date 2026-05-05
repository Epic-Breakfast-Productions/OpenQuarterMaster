package tech.ebp.oqm.core.characteristics.services;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.characteristics.model.ui.Uis;

@Slf4j
@ApplicationScoped
public class UisService {
	private static final String CACHE_NAME = "uis-cache";
	
	@CacheResult(cacheName = CACHE_NAME)
	public Uis getUis() {
		return null; //TODO
	}
	
	@CacheInvalidate(cacheName = CACHE_NAME)
	public void newFile(){
		log.info("Invalidating cache.");
	}
}
