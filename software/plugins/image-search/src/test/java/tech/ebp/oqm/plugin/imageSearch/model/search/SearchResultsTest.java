package tech.ebp.oqm.plugin.imageSearch.model.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.imageSearch.model.Model;

import java.util.Comparator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
//@QuarkusTest
public class SearchResultsTest {

//	@Inject
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testCreate(){
		SearchResults results = new SearchResults(5);

		assertEquals(0, results.getNumItems());
		assertEquals(0, results.getNumResultsTotal());
		assertEquals(0, results.getNumResults());
		assertEquals(5, results.getNumImageResultsThreshold());
		assertEquals(0.0, results.getAverageScore());
		assertEquals(0.0, results.getMinScore());
		assertEquals(0.0, results.getMaxScore());
	}

	@Test
	public void testSerializationEmpty() throws JsonProcessingException {
		SearchResults results = new SearchResults(5);


		log.info("Results json: {}", objectMapper.writeValueAsString(results));

	}

	@Test
	public void testSerialization() throws JsonProcessingException {
		SearchResults results = new SearchResults(5);

		results.add(
			ImageFinding.builder()
				.itemId("item")
				.imageId("image")
				.model(Model.RESNET_v2)
				.score(0.5)
				.build()
		);

		log.info("Results json: {}", objectMapper.writeValueAsString(results));
	}

	@Test
	public void testAddOne() throws JsonProcessingException {
		SearchResults results = new SearchResults(5);

		results.add(
			ImageFinding.builder()
				.itemId("item")
				.imageId("image")
				.model(Model.RESNET_v2)
				.score(0.5)
				.build()
		);

		assertEquals(1, results.getNumResults());
		assertEquals(1, results.getNumResultsTotal());
		assertEquals(0.5, results.getMaxScore());
		assertEquals(0.5, results.getMinScore());
		assertEquals(0.5, results.getAverageScore());
	}

	@Test
	public void testAddUpToThresholdSameItem() throws JsonProcessingException {
		SearchResults results = new SearchResults(5);

		for(int i = 0; i < 5; i++) {
			results.add(
				ImageFinding.builder()
					.itemId("item")
					.imageId("image-"+i)
					.model(Model.RESNET_v2)
					.score(0.5)
					.build()
			);
		}

		assertEquals(5, results.getNumResults());
		assertEquals(1, results.getNumItems());
		assertEquals(5, results.getNumResultsTotal());
		assertEquals(0.5, results.getMaxScore());
		assertEquals(0.5, results.getMinScore());
		assertEquals(0.5, results.getAverageScore());
	}

	@Test
	public void testAddUpToThresholdDiffItem() throws JsonProcessingException {
		SearchResults results = new SearchResults(5);

		for(int i = 0; i < 5; i++) {
			results.add(
				ImageFinding.builder()
					.itemId("item-"+i)
					.imageId("image-"+i)
					.model(Model.RESNET_v2)
					.score(0.5)
					.build()
			);
		}

		assertEquals(5, results.getNumResults());
		assertEquals(5, results.getNumItems());
		assertEquals(5, results.getNumResultsTotal());
		assertEquals(0.5, results.getMaxScore());
		assertEquals(0.5, results.getMinScore());
		assertEquals(0.5, results.getAverageScore());
	}

	@Test
	public void testAddPastThresholdDiffItem() throws JsonProcessingException {
		SearchResults results = new SearchResults(5);

		for(int i = 0; i < 6; i++) {
			results.add(
				ImageFinding.builder()
					.itemId("item-"+i)
					.imageId("image-"+i)
					.model(Model.RESNET_v2)
					.score(i*0.1)
					.build()
			);
		}

		log.info("Results: {}", results);

		assertEquals(5, results.getNumResults());
		assertEquals(5, results.getNumItems());
		assertEquals(6, results.getNumResultsTotal());
		assertEquals(0.5, results.getMaxScore());
		assertEquals(0.1, results.getMinScore());
		assertEquals(0.3, results.getAverageScore());
	}

	@Test
	public void testAddManyPastThresholdDiffItem() throws JsonProcessingException {
		SearchResults results = new SearchResults(5);

		for(int i = 0; i < 1000; i++) {
			results.add(
				ImageFinding.builder()
					.itemId("item-"+(i%5))
					.imageId("image-"+i)
					.model(Model.RESNET_v2)
					.score((i%10)*0.1)
					.build()
			);
		}

		log.info("Results: {}", results);

		assertEquals(5, results.getNumResults());
		assertEquals(1, results.getNumItems());
		assertEquals(1000, results.getNumResultsTotal());
		assertEquals(0.9, results.getMaxScore());
		assertEquals(0.9, results.getMinScore());
		assertEquals(0.9, results.getAverageScore());
	}

	@Test
	public void testResultsComparatorInSet(){
		SearchResults results = new SearchResults(5);
		Comparator<? super SearchResult> c = results.getResults().comparator();

		SearchResult r1 = SearchResult.builder()
							  .item("item")
							  .build();

		results.getResults().add(r1);

		assertTrue(results.getResults().remove(r1));
	}

	@Test
	public void testAddManyPastThresholdRand() throws JsonProcessingException {
		SearchResults results = new SearchResults(25);

		Random random = new Random();

		for(int i = 0; i < 10_000; i++) {
			results.add(
				ImageFinding.builder()
					.itemId("item-"+(i%5))
					.imageId("image-"+i)
					.model(Model.RESNET_v2)
					.score(random.nextDouble(0,1))
					.build()
			);
		}

		log.info("Results: {}", results);

		assertEquals(25, results.getNumResults());
//		assertEquals(1, results.getNumItems());
		assertEquals(10_000, results.getNumResultsTotal());
//		assertEquals(0.9, results.getMaxScore());
//		assertEquals(0.1, results.getMinScore());
//		assertEquals(0.5, results.getAverageScore());
	}

	@Test
	public void testAddDuplicate(){
		SearchResults results = new SearchResults(5);

		results.add(
			ImageFinding.builder()
				.itemId("item")
				.imageId("image")
				.model(Model.RESNET_v2)
				.score(0.5)
				.build()
		);

		SearchResult og = results.getResults().first();

		assertThrows(
			IllegalArgumentException.class,
			()->results.add(
				ImageFinding.builder()
					.itemId("item")
					.imageId("image")
					.model(Model.RESNET_v2)
					.score(0.5)
					.build()
			)
		);

		assertEquals(1, results.getNumResults());
		assertEquals(1, results.getNumResultsTotal());
		assertEquals(0.5, results.getMaxScore());
		assertEquals(0.5, results.getMinScore());
		assertEquals(0.5, results.getAverageScore());

		assertTrue(results.getResults().contains(og));
	}

}
