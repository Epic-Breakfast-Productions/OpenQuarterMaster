package tech.ebp.oqm.plugin.imageSearch.model.search;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;


/**
 * Thread-safe accumulator for image-search results across multiple items.
 * <p>
 * Each call to {@link #add(ImageFinding)} adds a similarity score for an item-image pair.
 * Scores are grouped by item and capped at a configurable threshold so memory stays bounded.
 *
 * <h3>Example</h3>
 * <pre>{@code
 * SearchResults results = SearchResults.builder()
 *     .numImageResultsThreshold(50)
 *     .build();
 *
 * for (ImageFinding f : findings) {
 *     results.add(f);
 * }
 *
 * }</pre>
 */
@Slf4j
@Data
@Setter(AccessLevel.PROTECTED)
public class SearchResults {

	/**
	 * Results for the search.
	 */
	private TreeSet<SearchResult> results = new TreeSet<>();

	/**
	 * Number of individual item-image score entries to retain before
	 * evicting the lowest-scoring ones.
	 */
	private long numImageResultsThreshold = 0;
	private long numResultsTotal = 0;
	private long numResults = 0;

	private double averageScore = 0;
	private double maxScore = 0;
	private double minScore = 0;


	@Builder
	public SearchResults(long numImageResultsThreshold) {
		this.setNumImageResultsThreshold(numImageResultsThreshold);
	}

	public int getNumItems() {
		return this.results.size();
	}

	/**
	 * Recalculates aggregate statistics (average, max, min score)
	 * from the current set of results. Called automatically after each add.
	 */
	private void recalculate() {
		this.averageScore = this.results.stream()
								.mapToDouble(SearchResult::getAverageScore)
								.average().orElse(0);
		this.maxScore = this.results.stream()
							.mapToDouble(SearchResult::getMaxScore)
							.max().orElse(0);
		this.minScore = this.results.stream()
							.mapToDouble(SearchResult::getMinScore)
							.min().orElse(0);
	}

	/**
	 * Adds a single image finding (item-image score pair) to the results.
	 * <p>
	 * Scores are grouped by item ID. If the total number of retained
	 * item-image entries exceeds the configured threshold, the lowest-scoring
	 * entries are evicted. Aggregate stats are recalculated after each add.
	 *
	 * @param f the finding to add; must not be null
	 */
	public synchronized void add(ImageFinding f) {
		Optional<SearchResult> resultOp = this.results.stream()
											  .filter(r->r.getItem().equals(f.itemId))
											  .findFirst();

		SearchResult result;
		if (resultOp.isPresent()) {
			result = resultOp.get();
			this.results.remove(result);
		} else {
			result = SearchResult.builder()
						 .item(f.getItemId())
						 .build();
		}

		this.results.remove(result);

		try {
			result.addScore(f.toImageModel());
		} finally {
			this.results.add(result);
		}

		this.numResultsTotal++;
		this.numResults++;

		this.cull();
		this.recalculate();
	}

	private void cull() {
		while (this.numResults > this.numImageResultsThreshold) {
			Optional<SearchResult> rOp = this.results.stream().min(Comparator.comparingDouble(SearchResult::getMinScore));

			if (rOp.isEmpty()) {
				throw new IllegalStateException("Failed to find a minimum valued result. Should not happen.");
			}

			SearchResult result = rOp.get();

			if (!result.hasImages()) {
				throw new IllegalStateException("Current item result chosen to cull from is empty. Should not happen.");
			}

			result.removeMin();

			if (!result.hasImages()) {
				if (!this.results.remove(result)) {
					throw new IllegalStateException("Failed to remove empty item result. Should not happen.");
				}
			}

			this.numResults--;
		}
	}
}
