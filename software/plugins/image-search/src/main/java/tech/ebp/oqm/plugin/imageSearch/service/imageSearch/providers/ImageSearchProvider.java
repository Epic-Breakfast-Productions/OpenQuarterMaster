package tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.search.SearchResults;

public abstract class ImageSearchProvider {

	/**
	 *
	 * @param query
	 * @param output
	 */
	@WithSpan
	public abstract void search(ImageSearch query, SearchResults output);

	/**
	 * Determines if this service is currently processing data.
	 * @return If this service is currently processing data.
	 */
	public abstract boolean isProcessing();
}
