package tech.ebp.oqm.plugin.mssController.testResources.modules.modInterfaces.serial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleInterface;
import tech.ebp.oqm.plugin.mssController.testResources.modules.engine.TestModuleEngine;
import tech.ebp.oqm.plugin.mssController.testResources.serial.SocatProcess;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class SerialTestModuleInterface extends TestModuleInterface {

	private SocatProcess process;
	private SerialPort mssModuleSerialPort;

	protected String getMssModulePortLocation() {
		return this.process.getPortALocation();
	}
	public String getMssConnectionPortLocation() {
		return this.process.getPortBLocation();
	}

	public SerialTestModuleInterface(ObjectMapper objectMapper, TestModuleEngine engine) throws IOException {
		super(objectMapper, engine);
	}

	@Override
	public void init() throws IOException {
		this.process = new SocatProcess();
		this.process.init();

		this.mssModuleSerialPort = SerialPort.getCommPort(this.getMssModulePortLocation());

		this.mssModuleSerialPort.addDataListener(
			new SerialPortDataListener() {
				@Override
				public int getListeningEvents() {
					return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_DATA_RECEIVED | SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
				}

				@Override
				public void serialEvent(SerialPortEvent serialPortEvent) {
					log.info("Serial event: {}", serialPortEvent.getEventType());
					if (serialPortEvent.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
						handleDataReceived(serialPortEvent);
					}
				}
			});

		this.mssModuleSerialPort.openPort();

		log.info("Got ports: {} (for hw impl) and {} (to connect to)", this.getMssModulePortLocation(), this.getMssConnectionPortLocation());
	}

	private void handleDataReceived(SerialPortEvent serialPortEvent) {
		log.info("Data available on port!");

		String data = new String(serialPortEvent.getReceivedData());

		String response = this.getEngine().handleData(data);

		if (response != null && !response.isEmpty()) {
			this.mssModuleSerialPort.writeBytes(response.getBytes(), response.length());
		}
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
		this.process.close();
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

}
