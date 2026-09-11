package tech.ebp.oqm.plugin.imageSearch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationScoped
public class ImageToItemCache {

	/**
	 *
	 */
	private final Map<String, DbCacheEntry> cacheEntries = new ConcurrentHashMap<>();

	@RestClient
	OqmCoreApiClientService oqmClient;

	@Inject
	KcClientAuthService authService;

	@WithSpan
	public Set<String> getItemsForImage(String oqmDb, String imageId) {
		DbCacheEntry dbEntry = cacheEntries.computeIfAbsent(oqmDb, k->new DbCacheEntry());

		CacheEntry imageCacheEntry = dbEntry.getCacheEntries().computeIfAbsent(
			imageId,
			k->{
				log.info("Cache miss, calling core api for items using image: {}", imageId);
				return oqmClient.invItemSearch(
						this.authService.getAuthString(),
						oqmDb,
						InventoryItemSearch.builder()
							.hasImages(List.of(imageId))
							.build()
					)
						   .map((ObjectNode results)->{
							   Set<String> itemIds = new HashSet<>();

							   for (JsonNode curResult : results.get("results")) {
								   itemIds.add(
									   curResult.get("id").asText()
								   );
							   }

							   CacheEntry output = new CacheEntry(
								   Set.copyOf(itemIds),
								   LocalDateTime.now().plus(Duration.ofMinutes(1))
							   );

							   log.info("Got new cache entry for items using image: {} / {}", imageId, output);

							   return output;
						   })
						   .await()
						   .indefinitely();
			}
		);

		return imageCacheEntry.itemIds();
	}

	@Scheduled(every = "1m")
	public void clearExpired(){
		log.info("Searching for and clearing expired cache entries.");
		for(Map.Entry<String, DbCacheEntry> entry : this.cacheEntries.entrySet()){
			DbCacheEntry dbCache = entry.getValue();

			dbCache.getCacheEntries().entrySet().removeIf(e->e.getValue().isExpired());
		}
		log.info("Done Searching for and clearing expired cache entries.");
	}

	private record CacheEntry(Set<String> itemIds, LocalDateTime expires) {
		public boolean isExpired() {
			return LocalDateTime.now().isAfter(this.expires());
		}
	}

	@Data
	@NoArgsConstructor
	private static class DbCacheEntry {

		/**
		 * Item ID -> CacheEntry
		 */
		private final Map<String, CacheEntry> cacheEntries = new ConcurrentHashMap<>();
	}
}
