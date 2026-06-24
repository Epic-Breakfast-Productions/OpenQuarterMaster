package tech.ebp.oqm.plugin.mssController.testResources.modules.serial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.Command;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleInterface;

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

	@Getter
	private final ObjectMapper objectMapper;
	private final Process process;
	private final InputStream inputStream;
	private final OutputStream outputStream;
	private final InputStream errStream;

	private final String mssModulePortLocation;
	@Getter
	private final String mssConnectionPortLocation;

	private SerialPort mssModuleSerialPort;

	public SerialTestModuleInterface(ObjectMapper objectMapper) throws IOException {
		this.objectMapper = objectMapper;
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
			Scanner scanner = new Scanner(errStream);
		) {
			this.mssModulePortLocation = parseOutPort(scanner.nextLine());
			this.mssConnectionPortLocation = parseOutPort(scanner.nextLine());
		}

		this.mssModuleSerialPort = SerialPort.getCommPort(this.mssModulePortLocation);

		log.info("Got ports: {} (for hw impl) and {} (to connect to)", this.mssModulePortLocation, this.mssConnectionPortLocation);
	}

	private String readLine() {
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
				run = false;
			}
		} while (run);

		String output = sb.toString();
		log.info("Got line: \"{}\"", output);
		return output;
	}

	public synchronized Optional<Command> getNextCommand() throws JsonProcessingException {
		log.info("Processing port data for test hardware {}", this.mssModuleSerialPort.getSystemPortPath());

		String curLine = this.readLine();

		if(curLine.isBlank()){
			return Optional.empty();
		}
		Command command = this.objectMapper.readValue(curLine, Command.class);

		return Optional.of(command);
	}

	@SneakyThrows
	public void close() {
		log.info("Closing out test serial port.");
		this.process.destroy();
		try {
			this.process.waitFor();
		} catch (InterruptedException e) {
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

		if(curLine.isBlank()){
			return Optional.empty();
		}
		return Optional.of(curLine);
	}
}
