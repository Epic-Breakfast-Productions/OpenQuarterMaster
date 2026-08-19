package tech.ebp.oqm.plugin.imageSearch.model.search;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.imageSearch.model.Model;

import java.util.NoSuchElementException;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;


class SearchResultTest {

	@Test
	public void testConstructor(){
		SearchResult result = new SearchResult("testItem");

		assertEquals("testItem", result.getItem());
		assertEquals(new TreeSet<>(), result.getImages());
		assertEquals(0, result.getAverageScore());
		assertEquals(0, result.getMinScore());
		assertEquals(0, result.getMaxScore());

		assertFalse(result.hasImages());
	}

	@Test
	public void testAdd(){
		SearchResult result = new SearchResult("testItem");

		result.addScore(
			ImageModelResult.builder()
				.imageId("testImage")
				.model(Model.RESNET_v2)
				.score(0.5)
				.build()
		);

		assertEquals(1, result.getNumResults());
		assertEquals(0.5, result.getAverageScore());
		assertEquals(0.5, result.getMinScore());
		assertEquals(0.5, result.getMaxScore());
	}

	@Test
	public void testAddSecond(){
		SearchResult result = new SearchResult("testItem");

		result.addScore(
			ImageModelResult.builder()
				.imageId("testImage")
				.model(Model.RESNET_v2)
				.score(0.75)
				.build()
		);
		result.addScore(
			ImageModelResult.builder()
				.imageId("testImage2")
				.model(Model.RESNET_v2)
				.score(0.5)
				.build()
		);

		assertEquals(2, result.getNumResults());
		assertEquals(0.625, result.getAverageScore());
		assertEquals(0.5, result.getMinScore());
		assertEquals(0.75, result.getMaxScore());


		ImageModelResult first = result.getImages().first();
		ImageModelResult second = result.getImages().last();

		assertEquals("testImage", first.getImageId());
		assertEquals("testImage2", second.getImageId());
	}

	@Test
	public void testRemoveMinWithNone(){
		SearchResult result = new SearchResult("testItem");

		assertThrows(
			NoSuchElementException.class,
			()->result.removeMin()
		);

		assertEquals(0, result.getNumResults());
		assertEquals(0, result.getAverageScore());
		assertEquals(0, result.getMinScore());
		assertEquals(0, result.getMaxScore());
	}

	@Test
	public void testRemoveMinWithOne(){
		SearchResult result = new SearchResult("testItem");

		result.addScore(
			ImageModelResult.builder()
				.imageId("testImage")
				.model(Model.RESNET_v2)
				.score(0.75)
				.build()
		);

		result.removeMin();

		assertEquals(0, result.getNumResults());
		assertEquals(0, result.getAverageScore());
		assertEquals(0, result.getMinScore());
		assertEquals(0, result.getMaxScore());
	}

	@Test
	public void testRemoveMinWithTwo(){
		SearchResult result = new SearchResult("testItem");

		result.addScore(
			ImageModelResult.builder()
				.imageId("testImage")
				.model(Model.RESNET_v2)
				.score(0.75)
				.build()
		);
		result.addScore(
			ImageModelResult.builder()
				.imageId("testImage2")
				.model(Model.RESNET_v2)
				.score(0.5)
				.build()
		);

		result.removeMin();

		assertEquals(1, result.getNumResults());
		assertEquals(0.75, result.getAverageScore());
		assertEquals(0.75, result.getMinScore());
		assertEquals(0.75, result.getMaxScore());
	}

	@Test
	public void testAddDuplicateImageModel(){
		SearchResult result = new SearchResult("testItem");

		result.addScore(
			ImageModelResult.builder()
				.imageId("testImage")
				.model(Model.RESNET_v2)
				.score(0.75)
				.build()
		);

		assertThrows(
			IllegalArgumentException.class,
			()->{
				result.addScore(
					ImageModelResult.builder()
						.imageId("testImage")
						.model(Model.RESNET_v2)
						.score(0.5)
						.build()
				);
			}
		);
	}

	@Disabled
	@Test
	public void testComparison(){
		SearchResult o = new SearchResult("testItem");
//		o.addScore(ImageModelResult.builder()
//
//					   .build());

		//TODO:: test against higher, lower, equals

	}
}
