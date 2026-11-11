package tech.ebp.oqm.plugin.imageSearch.service.imageSearch.providers;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import tech.ebp.oqm.plugin.imageSearch.model.search.ImageSearch;
import tech.ebp.oqm.plugin.imageSearch.model.search.SearchResults;

public abstract class ImageSearchProvider {

	@WithSpan
	public abstract void search(ImageSearch query, SearchResults output);
}
