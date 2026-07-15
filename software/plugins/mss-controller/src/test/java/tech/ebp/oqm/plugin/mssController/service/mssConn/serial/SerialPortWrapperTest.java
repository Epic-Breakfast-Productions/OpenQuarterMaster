package tech.ebp.oqm.plugin.mssController.service.mssConn.serial;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialModuleLockRequiredException;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialPortSetupFailedException;
import tech.ebp.oqm.plugin.mssController.testResources.serial.SocatProcess;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.plugin.mssController.model.utils.JacksonUtils.OBJECT_MAPPER;

@Slf4j
class SerialPortWrapperTest {

	private SocatProcess process;

	private void setupSocatProcess() throws IOException {
		this.process = new SocatProcess();
		this.process.init();
	}

	@AfterEach
	public void tearDown() {
		if (this.process != null) {
			this.process.close();
			this.process = null;
		}
	}

	@Test
	public void testCreate() throws IOException, SerialPortSetupFailedException {
		this.setupSocatProcess();

		try (
			SerialPortWrapper serialPortWrapper = new SerialPortWrapper(
				OBJECT_MAPPER,
				this.process.getPortALocation(),
				Optional.empty(),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1)
			)
		) {
			log.info("Serial port wrapper created");
		}
	}

	@Test
	public void testCreateFailOpen() throws IOException, SerialPortSetupFailedException {
		SerialPortSetupFailedException e = assertThrows(
			SerialPortSetupFailedException.class,
			()->{
				new SerialPortWrapper(
					OBJECT_MAPPER,
					"/foo/bar",
					Optional.empty(),
					Duration.ofSeconds(1),
					Duration.ofSeconds(1),
					Duration.ofSeconds(1),
					Duration.ofSeconds(1)
				);
			}
		);

		log.info("Error: {}", e.getMessage());
	}

	@Test
	public void testAssertLockNotLocked() throws IOException, SerialPortSetupFailedException {
		this.setupSocatProcess();

		try (
			SerialPortWrapper serialPortWrapper = new SerialPortWrapper(
				OBJECT_MAPPER,
				this.process.getPortALocation(),
				Optional.empty(),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1)
			)
		) {
			assertThrows(
				SerialModuleLockRequiredException.class,
				serialPortWrapper::assertLockAcquired
			);
		}
	}

	@Test
	public void testAssertLockLocked() throws IOException, SerialPortSetupFailedException {
		this.setupSocatProcess();

		try (
			SerialPortWrapper serialPortWrapper = new SerialPortWrapper(
				OBJECT_MAPPER,
				this.process.getPortALocation(),
				Optional.empty(),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1)
			)
		) {
			serialPortWrapper.acquireLock();
			serialPortWrapper.assertLockAcquired();
		}
	}

	@Test
	public void testWrapperCloses() throws IOException, SerialPortSetupFailedException, InterruptedException {
		this.setupSocatProcess();

		try (
			SerialPortWrapper serialPortWrapper = new SerialPortWrapper(
				OBJECT_MAPPER,
				this.process.getPortALocation(),
				Optional.empty(),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1)
			)
		) {
			this.process.close();

			Thread.sleep(250);

			assertFalse(serialPortWrapper.isOpen());
		}
	}

	@Test
	public void testCommSpacing() throws IOException, SerialPortSetupFailedException, InterruptedException {
		this.setupSocatProcess();

		try (
			SerialPortWrapper serialPortWrapper = new SerialPortWrapper(
				OBJECT_MAPPER,
				this.process.getPortALocation(),
				Optional.empty(),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1),
				Duration.ofSeconds(1)
			)
		) {
			assertTrue(serialPortWrapper.pastCommSpacing());

			serialPortWrapper.updateNoCommBefore();

			assertFalse(serialPortWrapper.pastCommSpacing());

			serialPortWrapper.waitForCommSpacing();

			assertTrue(serialPortWrapper.pastCommSpacing());
		}
	}

	//TODO:: more
}
