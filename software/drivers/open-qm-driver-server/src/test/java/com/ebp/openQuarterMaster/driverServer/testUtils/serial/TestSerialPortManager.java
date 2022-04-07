package com.ebp.openQuarterMaster.driverServer.testUtils.serial;

import com.ebp.openQuarterMaster.driverServer.serial.SerialPortWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Singleton
public class TestSerialPortManager implements Closeable {
	private static final String[] NEW_SERIAL_COMMAND = {
		"socat", "-d", "-d", "pty,raw,echo=0", "pty,raw,echo=0"
	};
	private static final Pattern DEVICE_FIND_PATTERN = Pattern.compile("\\/\\w+\\/\\w+\\/\\w+$");
	
	private Collection<PortObjects> portObjects = new ArrayList<>();
	
	
	private static String parseOutPort(String logLine){
		Matcher matcher = DEVICE_FIND_PATTERN.matcher(logLine);
		if(!matcher.find()){
			throw new IllegalStateException("Unable to find device from log line: \"" + logLine + "\"");
		}
		return matcher.group();
	}
	
	public String createNewHardware() throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(NEW_SERIAL_COMMAND);
		log.info("Starting new Socat process.");
//		pb.inheritIO(); //debugging
		Process p = pb.start();
		log.info("Started new socat process. pid: {}, normTerm: {}", p.pid(), p.supportsNormalTermination());
		
		InputStream inputStream = p.getInputStream();
		InputStream errStream = p.getErrorStream();
		OutputStream outStream = p.getOutputStream();
		
		String portOne;
		String portTwo;
		/*
		Output should look like this:
			2022/04/07 11:46:05 socat[23094] N PTY is /dev/pts/6
			2022/04/07 11:46:05 socat[23094] N PTY is /dev/pts/7
			2022/04/07 11:46:05 socat[23094] N starting data transfer loop with FDs [5,5] and [7,7]
		 */
		try(
			Scanner scanner = new Scanner(errStream);
		){
			portOne = parseOutPort(scanner.nextLine());
			portTwo = parseOutPort(scanner.nextLine());
		}
		log.info("Got ports: {} (for hw impl) and {} (to connect to)", portOne, portTwo);
		
		ReferenceStorageHardwareImplementation rhwi = new ReferenceStorageHardwareImplementation(
			new SerialPortWrapper(portOne)
		);
		
		this.portObjects.add(
			new PortObjects(
				portTwo,
				p,
				rhwi,
				inputStream,
				outStream,
				errStream
			)
		);
		
		return portTwo;
	}
	
	public List<String> createNewHardware(int numPorts) throws IOException, InterruptedException, ParseException {
		log.info("Creating {} new serial ports.", numPorts);
		List<String> output = new ArrayList<>();
		
		for(int i = 0; i < numPorts; i++){
			output.add(this.createNewHardware());
		}
		
		return output;
	}
	
	public void processHw() throws InterruptedException {
		for(PortObjects cur : this.portObjects){
			log.info(
				"Processed {} lines at {}",
				cur.getHw().processPortData(),
				cur.getOutSerialPort()
			);
		}
	}
	
	@PreDestroy
	@Override
	public void close() throws IOException {
		for(PortObjects cur : this.portObjects){
			cur.close();
		}
	}
	
	@Data
	@AllArgsConstructor
	private static class PortObjects implements Closeable{
		private final String outSerialPort;
		private final Process process;
		private final ReferenceStorageHardwareImplementation hw;
		private final InputStream inputStream;
		private final OutputStream outputStream;
		private final InputStream errStream;
		
		@SneakyThrows
		@Override
		public void close() throws IOException {
			log.info("Closing out test serial port.");
			this.hw.close();
			this.process.destroy();
			this.process.waitFor();
			log.info("Exited socat with code {}", this.process.exitValue());
		}
	}
}
