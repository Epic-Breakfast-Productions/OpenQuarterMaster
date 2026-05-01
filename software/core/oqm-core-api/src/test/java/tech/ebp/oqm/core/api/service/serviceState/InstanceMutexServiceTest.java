package tech.ebp.oqm.core.api.service.serviceState;


import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.exception.MutexWaitTimeoutException;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.io.IOException;
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
			Arguments.of(false, 2, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(true, 2, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(false, 3, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(true, 3, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(false, 5, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(true, 5, 10, Duration.of(250, ChronoUnit.MILLIS)),
			
			Arguments.of(false, 2, 20, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(true, 2, 20, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(false, 3, 20, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(true, 3, 20, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(false, 5, 20, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(true, 5, 20, Duration.of(250, ChronoUnit.MILLIS)),
			
			Arguments.of(false, 10, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(true, 10, 10, Duration.of(250, ChronoUnit.MILLIS)),
			Arguments.of(false, 20, 20, Duration.of(150, ChronoUnit.MILLIS)),
			Arguments.of(true, 20, 20, Duration.of(150, ChronoUnit.MILLIS))
		);
	}
	
	@Inject
	InstanceMutexService instanceMutexService;
	
	
	@ParameterizedTest
	@MethodSource("getParams")
	public void threadTest(boolean await, int numThreads, int numIterations, Duration workDuration) throws InterruptedException, ExecutionException {
		String mutexId = "testMutex2";
		List<Future<List<ThreadResult>>> futures = new ArrayList<>(numThreads);
		SortedSet<ThreadResult> results = new TreeSet<>();
		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		Duration expectedWaitTime = Duration.of(numThreads * numIterations * workDuration.toMillis(), ChronoUnit.MILLIS);
		
		TestThread.TestThreadBuilder threadBuilder = TestThread.builder()
														 .await(await)
														 .mutexId(mutexId)
														 .numIterations(numIterations)
														 .durationOfWork(workDuration)
														 .instanceMutexService(instanceMutexService);
		
		StopWatch overall = StopWatch.createStarted();
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
		overall.stop();
		
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
		
		Duration expectedVsActualTimeDiff = overall.getDuration().minus(expectedWaitTime);
		log.info("Perfect efficiency vs actual time: {} vs {} (difference: {})", expectedWaitTime, overall.getDuration(), expectedVsActualTimeDiff);
		
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
		
		private boolean await;
		private String mutexId;
		private String threadId;
		private InstanceMutexService instanceMutexService;
		private int numIterations;
		private Duration durationOfWork;
		
		@SneakyThrows
		@Override
		public List<ThreadResult> call() {
			log.info("Running test thread {}", this.threadId);
			
			Optional<String> threadIdOpt = Optional.ofNullable(this.threadId);
			
			this.instanceMutexService.register(this.mutexId);
			
			List<ThreadResult> results = new ArrayList<>(this.numIterations);
			for (int i = 1; i <= this.numIterations; i++) {
				log.info("Thread {} waiting for lock on iteration {}", this.threadId, i);
				
				if(await){
					instanceMutexService.awaitLock(this.mutexId, threadIdOpt);
				} else {
					//noinspection StatementWithEmptyBody
					while (!instanceMutexService.lock(this.mutexId, threadIdOpt)) {
						//nothing to do
					}
				}
				log.info("Thread {} got lock on iteration {}/{}", this.threadId, i, this.numIterations);
				ThreadResult.ThreadResultBuilder resultBuilder = ThreadResult.builder()
																	 .threadId(this.threadId)
																	 .start(LocalDateTime.now());
				
				Thread.sleep(this.durationOfWork);
				
				resultBuilder.end(LocalDateTime.now());
				
				assertTrue(this.instanceMutexService.free(this.mutexId, Optional.of(this.threadId)));
				
				log.info("Thread {} done doing work & released lock on iteration {}", this.threadId, i);
				results.add(resultBuilder.build());
			}
			log.info("DONE running test thread {}", this.threadId);
			return results;
		}
	}
	
	@Test
	public void basicTest() {
		String mutexId = "testMutex";
		this.instanceMutexService.register(mutexId);
		
		assertTrue(this.instanceMutexService.lock(mutexId));
		
		assertFalse(this.instanceMutexService.lock(mutexId));
		
		assertTrue(this.instanceMutexService.free(mutexId));
		
		assertTrue(this.instanceMutexService.lock(mutexId));
		this.instanceMutexService.free(mutexId);
	}
	
	@Test
	public void testWithResources() throws InterruptedException {
		String id = "testWithResources";
		
		log.info("Starting mutex with resources check");
		
		this.instanceMutexService.register(id);
		
		try (
			InstanceMutexService.InstanceMutexResource mutex = this.instanceMutexService.getResource(true, id, Optional.empty())
		) {
			log.info("In the critical section, 1");
		}
		
		log.info("In between mutex try-with-resources");
		
		try (
			InstanceMutexService.InstanceMutexResource mutex = this.instanceMutexService.getResource(true, id, Optional.empty())
		) {
			log.info("In the critical section, 2");
		}
		
		log.info("Finished mutex 2");
	}
	
	@Test
	public void testUseBeforeRegister() {
		assertEquals(
			"Mutex was not registered before usage: testUseBeforeRegister",
			assertThrows(
				IllegalStateException.class, ()->{
					this.instanceMutexService.lock("testUseBeforeRegister");
				}
			).getMessage()
		);
	}
	
	@Test
	public void testSameEntityDoubleLock() {
		String id = "testSameEntityDoubleLock";
		
		this.instanceMutexService.register(id);
		
		assertTrue(this.instanceMutexService.lock(id));
		assertFalse(this.instanceMutexService.lock(id));
		assertFalse(this.instanceMutexService.lock(id, Optional.of("foo")));
	}
	
	@Test
	public void testAwaitTimeout() {
		String id = "testAwaitTimeout";
		
		this.instanceMutexService.register(id);
		
		assertTrue(this.instanceMutexService.lock(id));
		
		assertThrows(
			MutexWaitTimeoutException.class, () ->{
				this.instanceMutexService.awaitLock(id, Optional.of("foo"));
			}
		);
	}
	
}
