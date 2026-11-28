package tech.ebp.oqm.plugin.imageSearch.service.imageSearch;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.*;

import tech.ebp.oqm.plugin.imageSearch.model.Model;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.search.SearchResults;
import tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers.resnet.ResnetProvider;

@Slf4j
@ApplicationScoped
public class ImageSearchService {

	@Inject
	ResnetProvider resnetProvider;

	@ConfigProperty(name = "image-search.defaults.models")
	Set<Model> defaultModels;

	@PostConstruct
	void validateConfig(){
		if(this.defaultModels == null || this.defaultModels.isEmpty()){
			throw new IllegalStateException("Default Models (image-search.defaults.models) cannot be null or empty.");
		}
	}

	/**
	 *
	 * @param query
	 * @return
	 */
	@WithSpan
	public SearchResults search(ImageSearch query) throws IOException {
		log.info("Searching for query: {}", query);

		SearchResults output = new SearchResults(query.maxResults);

		Set<Model> modelsToRun = query.models;

		if(modelsToRun.isEmpty()){
			log.info("No models explicitly specified. Running default(s): {}", this.defaultModels);
			modelsToRun = this.defaultModels;
		}

		if(modelsToRun.contains(Model.RESNET_v2)) {
			this.resnetProvider.search(query, output);
		}

		return output;
	}
}
