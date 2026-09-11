package tech.ebp.oqm.plugin.imageSearch.model.search;


import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.DoubleStream;

/**
 * Holds all image-model scores for a single item within a search.
 * <p>
 * Instances are sorted by descending max score, then by item ID,
 * so they appear best-match-first in a {@code TreeSet&lt;SearchResult&gt;}.
 */
@Data
@Setter(AccessLevel.PROTECTED)
public class SearchResult implements Comparable<SearchResult> {

	@Builder
	public SearchResult(@NonNull String item){
		this.item = item;
		this.recalc();
	}

	/**
	 * The item this is a result for
	 */
	@NonNull
	@NotNull
	private String item;

	/**
	 * Image id to score for the image.
	 */
	private TreeSet<ImageModelResult> images = new TreeSet<>();

	private double averageScore;
	private double maxScore;
	private double minScore;

	private DoubleStream getValueStream() {
		return this.images
				   .stream()
				   .mapToDouble(ImageModelResult::getScore);
	}

	/**
	 * Returns the number of image-model score entries for this item.
	 */
	public int getNumResults(){
		return this.images.size();
	}

	/**
	 * Returns {@code true} if this result has at least one image-model score.
	 */
	public boolean hasImages(){
		return !this.images.isEmpty();
	}

	private double maxScore() {
		return this.getValueStream().max().orElse(0);
	}
	private double minScore() {
		return this.getValueStream().min().orElse(0);
	}

	private double avgScore() {
		return this.getValueStream().average().orElse(0);
	}

	private void recalc(){
		this.averageScore = this.avgScore();
		this.maxScore = this.maxScore();
		this.minScore = this.minScore();
	}

	/**
	 * Adds a new image-model score to this result.
	 *
	 * @param image the score entry to add
	 * @throws IllegalArgumentException if an entry with the same image ID and model already exists
	 */
	public synchronized void addScore(ImageModelResult image) {
		if(
			this.getImages()
				.stream()
				.anyMatch((r)->{
					return r.getImageId().equals(image.getImageId()) &&
					       r.getModel().equals(image.getModel());
				})
		){
			throw new IllegalArgumentException("Cannot add another score with same image id and model.");
		}

		this.images.add(image);
		this.recalc();
	}

	/**
	 * Removes the lowest-scoring image-model entry from this result.
	 *
	 * @throws NoSuchElementException if this result has no entries
	 */
	public void removeMin() throws NoSuchElementException {
		this.images.removeLast();

		this.recalc();
	}

	@Override
	public int compareTo(SearchResult searchResult) {
		int r = Double.compare(
			this.getMaxScore(),
			searchResult.getMaxScore()
		);

		if(r != 0){
			return r;
		}

		return this.item.compareTo(searchResult.getItem());
	}
}
