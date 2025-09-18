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
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdGenResult;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdentifierGenerator;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
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
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{rand}").build(), "^[0-9a-zA-Z]{5}$"),
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
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("{inc}{rand}").build(), "^00001[0-9a-zA-Z]{5}$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("foo-{inc}{rand}-bar").build(), "^foo-00001[0-9a-zA-Z]{5}-bar$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("foo-{inc}-{rand}-bar").build(), "^foo-00001-[0-9a-zA-Z]{5}-bar$"),
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("foo-{inc}-some-{rand}-bar").build(), "^foo-00001-some-[0-9a-zA-Z]{5}-bar$"),
			/*
			 * Other
			 */
			//encoding
			Arguments.of(UniqueIdentifierGenerator.builder().idFormat("foo-{inc}").encoded(true).build(), "^Zm9vLTAwMDAx$")
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
	
	
	@ParameterizedTest
	@MethodSource("getGenerationValidTestArgs")
	public void validFormatTest(UniqueIdentifierGenerator generator, String expectedFormat) {
		StopWatch sw = StopWatch.createStarted();
		String result = UniqueIdentifierGenerationService.getNextNUniqueIds(generator);
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
		assertThrows(IllegalArgumentException.class, ()->UniqueIdentifierGenerationService.getNextNUniqueIds(generator));
	}
	
	@Test
	public void getNewIdTest() {
		UniqueIdentifierGenerator gen = UniqueIdentifierGenerator.builder()
											.name(FAKER.name().name())
											.idFormat("{dt}-{rand}")
											.build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.uniqueIdentifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen, testUser);
		
		UniqueIdGenResult output = this.uniqueIdentifierGenerationService.getNextNUniqueIds(DEFAULT_TEST_DB_NAME, gen.getId(), 1);
		
		log.info("Generated ID: {}", output);
		
		assertNotNull(output);
		assertEquals(1, output.getGeneratedIds().size());
	}
	
	@Test
	public void incrementTest() {
		UniqueIdentifierGenerator gen = UniqueIdentifierGenerator.builder()
											.name(FAKER.name().name())
											.idFormat("{inc}")
											.build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.uniqueIdentifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen, testUser);
		
		UniqueIdGenResult output = this.uniqueIdentifierGenerationService.getNextNUniqueIds(DEFAULT_TEST_DB_NAME, gen.getId(), 1);
		
		log.info("Generated ID: {}", output);
		
		assertEquals("00001", output.getGeneratedIds().getFirst());
		
		output = this.uniqueIdentifierGenerationService.getNextNUniqueIds(DEFAULT_TEST_DB_NAME, gen.getId(), 1);
		
		log.info("Second Generated ID: {}", output);
		
		assertEquals("00002", output.getGeneratedIds().getFirst());
	}
	
	
	public static Stream<Arguments> getThreadTestParams() {
		List<Arguments> output = new ArrayList<>();
		
		for(int curNumThreads : List.of(2, 5, 10, 20)){
			for(int curNumIterations : List.of(10, 20)){
				for(int curNumPerIteration : List.of(1, 2, 5, 10, 20)){
					output.add(Arguments.of("{rand}", curNumThreads, curNumIterations, curNumPerIteration));
				}
			}
		}
		
		for(int curNumThreads : List.of(2, 10)){
			for(int curNumIterations : List.of(10)){
				for(int curNumPerIteration : List.of(1, 2, 5, 10, 20)){
					output.add(Arguments.of("{inc}", curNumThreads, curNumIterations, curNumPerIteration));
				}
			}
		}
		
		return output.stream();
	}
	
	@ParameterizedTest
	@MethodSource("getThreadTestParams")
	public void generateThreadTest(String format, int numThreads, int numIterations, int numPerIteration) throws InterruptedException, ExecutionException {
		UniqueIdentifierGenerator gen = UniqueIdentifierGenerator.builder()
											.name(FAKER.name().name())
											.idFormat(format)
//											.encoded(true)
											.build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.uniqueIdentifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen, testUser);
		
		List<Future<List<UniqueIdGenResult>>> futures = new ArrayList<>(numThreads);
		SortedSet<String> results = new TreeSet<>();
		
		StopWatch sw;
		try(ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			TestThread.Builder threadBuilder = TestThread.builder()
												   .generatorId(gen.getId())
												   .numIterations(numIterations)
												   .numPerIteration(numPerIteration)
												   .uniqueIdentifierGenerationService(this.uniqueIdentifierGenerationService);
			
			sw = StopWatch.createStarted();
			for (int i = 1; i <= numThreads; i++) {
				threadBuilder.threadId("testThread-" + i);
				
				futures.add(executor.submit(threadBuilder.build()));
			}
			executor.shutdown();
			while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
				log.info("Still waiting on threads...");
			}
			sw.stop();
		}
		
		for (Future<List<UniqueIdGenResult>> future : futures) {
			List<UniqueIdGenResult> curResultList = future.get();
			
			for (UniqueIdGenResult curResult : curResultList) {
				assertEquals(numPerIteration, curResult.getGeneratedIds().size());
				results.addAll(curResult.getGeneratedIds());
			}
		}
		
		assertEquals(numIterations * numThreads * numPerIteration, results.size());
		
		Duration avgDurationPerId = sw.getDuration().dividedBy(results.size());
		log.info("Generated {} results in {} ms ({} per id average)", results.size(), sw.getDuration(), avgDurationPerId);
		
		if(format.equals("{inc}")){
			
			BigInteger expected = BigInteger.ONE;
			for(String curResultStr : results){
				BigInteger curResult = new BigInteger(curResultStr);
				
				assertEquals(expected, curResult, "Was not a contiguous set of ids; expected: " + expected.toString() + " Got: " + curResult.toString());
				
				expected = expected.add(BigInteger.ONE);
			}
		}
	}
	
	@Builder
	@Slf4j
	@AllArgsConstructor
	static class TestThread implements Callable<List<UniqueIdGenResult>> {
		
		private String threadId;
		private ObjectId generatorId;
		private UniqueIdentifierGenerationService uniqueIdentifierGenerationService;
		private int numIterations;
		private int numPerIteration;
		
		@SneakyThrows
		@Override
		public List<UniqueIdGenResult> call() {
			log.info("Running test thread {}", this.threadId);
			
			List<UniqueIdGenResult> results = new ArrayList<>(this.numIterations);
			
			for (int i = 1; i <= this.numIterations; i++) {
				results.add(
					this.uniqueIdentifierGenerationService.getNextNUniqueIds(DEFAULT_TEST_DB_NAME, this.generatorId, this.numPerIteration)
				);
			}
			log.info("DONE running test thread {}", this.threadId);
			return results;
		}
	}
	
}