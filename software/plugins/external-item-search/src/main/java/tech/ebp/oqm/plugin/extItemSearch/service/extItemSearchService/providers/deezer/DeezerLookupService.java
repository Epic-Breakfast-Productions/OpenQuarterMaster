package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.providers.deezer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupProviderInfo;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResult;
import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.LookupResultNoResults;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeezerLookupService extends ItemSearchService {

	private final DeezerLookupClient deezerLookupClient;

	public DeezerLookupService(
		@RestClient
		DeezerLookupClient deezerLookupClient,
		@ConfigProperty
			(name = "productLookup.providers.openlibrary.enabled", defaultValue = "true") boolean enabled) {
		super(
			enabled,
			LookupService.DEEZER,
			ExtItemLookupProviderInfo.
				builder()
				.displayName("Deezer")
				.description("Music streaming app that gives you access to over 120 million tracks worldwide and other audio content like podcasts.")
				.cost("free")
				.acceptsContributions(false)
				.homepage(URI.create("https://deezer.com")));
		this.deezerLookupClient = deezerLookupClient;
	}

	@Override
	protected Multi<LookupResult> performSearch(LookupSource source, LookupMethod lookupMethod, String term) {
		return switch (source) {
			case DEEZER -> switch (lookupMethod) {
				case ALBUM -> this.deezerLookupClient.search(term)
					.onItem().transformToMulti(result -> {
						if (result.has("error")) {
							throw new IllegalStateException(getErrorMessage(result));
						}

						JsonNode data = result.get("data");
						if (data == null || !data.isArray() || data.isEmpty()) {
							return Multi.createFrom().item(this.setupResponseBuilder(LookupResultNoResults.builder(), source, lookupMethod).detail("No albums found.").build());
						}

						return this.deezerLookupClient.getAlbum(data.get(0).get("id").asText())
							.map(album -> mapJsonToResponse(source, lookupMethod, album))
							.toMulti();
					})
					.onFailure().recoverWithItem(e -> this.handleError(source, lookupMethod, e));
				default -> throw new IllegalArgumentException("Invalid lookup method: " + lookupMethod);
			};
			default -> throw new IllegalArgumentException("Invalid lookup source: " + source);
		};
	}

	private LookupResult mapJsonToResponse(LookupSource source, LookupMethod lookupMethod, ObjectNode result) {
		if (result.has("error")) {
			throw new IllegalStateException(getErrorMessage(result));
		}

		ExtItemLookupResult.ExtItemLookupResultBuilder<?, ?> resultBuilder = this.setupResponseBuilder(ExtItemLookupResult.builder(), source, lookupMethod);
		JsonNode artist = result.get("artist");
		StringBuilder unifiedName = new StringBuilder();
		Map<String, String> attributes = new HashMap<>();
		Map<String, String> links = new HashMap<>();
		List<String> images = new ArrayList<>();

		String name = result.get("title").asText();
		unifiedName.append(artist.get("name").asText()).append(" - ").append(result.get("title").asText());
		attributes.put("track_numbers", result.get("nb_tracks").asText());
		attributes.put("duration", result.get("duration").asText());
		attributes.put("release_date", result.get("release_date").asText());
		links.put("deezer_link", result.get("link").asText());
		images.add(result.get("picture").asText());

		return resultBuilder
			.name(name)
			.unifiedName(unifiedName.toString())
			.attributes(attributes)
			.images(images)
			.links(links)
			.build();
	}

	private static String getErrorMessage(ObjectNode result) {
		JsonNode error = result.get("error");
		return error != null && error.has("message") ? error.get("message").asText() : "Deezer returned an error response.";
	}
}
