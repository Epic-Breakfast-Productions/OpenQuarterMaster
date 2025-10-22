package tech.ebp.oqm.core.api.service.mongo;


import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
public class InstanceMutexServiceTest extends RunningServerTest {

	public static Stream<Arguments> getParams() {
		return Stream.of(
			Arguments.of(2, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(3, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(5, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(10, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(20, 20, Duration.of(150, ChronoUnit.MILLIS))
		);
	}

	@Inject
	InstanceMutexService instanceMutexService;

	@Test
	public void basicTest() {
		String mutexId = "testMutex";
		this.instanceMutexService.register(mutexId);

		assertTrue(this.instanceMutexService.lock(mutexId));

		assertFalse(this.instanceMutexService.lock(mutexId));

		this.instanceMutexService.free(mutexId);

		assertTrue(this.instanceMutexService.lock(mutexId));
		this.instanceMutexService.free(mutexId);
	}


	@ParameterizedTest
	@MethodSource("getParams")
	public void threadTest(int numThreads, int numIterations, Duration workDuration) throws InterruptedException, ExecutionException {
		String mutexId = "testMutex2";
		List<Future<List<ThreadResult>>> futures = new ArrayList<>(numThreads);
		SortedSet<ThreadResult> results = new TreeSet<>();
		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

		TestThread.Builder threadBuilder = TestThread.builder()
			.mutexId(mutexId)
			.numIterations(numIterations)
			.durationOfWork(workDuration)
			.instanceMutexService(instanceMutexService);

		for (int i = 1; i <= numThreads; i++) {
			threadBuilder.threadId("testThread-" + i);

			futures.add(executor.submit(threadBuilder.build()));
		}
		executor.shutdown();
		while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
			log.info("Still waiting on threads...");
		}

		for (Future<List<ThreadResult>> future : futures) {
			results.addAll(future.get());
		}

		assertEquals(numIterations * numThreads, results.size());

		//TODO:: check results
		log.info("Results: {}", results);

		Iterator<ThreadResult> iterator = results.iterator();
		ThreadResult cur = iterator.next();
		while (iterator.hasNext()) {
			ThreadResult next = iterator.next();

			assertTrue(
				next.getStart().isAfter(cur.getStart()),
				"result " + cur + " start overlaps with the next result " + next + " (next start is before cur start)"
			);
			assertTrue(
				(next.getStart().isAfter(cur.getEnd()) || next.getStart().equals(cur.getEnd())),
				"result " + cur + " overlaps with the next result " + next + " (next start is before cur end)"
			);

			cur = next;
		}

	}

	@Builder
	@Data
	@AllArgsConstructor
	static
	class ThreadResult implements Comparable<ThreadResult> {
		private String threadId;
		private LocalDateTime start;
		private LocalDateTime end;

		@Override
		public int compareTo(@NonNull InstanceMutexServiceTest.ThreadResult threadResult) {
			return this.getStart().compareTo(threadResult.getStart());
		}
	}

	@Builder
	@Slf4j
	@AllArgsConstructor
	static class TestThread implements Callable<List<ThreadResult>> {

		private String mutexId;
		private String threadId;
		private InstanceMutexService instanceMutexService;
		private int numIterations;
		private Duration durationOfWork;

		@SneakyThrows
		@Override
		public List<ThreadResult> call() {
			log.info("Running test thread {}", this.threadId);

			this.instanceMutexService.register(this.mutexId);

//			Thread.sleep(500);

			List<ThreadResult> results = new ArrayList<>(this.numIterations);
			for (int i = 1; i <= this.numIterations; i++) {
				log.info("Thread {} waiting for lock on iteration {}", this.threadId, i);
				while (!instanceMutexService.lock(this.mutexId, Optional.of(this.threadId))) {
					Thread.sleep(50);
				}
				log.info("Thread {} got lock on iteration {}/{}", this.threadId, i, this.numIterations);
				ThreadResult.Builder resultBuilder = ThreadResult.builder()
					.threadId(this.threadId)
					.start(LocalDateTime.now());

				Thread.sleep(this.durationOfWork);

				resultBuilder.end(LocalDateTime.now());

				this.instanceMutexService.free(this.mutexId, Optional.of(this.threadId));
				log.info("Thread {} done doing work & released lock on iteration {}", this.threadId, i);
				results.add(resultBuilder.build());
			}
			log.info("DONE running test thread {}", this.threadId);
			return results;
		}
	}

}
