package tech.ebp.oqm.plugin.mssController.testResources.serial;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SocatProcess implements AutoCloseable {
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

	@Getter
	private String portALocation;
	@Getter
	private String portBLocation;

	public void init() throws IOException {
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
			this.portALocation = parseOutPort(scanner.nextLine());
			this.portBLocation = parseOutPort(scanner.nextLine());
		}
	}

	@Override
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
