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
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralGeneratedId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.Generates;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.GeneratedUniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.IdGenResult;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.IdentifierGenerator;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.ProvidedUniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.ToGenerateUniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueId;
import tech.ebp.oqm.core.api.exception.db.DbModValidationException;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
class IdentifierGenerationServiceTest extends RunningServerTest {
	
	@Inject
	IdentifierGenerationService identifierGenerationService;
	
	public static Stream<Arguments> getGenerationValidTestArgs() {
		return Stream.of(
			/*
			 * Basics/ individual
			 */
			//datetime
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{dt}").build(), "^\\d{2}/\\d{2}/\\d{4}-\\d{2}:\\d{2}:\\d{2}$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{dt;yyyy-MM-dd}").build(), "^\\d{4}-\\d{2}-\\d{2}$"),
			//uuid
			Arguments.of(
				IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{uuid}").build(),
				"^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
			),
			//random
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{rand}").build(), "^[0-9a-zA-Z]{5}$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{rand;1}").build(), "^[0-9a-zA-Z]{1}$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{rand;5}").build(), "^[0-9a-zA-Z]{5}$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{rand;50}").build(), "^[0-9a-zA-Z]{50}$"),
			//increment
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc}").build(), "^00001$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;1}").build(), "^1$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;50}").build(), "^0{49}1$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;3;2}").build(), "^001$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;3;36}").build(), "^001$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc}").lastIncremented(BigInteger.TWO).build(), "^00003$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;2}").lastIncremented(new BigInteger("100", 10)).build(), "^101$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;2;36}").lastIncremented(new BigInteger("34", 10)).build(), "^0Z$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;2;36}").lastIncremented(new BigInteger("35", 10)).build(), "^10$"),
			/*
			 * Combined
			 */
			//prefix/suffix
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("foo-{inc}").build(), "^foo-00001$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc}-foo").build(), "^00001-foo$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("bar-{inc}-foo").build(), "^bar-00001-foo$"),
			//multiple
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc}{rand}").build(), "^00001[0-9a-zA-Z]{5}$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("foo-{inc}{rand}-bar").build(), "^foo-00001[0-9a-zA-Z]{5}-bar$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("foo-{inc}-{rand}-bar").build(), "^foo-00001-[0-9a-zA-Z]{5}-bar$"),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("foo-{inc}-some-{rand}-bar").build(), "^foo-00001-some-[0-9a-zA-Z]{5}-bar$"),
			/*
			 * Other
			 */
			//encoding
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("foo-{inc}").encoded(true).build(), "^Zm9vLTAwMDAx$")
		);
	}
	
	public static Stream<Arguments> getGenerationInValidTestArgs() {
		return Stream.of(
			//blank things
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("\t").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat(" ").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("\n").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("  {dt}").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{dt} ").build()),
			//no args in format
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("test").build()),
			//Bad args
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{}").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{test}").build()),
			//bad rand
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{rand;-1}").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{rand;0}").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{rand;51}").build()),
			//bad increment
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;-1}").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;51}").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;3;37}").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc;3;1}").build()),
			Arguments.of(IdentifierGenerator.builder().name("test").generates(Generates.UNIQUE).idFormat("{inc}{inc}").build())
		);
	}
	
	
	@ParameterizedTest
	@MethodSource("getGenerationValidTestArgs")
	public void validFormatTest(IdentifierGenerator generator, String expectedFormat) {
		StopWatch sw = StopWatch.createStarted();
		String result = IdentifierGenerationService.getNextId(generator);
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
	public void invalidFormatTest(IdentifierGenerator generator) {
		assertThrows(IllegalArgumentException.class, ()->IdentifierGenerationService.getNextId(generator));
	}
	
	@Test
	public void getNewGeneralIdTest() {
		IdentifierGenerator gen = IdentifierGenerator.builder()
									  .generates(Generates.GENERAL)
									  .name(FAKER.name().name())
									  .idFormat("{dt}-{rand}")
									  .build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen, testUser);
		
		IdGenResult<GeneralGeneratedId>
			output =
			(IdGenResult<GeneralGeneratedId>) this.identifierGenerationService.getNextNIds(DEFAULT_TEST_DB_NAME, gen.getId(), 1, Generates.GENERAL);
		
		log.info("Generated ID: {}", output);
		
		assertNotNull(output);
		assertEquals(1, output.getGeneratedIds().size());
		
		GeneralGeneratedId id = output.getGeneratedIds().getFirst();
		
		assertEquals(gen.getId(), id.getGeneratedFrom());
		assertEquals(gen.getName(), id.getLabel());
		assertNotNull(id.getValue());
		assertEquals(gen.isBarcode(), id.isBarcode());
	}
	
	@Test
	public void getNewUniqueIdTest() {
		IdentifierGenerator gen = IdentifierGenerator.builder()
									  .generates(Generates.UNIQUE)
									  .name(FAKER.name().name())
									  .idFormat("{dt}-{rand}")
									  .build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen, testUser);
		
		IdGenResult<GeneratedUniqueId> output =
			(IdGenResult<GeneratedUniqueId>) this.identifierGenerationService.getNextNIds(DEFAULT_TEST_DB_NAME, gen.getId(), 1, Generates.UNIQUE);
		
		log.info("Generated ID: {}", output);
		
		assertNotNull(output);
		assertEquals(1, output.getGeneratedIds().size());
		
		GeneratedUniqueId id = output.getGeneratedIds().getFirst();
		
		assertEquals(gen.getId(), id.getGeneratedFrom());
		assertEquals(gen.getName(), id.getLabel());
		assertNotNull(id.getValue());
		assertEquals(gen.isBarcode(), id.isBarcode());
	}
	
	@Test
	public void getNewIdBarcodeTest() {
		IdentifierGenerator gen = IdentifierGenerator.builder()
									  .generates(Generates.UNIQUE)
									  .name(FAKER.name().name())
									  .idFormat("{dt}-{rand}")
									  .barcode(true)
									  .build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen, testUser);
		
		IdGenResult<GeneratedUniqueId> output =
			(IdGenResult<GeneratedUniqueId>) this.identifierGenerationService.getNextNIds(DEFAULT_TEST_DB_NAME, gen.getId(), 1, Generates.UNIQUE);
		
		log.info("Generated ID: {}", output);
		
		assertNotNull(output);
		assertEquals(1, output.getGeneratedIds().size());
		
		GeneratedUniqueId id = output.getGeneratedIds().getFirst();
		
		assertEquals(gen.getId(), id.getGeneratedFrom());
		assertEquals(gen.getName(), id.getLabel());
		assertNotNull(id.getValue());
		assertEquals(gen.isBarcode(), id.isBarcode());
	}
	
	@Test
	public void incrementTest() {
		IdentifierGenerator gen = IdentifierGenerator.builder()
									  .generates(Generates.UNIQUE)
									  .name(FAKER.name().name())
									  .idFormat("{inc}")
									  .build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen, testUser);
		
		IdGenResult<GeneratedUniqueId> output =
			(IdGenResult<GeneratedUniqueId>) this.identifierGenerationService.getNextNIds(DEFAULT_TEST_DB_NAME, gen.getId(), 1, Generates.UNIQUE);
		
		log.info("Generated ID: {}", output);
		
		assertEquals("00001", output.getGeneratedIds().getFirst().getValue());
		
		output = (IdGenResult<GeneratedUniqueId>) this.identifierGenerationService.getNextNIds(DEFAULT_TEST_DB_NAME, gen.getId(), 1, Generates.UNIQUE);
		
		log.info("Second Generated ID: {}", output);
		
		assertEquals("00002", output.getGeneratedIds().getFirst().getValue());
	}
	
	
	public static Stream<Arguments> getThreadTestParams() {
		List<Arguments> output = new ArrayList<>();
		
		for (int curNumThreads : List.of(2, 5, 10, 20)) {
			for (int curNumIterations : List.of(10, 20)) {
				for (int curNumPerIteration : List.of(1, 2, 5, 10, 20)) {
					output.add(Arguments.of("{rand}", curNumThreads, curNumIterations, curNumPerIteration));
				}
			}
		}
		
		for (int curNumThreads : List.of(2, 10)) {
			for (int curNumIterations : List.of(10)) {
				for (int curNumPerIteration : List.of(1, 2, 5, 10, 20)) {
					output.add(Arguments.of("{inc}", curNumThreads, curNumIterations, curNumPerIteration));
				}
			}
		}
		
		return output.stream();
	}
	
	@ParameterizedTest
	@MethodSource("getThreadTestParams")
	public void generateThreadTest(String format, int numThreads, int numIterations, int numPerIteration) throws InterruptedException, ExecutionException {
		IdentifierGenerator gen = IdentifierGenerator.builder()
									  .generates(Generates.UNIQUE)
									  .name(FAKER.name().name())
									  .idFormat(format)
									  .build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen, testUser);
		
		List<Future<List<IdGenResult<GeneratedUniqueId>>>> futures = new ArrayList<>(numThreads);
		SortedSet<GeneratedUniqueId> results = new TreeSet<>();
		
		StopWatch sw;
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			TestThread.Builder threadBuilder = TestThread.builder()
												   .generatorId(gen.getId())
												   .numIterations(numIterations)
												   .numPerIteration(numPerIteration)
												   .identifierGenerationService(this.identifierGenerationService);
			
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
		
		for (Future<List<IdGenResult<GeneratedUniqueId>>> future : futures) {
			List<IdGenResult<GeneratedUniqueId>> curResultList = future.get();
			
			for (IdGenResult<GeneratedUniqueId> curResult : curResultList) {
				assertEquals(numPerIteration, curResult.getGeneratedIds().size());
				results.addAll(curResult.getGeneratedIds());
			}
		}
		
		Duration avgDurationPerId = sw.getDuration().dividedBy(results.size());
		log.info("Generated {} results in {} ms ({} per id average)", results.size(), sw.getDuration(), avgDurationPerId);
		
		assertEquals(numIterations * numThreads * numPerIteration, results.size(), "Did not have expected number of results.");
		
		if (format.equals("{inc}")) {
			
			BigInteger expected = BigInteger.ONE;
			for (GeneratedUniqueId curId : results) {
				BigInteger curResult = new BigInteger(curId.getValue());
				
				assertEquals(expected, curResult, "Was not a contiguous set of ids; expected: " + expected.toString() + " Got: " + curResult.toString());
				
				expected = expected.add(BigInteger.ONE);
			}
		}
	}
	
	@Builder
	@Slf4j
	@AllArgsConstructor
	static class TestThread implements Callable<List<IdGenResult<GeneratedUniqueId>>> {
		
		private String threadId;
		private ObjectId generatorId;
		private IdentifierGenerationService identifierGenerationService;
		private int numIterations;
		private int numPerIteration;
		
		@SneakyThrows
		@Override
		public List<IdGenResult<GeneratedUniqueId>> call() {
			log.info("Running test thread {}", this.threadId);
			
			List<IdGenResult<GeneratedUniqueId>> results = new ArrayList<>(this.numIterations);
			
			for (int i = 1; i <= this.numIterations; i++) {
				results.add(
					(IdGenResult<GeneratedUniqueId>) this.identifierGenerationService.getNextNIds(DEFAULT_TEST_DB_NAME, this.generatorId, this.numPerIteration, Generates.UNIQUE)
				);
			}
			log.info("DONE running test thread {}", this.threadId);
			return results;
		}
	}
	
	public static Stream<Arguments> getPlaceholderParams() {
		IdentifierGenerator uniqueGen = IdentifierGenerator.builder()
											.id(ObjectId.get())
											.generates(Generates.UNIQUE)
											.name(FAKER.name().name())
											.idFormat("{inc}")
											.build();
		return Stream.of(
			Arguments.of(
				uniqueGen,
				new LinkedHashSet<>() {{
					this.add(ToGenerateUniqueId.builder().generateFrom(uniqueGen.getId()).build());
					this.add(ToGenerateUniqueId.builder().generateFrom(uniqueGen.getId()).label("second").build());
				}},
				new LinkedHashSet<>() {{
					this.add(GeneratedUniqueId.builder().generatedFrom(uniqueGen.getId()).value("00001").barcode(false).label(uniqueGen.getLabel()).build());
					this.add(GeneratedUniqueId.builder().generatedFrom(uniqueGen.getId()).value("00002").barcode(false).label("second").build());
				}}
			),
			Arguments.of(
				uniqueGen,
				new LinkedHashSet<>() {{
					this.add(ToGenerateUniqueId.builder().generateFrom(uniqueGen.getId()).build());
					this.add(ProvidedUniqueId.builder().value("foobar").label("foobar").build());
					this.add(ToGenerateUniqueId.builder().generateFrom(uniqueGen.getId()).label("second").build());
				}},
				new LinkedHashSet<>() {{
					this.add(GeneratedUniqueId.builder().generatedFrom(uniqueGen.getId()).value("00001").barcode(false).label(uniqueGen.getLabel()).build());
					this.add(ProvidedUniqueId.builder().value("foobar").label("foobar").build());
					this.add(GeneratedUniqueId.builder().generatedFrom(uniqueGen.getId()).value("00002").barcode(false).label("second").build());
				}}
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getPlaceholderParams")
	public void testPlaceholderReplacement(IdentifierGenerator gen, LinkedHashSet<UniqueId> placeholders, LinkedHashSet<UniqueId> expectedIds) {
		User testUser = TestUserService.getInstance().getTestUser();
		this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen, testUser);
		
		LinkedHashSet<UniqueId> output = this.identifierGenerationService.replaceIdPlaceholders(DEFAULT_TEST_DB_NAME, placeholders);
		
		assertEquals(expectedIds, output);
	}
	
	
	@Test
	public void no2sameNamesTestNew() {
		IdentifierGenerator gen1 = IdentifierGenerator.builder()
									   .generates(Generates.UNIQUE)
									   .name(FAKER.name().name())
									   .idFormat("{inc}")
									   .build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen1, testUser);
		
		IdentifierGenerator gen2 = IdentifierGenerator.builder()
									   .generates(Generates.UNIQUE)
									   .name(gen1.getLabel())
									   .idFormat("{inc}")
									   .build();
		
		assertThrows(DbModValidationException.class, ()->this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen2, testUser));
		
	}
	
	@Test
	public void no2sameNamesTestUpdates() {
		IdentifierGenerator gen1 = IdentifierGenerator.builder()
									   .generates(Generates.UNIQUE)
									   .name(FAKER.name().name())
									   .idFormat("{inc}")
									   .build();
		User testUser = TestUserService.getInstance().getTestUser();
		this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen1, testUser);
		
		IdentifierGenerator gen2 = IdentifierGenerator.builder()
									   .generates(Generates.UNIQUE)
									   .name(FAKER.name().name())
									   .idFormat("{inc}")
									   .build();
		this.identifierGenerationService.add(DEFAULT_TEST_DB_NAME, gen2, testUser);
		
		assertThrows(DbModValidationException.class, ()->this.identifierGenerationService.update(DEFAULT_TEST_DB_NAME, gen2.setName(gen1.getName()), testUser));
	}
	
}