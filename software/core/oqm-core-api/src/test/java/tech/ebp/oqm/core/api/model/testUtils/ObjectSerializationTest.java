package tech.ebp.oqm.core.api.model.testUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
public abstract class ObjectSerializationTest<T> extends BasicTest {
	
	/** The max amount of time in seconds we want de/serialization to take (triggers warning if over this) */
	private static final int SERIALIZATION_TIME_THRESHOLD = 3;
	/** The threshold for logging out the json string representation of the data */
	private static final int SERIALIZED_SIZE_LOG_THRESHOLD = 1_500;
	
	
	/** The speed of the theoretical connection. Used to calculate {@link #SERIALIZED_SIZE_THRESHOLD}. In bytes per second. */
	private static final int TRANSFER_SPEED = 100_000_000;
	/** The max time in seconds we would want a transfer to take. Used to calculate {@link #SERIALIZED_SIZE_THRESHOLD}. */
	private static final double MAX_TRANSFER_TIME = 1.25;
	/**
	 * The max size of a json document we want to make (triggers warning if over this). Calculated based on how long it would take to
	 * transfer
	 */
	private static final double SERIALIZED_SIZE_THRESHOLD = TRANSFER_SPEED * MAX_TRANSFER_TIME;
	public static final String SERIALIZATION_TIMINGS_DIR = "build/reports/serializationTimings/";
	
	/**
	 * The class of the object we are de/serializing.
	 */
	private final Class<T> clazz;
	private TestInfo testInfo;
	
	protected ObjectSerializationTest(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@BeforeEach
	public void stateThresholds(TestInfo testInfo) {
		log.info("Serialization time threshold: {}s", SERIALIZATION_TIME_THRESHOLD);
		log.info("Serialized size threshold: {}", FileUtils.byteCountToDisplaySize((int) SERIALIZED_SIZE_THRESHOLD));
		new File(SERIALIZATION_TIMINGS_DIR).mkdirs();
		this.testInfo = testInfo;
	}
	
	@ParameterizedTest
	@MethodSource("getObjects")
	public void testSerialization(T object) throws IOException {
		StopWatch swWrite = StopWatch.createStarted();
		String json = OBJECT_MAPPER.writeValueAsString(object);
		swWrite.stop();
		log.info("Serialized object in {}", swWrite);
		
		boolean failedSerializeTime = false,
			failedSerializeSize = false,
			failedDeserializeTime = false;
		
		if (swWrite.getTime(TimeUnit.SECONDS) >= SERIALIZATION_TIME_THRESHOLD) {
			log.warn("Serialization took longer than threshold {} seconds to complete.", SERIALIZATION_TIME_THRESHOLD);
			failedSerializeTime = true;
		}
		
		log.info("Length of json string: {} bytes ({})", json.length(), FileUtils.byteCountToDisplaySize(json.length()));
		log.info("Would take {}s to send over {}bps connection.", (double) json.length() / (double) TRANSFER_SPEED, TRANSFER_SPEED);
		
		if (json.length() > SERIALIZED_SIZE_THRESHOLD) {
			log.warn("Length of JSON string very, very long.");
			failedSerializeSize = true;
		}
		
		if (json.length() < SERIALIZED_SIZE_LOG_THRESHOLD) {
			log.info("json: {}", json);
		}
		
		StopWatch swRead = StopWatch.createStarted();
		T objectBack = OBJECT_MAPPER.readValue(json, clazz);
		swRead.stop();
		log.info("Deserialized object in {}", swRead);
		
		if (swRead.getTime(TimeUnit.SECONDS) >= SERIALIZATION_TIME_THRESHOLD) {
			log.warn("Deserialization took longer than the threshold {} seconds to complete.", SERIALIZATION_TIME_THRESHOLD);
			failedDeserializeTime = true;
		} else {
			log.info("Deserialization did not take too long.");
		}
		
		try {
//			log.debug("{} vs {}", object, objectBack);
			assertEquals(object, objectBack, "Deserialized object was not equal to original.");
		} catch(AssertionError e) {
			throw e;
		} catch(Throwable e) {
			throw new IllegalStateException("Failed to determine if original and deserialized were equal.", e);
		}
		
		log.info("Original and deserialized objects were equal.");
		
		try (
			FileOutputStream os = new FileOutputStream(
				SERIALIZATION_TIMINGS_DIR + this.clazz.getSimpleName() + ".txt",
				true
			);
			PrintWriter writer = new PrintWriter(os);
		) {
			writer.println(this.testInfo.getDisplayName());
			//			writer.println(object);
			writer.println("json length: " + json.length());
			writer.println("time to Serialize: " + swWrite);
			writer.println("time to Deserialize: " + swRead);
			if (failedSerializeSize) {
				writer.println("FAILED size");
			}
			if (failedSerializeTime) {
				writer.println("FAILED serialize time");
			}
			if (failedDeserializeTime) {
				writer.println("FAILED deserialize time");
			}
			writer.println();
			writer.println();
		}
		
		assertFalse(
			failedSerializeSize || failedSerializeTime || failedDeserializeTime,
			"Failed one or more of size/time related checks; failedSerialSize=" +
			failedSerializeSize +
			", failedSerializeTime=" +
			failedSerializeTime +
			", failedDeserializeTime=" +
			failedDeserializeTime
		);
	}
	
}
