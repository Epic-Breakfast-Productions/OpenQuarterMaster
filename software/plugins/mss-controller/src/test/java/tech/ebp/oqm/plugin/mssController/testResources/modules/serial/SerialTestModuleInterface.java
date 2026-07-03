package tech.ebp.oqm.plugin.mssController.testResources.modules.serial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModule;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleInterface;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SerialTestModuleInterface extends TestModuleInterface {

	private static final String[] NEW_SERIAL_COMMAND = {
		"socat", "-d", "-d", "pty,raw,echo=0", "pty,raw,echo=0"
	};
	private static final Pattern DEVICE_FIND_PATTERN = Pattern.compile("\\/\\w+\\/\\w+\\/\\w+$");

	private static String parseOutPort(String logLine) {
		Matcher matcher = DEVICE_FIND_PATTERN.matcher(logLine);
		if (!matcher.find()) {
			throw new IllegalStateException("Unable to find device from log line: \"" + logLine + "\"");
		}
		return matcher.group();
	}

	private Process process;
	private InputStream inputStream;
	private OutputStream outputStream;
	private InputStream errStream;

	private String mssModulePortLocation;
	@Getter
	private String mssConnectionPortLocation;

	private SerialPort mssModuleSerialPort;

	public SerialTestModuleInterface(ObjectMapper objectMapper) throws IOException {
		super(objectMapper);
	}

	@Override
	public void init(TestModule module) throws IOException {
		this.setModule(module);

		ProcessBuilder pb = new ProcessBuilder(NEW_SERIAL_COMMAND);
		log.info("Starting new Socat process.");
		//		pb.inheritIO(); //debugging
		this.process = pb.start();
		log.info("Started new socat process. pid: {}, normTerm: {}", this.process.pid(), this.process.supportsNormalTermination());

		this.inputStream = this.process.getInputStream();
		this.errStream = this.process.getErrorStream();
		this.outputStream = this.process.getOutputStream();

		/*
		Output should look like this:
			2022/04/07 11:46:05 socat[23094] N PTY is /dev/pts/6
			2022/04/07 11:46:05 socat[23094] N PTY is /dev/pts/7
			2022/04/07 11:46:05 socat[23094] N starting data transfer loop with FDs [5,5] and [7,7]
		 */
		try (
			Scanner scanner = new Scanner(new UnClosableDecorator(this.errStream))
		) {
			this.mssModulePortLocation = parseOutPort(scanner.nextLine());
			this.mssConnectionPortLocation = parseOutPort(scanner.nextLine());
		}

		this.mssModuleSerialPort = SerialPort.getCommPort(this.mssModulePortLocation);

		//		mssModuleSerialPort.addDataListener(
		//			new SerialPortDataListener() {
		//				@Override
		//				public int getListeningEvents() {
		//					return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
		//				}
		//
		//				@Override
		//				public void serialEvent(SerialPortEvent serialPortEvent) {
		//					log.info("Serial event: {}", serialPortEvent.getEventType());
		//					if (serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
		//						log.info("Data available on port!");
		//					}
		//				}
		//			});

		this.mssModuleSerialPort.openPort();

		log.info("Got ports: {} (for hw impl) and {} (to connect to)", this.mssModulePortLocation, this.mssConnectionPortLocation);
	}

	private String readLine() {
		if(!this.mssModuleSerialPort.isOpen() || this.mssModuleSerialPort.bytesAvailable() == -1){
			log.warn("MSS serial port is not open.");
			return "";
		}
		if(this.mssModuleSerialPort.bytesAvailable() == 0){
			log.debug("No data available on test mss serial port.");
			return "";
		}


		//TODO:: smarter way to accomplish? Scanner?
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[1];
		log.info("Trying to read a line from serial port...");

		boolean run = true;
		do {
			int read = this.mssModuleSerialPort.readBytes(buffer, 1);
			//			log.debug("Read {} bytes ({})", read, (char)buffer[0]);
			if (read > 0) {
				if (buffer[0] == '\n') {
					log.info("Got Newline.");
					break;
				}
				//ignore carriage returns
				if (buffer[0] == '\r') {
					continue;
				}
				sb.append((char) buffer[0]);
			} else {
				log.debug("No bytes read.");
				run = false;
			}
		} while (run);

		String output = sb.toString();
		log.info("Got line: \"{}\"", output);
		return output;
	}

	@SneakyThrows
	public void close() {
		log.info("Closing out test serial port.");
		this.process.destroy();
		try {
			this.process.waitFor();
		} catch(InterruptedException e) {
			log.error("Failed to wait for process to finish.", e);
			this.process.destroyForcibly();
		}
		log.info("Exited socat with code {}", this.process.exitValue());
	}

	@Override
	public void send(String message) {
		log.debug("Writing command to serial port: {}", message);
		byte[] buff = message.getBytes();
		this.mssModuleSerialPort.writeBytes(buff, buff.length);
	}

	@Override
	public Optional<String> receive() {
		log.info("Processing port data for test hardware {}", this.mssModuleSerialPort.getSystemPortPath());

		String curLine = this.readLine();

		if (curLine.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(curLine);
	}

	private static class UnClosableDecorator extends InputStream {

		private final InputStream inputStream;

		public UnClosableDecorator(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public int read() throws IOException {
			return inputStream.read();
		}

		@Override
		public int read(byte[] b) throws IOException {
			return inputStream.read(b);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return inputStream.read(b, off, len);
		}

		@Override
		public long skip(long n) throws IOException {
			return inputStream.skip(n);
		}

		@Override
		public int available() throws IOException {
			return inputStream.available();
		}

		@Override
		public synchronized void mark(int readlimit) {
			inputStream.mark(readlimit);
		}

		@Override
		public synchronized void reset() throws IOException {
			inputStream.reset();
		}

		@Override
		public boolean markSupported() {
			return inputStream.markSupported();
		}

		@Override
		public void close() throws IOException {
			//do nothing
		}
	}
}
