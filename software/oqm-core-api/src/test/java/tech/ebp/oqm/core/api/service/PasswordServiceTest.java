package tech.ebp.oqm.core.api.service;

import com.google.common.base.Stopwatch;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.ebp.oqm.core.api.testResources.testClasses.WebServerTest;

import jakarta.inject.Inject;
import tech.ebp.oqm.core.api.service.PasswordService;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
class PasswordServiceTest extends WebServerTest {
	
	private static final int GOAL_MS_HASH_TIME = 250;
	private static final int THRESHOLD_MS_HASH_TIME = 1000;
	private static final int MIN_ITERATIONS = 4;
	private static final int[] iterationsToTest = IntStream.range(MIN_ITERATIONS, 34).toArray();
	private static final String[] passwordsForTiming = {
		"hello",
		"helloWorld",
		"1234!@#$#234rfekvfn 42iuhfeovgrefrf 23ur432^%$73457$#^4545g4243vgert4%ybvH5ubTRH;rg[tgrt]ewbgr; wnger h4w ggeoirjgrejgelg",
		"1@@#2345#3grefdFDdFgretREtet5et554",
		"dsad3@#$324",
		"1234qwreASD",
		"47%^*%856*&%&565fghR%Y45gr$%tg3"
	};
	
	private static Stream<Arguments> passwdsAsArgs() {
		return Arrays.stream(passwordsForTiming).map(Arguments::of);
	}
	
	
	@Inject
	PasswordService passwordService;
	
	/**
	 * Test to help determine how many iterations to use.
	 * <p>
	 * https://security.stackexchange.com/questions/17207/recommended-of-rounds-for-bcrypt
	 * <p>
	 * Uncomment the next line to test
	 */
	//    @Test
	public void passwordHashTime() {
		int curClosestIteration = Integer.MAX_VALUE;
		double curClosestIterationDistance = Double.MAX_VALUE;
		for (int curIterations : iterationsToTest) {
			long[] hashTimes = new long[passwordsForTiming.length];
			long[] checkTimes = new long[passwordsForTiming.length];
			
			for (int j = 0; j < hashTimes.length; j++) {
				String curPass = passwordsForTiming[j];
				Stopwatch hashSw = Stopwatch.createStarted();
				String hashed = passwordService.createPasswordHash(curPass, curIterations);
				hashSw.stop();
				hashTimes[j] = hashSw.elapsed().toMillis();
				
				Stopwatch checkSw = Stopwatch.createStarted();
				passwordService.passwordMatchesHash(hashed, curPass);
				checkSw.stop();
				checkTimes[j] = checkSw.elapsed().toMillis();
			}
			double hashAverage = Arrays.stream(hashTimes).average().getAsDouble();
			double checkAverage = Arrays.stream(checkTimes).average().getAsDouble();
			log.info("Average hash time for {} iterations: {}ms", curIterations, hashAverage);
			log.info("Average check time for {} iterations: {}ms", curIterations, checkAverage);
			double distanceToGoal = (hashAverage < GOAL_MS_HASH_TIME ? GOAL_MS_HASH_TIME - hashAverage : hashAverage - GOAL_MS_HASH_TIME);
			if (
				curClosestIterationDistance > distanceToGoal
			) {
				curClosestIteration = curIterations;
				curClosestIterationDistance = distanceToGoal;
			}
			
			if (hashAverage > THRESHOLD_MS_HASH_TIME) {
				log.warn("Average time of last # iterations exceeded threshold time ({}ms). Ending test.", THRESHOLD_MS_HASH_TIME);
				break;
			}
		}
		log.info("Recommend using {} iterations as default for goal hash time of {}ms.", curClosestIteration, GOAL_MS_HASH_TIME);
	}
	
	@ParameterizedTest(name = "hashTest[{index}]")
	@MethodSource("passwdsAsArgs")
	public void hashTest(String testPw) {
		Stopwatch sw = Stopwatch.createStarted();
		String hash = this.passwordService.createPasswordHash(testPw);
		sw.stop();
		
		log.info("Hash of \"{}\" in {}ms: {}", testPw, sw.elapsed().toMillis(), hash);
		
		assertTrue(this.passwordService.passwordMatchesHash(hash, testPw));
		assertFalse(this.passwordService.passwordMatchesHash(hash, "some bad password"));
	}
	
}