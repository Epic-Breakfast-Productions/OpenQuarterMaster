package tech.ebp.oqm.plugin.mssController.service.mssConn.connectors.serial;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Durations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialModuleLockRequiredException;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialPortClosedException;
import tech.ebp.oqm.plugin.mssController.model.exception.SerialPortSetupFailedException;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponseType;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.message.Message;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.message.report.InventoryEventReport;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.message.report.UniqueItemReport;
import tech.ebp.oqm.plugin.mssController.testResources.serial.SocatProcess;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.awaitility.Awaitility.await;
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
					Path.of("/foo/bar"),
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
	public void testCommSpacing() throws IOException, SerialPortSetupFailedException {
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

	@Test
	public void testStartCommWhenClosed() throws IOException, SerialPortSetupFailedException {
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
			serialPortWrapper.close();

			assertThrows(
				SerialPortClosedException.class,
				serialPortWrapper::startComm
			);
		}
	}

	@Test
	public void testClosesOnDeadSocat() throws IOException, SerialPortSetupFailedException {
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

			assertFalse(serialPortWrapper.isOpen());
		}
	}

	private void writeToTestPort(String data) throws IOException {
		try (
			OutputStream os = Files.newOutputStream(this.process.getPortBLocation())
		) {
			os.write(data.getBytes());
		}
	}

	@Test
	public void testReadOneJson() throws IOException, SerialPortSetupFailedException {
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
			CommandResponse commandResponseOg = CommandResponse.builder()
													.status(CommandResponseType.OK)
													.build();


			String one = OBJECT_MAPPER.writeValueAsString(commandResponseOg);

			this.writeToTestPort(one.toString());

			try(
				SerialPortWrapper.CommAction a = serialPortWrapper.acquireLock()
			) {
				AtomicReference<Optional<CommandResponse>> responseOp = new AtomicReference<>();
				await()
					.atMost(Duration.ofSeconds(2))
					.until(()->{
						 responseOp.set(serialPortWrapper.getCommandresponse());
						 return responseOp.get().isPresent();
					});

				CommandResponse response = responseOp.get().get();

				assertEquals(commandResponseOg, response);

			}
		}
	}
	@Test
	public void testReadMultiJson() throws IOException, SerialPortSetupFailedException, InterruptedException {
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
			int num = 50;

			for(int i = 0; i < num; i++){
				this.writeToTestPort(OBJECT_MAPPER.writeValueAsString(new InventoryEventReport()));
				Thread.sleep(100);
			}

			try(
				SerialPortWrapper.CommAction a = serialPortWrapper.acquireLock()
			) {
				await()
					.atMost(Duration.ofSeconds(15))
					.until(()->serialPortWrapper.getReceivedMessages().size() == num);

				for(int i = 0; i < num; i++){
					InventoryEventReport gotten = (InventoryEventReport) serialPortWrapper.getReceivedMessages().remove();

					assertEquals(
						new InventoryEventReport(),
						gotten
					);
				}
			}
		}
	}

	//TODO:: more
}
