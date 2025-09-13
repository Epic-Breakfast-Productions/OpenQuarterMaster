package tech.ebp.oqm.core.api.service.mongo;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.UniqueIdentifierGenerator;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
class UniqueIdentifierGenerationServiceTest extends RunningServerTest {
	
	@Inject
	UniqueIdentifierGenerationService uniqueIdentifierGenerationService;
	
	public static Stream<Arguments> getGenerationValidTestArgs() {
		return Stream.of(
			/*
			 * Basics/ individual
			 */
			//datetime
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{dt}").build(), "^\\d{2}/\\d{2}/\\d{4}-\\d{2}:\\d{2}:\\d{2}$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{dt;yyyy-MM-dd}").build(), "^\\d{4}-\\d{2}-\\d{2}$"),
			//uuid
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{uuid}").build(), "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"),
			//random
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{rand}").build(), "^[0-9a-zA-Z]{3}$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{rand;1}").build(), "^[0-9a-zA-Z]{1}$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{rand;5}").build(), "^[0-9a-zA-Z]{5}$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{rand;50}").build(), "^[0-9a-zA-Z]{50}$"),
			//increment
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc}").build(), "^00001$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;1}").build(), "^1$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;50}").build(), "^0{49}1$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;3;2}").build(), "^001$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;3;36}").build(), "^001$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc}").lastIncremented(BigInteger.TWO).build(), "^00003$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;2}").lastIncremented(new BigInteger("100", 10)).build(), "^101$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;2;36}").lastIncremented(new BigInteger("34", 10)).build(), "^0Z$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;2;36}").lastIncremented(new BigInteger("35", 10)).build(), "^10$"),
			/*
			 * Combined
			 */
			//prefix/suffix
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("foo-{inc}").build(), "^foo-00001$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc}-foo").build(), "^00001-foo$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("bar-{inc}-foo").build(), "^bar-00001-foo$"),
			//multiple
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc}{rand}").build(), "^00001[0-9a-zA-Z]{3}$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("foo-{inc}{rand}-bar").build(), "^foo-00001[0-9a-zA-Z]{3}-bar$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("foo-{inc}-{rand}-bar").build(), "^foo-00001-[0-9a-zA-Z]{3}-bar$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("foo-{inc}-some-{rand}-bar").build(), "^foo-00001-some-[0-9a-zA-Z]{3}-bar$")
		);
	}
	
	public static Stream<Arguments> getGenerationInValidTestArgs() {
		return Stream.of(
			//blank things
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("\t").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat(" ").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("\n").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("  {dt}").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{dt} ").build()),
			//no args in format
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("test").build()),
			//Bad args
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{}").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{test}").build()),
			//bad rand
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{rand;-1}").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{rand;0}").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{rand;51}").build()),
			//bad increment
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;-1}").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;51}").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;3;37}").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc;3;1}").build()),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc}{inc}").build())
		);
	}
	
	public static Stream<Arguments> getThreadTestParams() {
		return Stream.of(
			Arguments.of(2, 10),
			Arguments.of(3, 10),
			Arguments.of(5, 10),
			Arguments.of(10, 10),
			Arguments.of(20, 20)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getGenerationValidTestArgs")
	public void validFormatTest(UniqueIdentifierGenerator generator, String expectedFormat) {
		StopWatch sw = StopWatch.createStarted();
		String result = UniqueIdentifierGenerationService.getNextUniqueId(generator);
		sw.stop();
		log.info("Generated ID in {}: {}", sw, result);
		
		assertNotNull(result);
		assertTrue(
			result.matches(expectedFormat),
			"Generated ID (\"" + result + "\") does not match expected format: " + expectedFormat
		);
	}
	
	@ParameterizedTest
	@MethodSource("getGenerationInValidTestArgs")
	public void invalidFormatTest(UniqueIdentifierGenerator generator) {
		assertThrows(IllegalArgumentException.class, ()->UniqueIdentifierGenerationService.getNextUniqueId(generator));
	}
	
	@Test
	public void getNewIdTest() {
		UniqueIdentifierGenerator gen = UniqueIdentifierGenerator.builder()
											.generatorName(FAKER.name().name())
											.idFormat("{dt}-{rand}")
											.build();
		
		this.uniqueIdentifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen);
		
		String output = this.uniqueIdentifierGenerationService.getNextUniqueId(DEFAULT_TEST_DB_NAME, gen.getId());
		
		log.info("Generated ID: {}", output);
		
		assertNotNull(output);
	}
	
	@Test
	public void incrementTest() {
		UniqueIdentifierGenerator gen = UniqueIdentifierGenerator.builder()
											.generatorName(FAKER.name().name())
											.idFormat("{inc}")
											.build();
		
		this.uniqueIdentifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen);
		
		String output = this.uniqueIdentifierGenerationService.getNextUniqueId(DEFAULT_TEST_DB_NAME, gen.getId());
		
		log.info("Generated ID: {}", output);
		
		assertEquals("00001", output);
		
		output = this.uniqueIdentifierGenerationService.getNextUniqueId(DEFAULT_TEST_DB_NAME, gen.getId());
		
		log.info("Second Generated ID: {}", output);
		
		assertEquals("00002", output);
	}
	
	
	@ParameterizedTest
	@MethodSource("getThreadTestParams")
	public void incrementThreadTest(int numThreads, int numIterations) throws InterruptedException, ExecutionException {
		UniqueIdentifierGenerator gen = UniqueIdentifierGenerator.builder()
											.generatorName(FAKER.name().name())
											.idFormat("{inc}")
											.build();
		this.uniqueIdentifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen);
		
		List<Future<List<String>>> futures = new ArrayList<>(numThreads);
		SortedSet<String> results = new TreeSet<>();
		
		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		
		TestThread.Builder threadBuilder = TestThread.builder()
											   .generatorId(gen.getId())
											   .numIterations(numIterations)
											   .uniqueIdentifierGenerationService(this.uniqueIdentifierGenerationService);
		
		for (int i = 1; i <= numThreads; i++) {
			threadBuilder.threadId("testThread-" + i);
			
			futures.add(executor.submit(threadBuilder.build()));
		}
		executor.shutdown();
		while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
			log.info("Still waiting on threads...");
		}
		
		for (Future<List<String>> future : futures) {
			results.addAll(future.get());
		}
		
		assertEquals(numIterations * numThreads, results.size());
	}
	
	@Builder
	@Slf4j
	@AllArgsConstructor
	static class TestThread implements Callable<List<String>> {
		
		private String threadId;
		private ObjectId generatorId;
		private UniqueIdentifierGenerationService uniqueIdentifierGenerationService;
		private int numIterations;
		
		@SneakyThrows
		@Override
		public List<String> call() {
			log.info("Running test thread {}", this.threadId);
			
			List<String> results = new ArrayList<>(this.numIterations);
			
			for (int i = 1; i <= this.numIterations; i++) {
				results.add(
					this.uniqueIdentifierGenerationService.getNextUniqueId(DEFAULT_TEST_DB_NAME, this.generatorId)
				);
			}
			log.info("DONE running test thread {}", this.threadId);
			return results;
		}
	}
	
}