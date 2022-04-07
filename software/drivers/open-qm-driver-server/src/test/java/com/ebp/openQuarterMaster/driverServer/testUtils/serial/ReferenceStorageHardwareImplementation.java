package com.ebp.openQuarterMaster.driverServer.testUtils.serial;

import com.ebp.openQuarterMaster.driverServer.serial.SerialPortWrapper;
import com.ebp.openQuarterMaster.lib.driver.BlockLightSetting;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Getter
public class ReferenceStorageHardwareImplementation implements Closeable {
	private static final int DEFAULT_NUM_BLOCKS = 5;
	
	private final String serialNo = UUID.randomUUID().toString();
	private SerialPortWrapper portWrapper;
	private final int numBlocks;
	//state
	private String currentMessage = "";
	private final BlockLightSetting[] lightSettings;
	
	
	public ReferenceStorageHardwareImplementation(SerialPortWrapper portWrapper, final int numBlocks){
		this.portWrapper = portWrapper;
		this.numBlocks = numBlocks;
		this.lightSettings = new BlockLightSetting[this.numBlocks];
	}
	
	public ReferenceStorageHardwareImplementation(SerialPortWrapper portWrapper){
		this(portWrapper, DEFAULT_NUM_BLOCKS);
	}
	
	private void processLine(String line){
		log.info("Processing line: {}", line);
		
		if(line.isBlank()){
			return;
		}
		if(line.equals("$P")){
			this.portWrapper.writeLine("Got ping request.");
			this.portWrapper.writeLine("$P");
		}
	}
	
	public int processPortData() throws InterruptedException {
		log.info("Processing port data for test hardware {}", this.portWrapper.getPort().getSystemPortPath());
		this.portWrapper.acquireLock();
		try {
			int processedLines = 0;
			
			String curLine = this.portWrapper.readLine();
			while (!curLine.isBlank()) {
				this.processLine(curLine);
				processedLines++;
				if(this.portWrapper.getPort().bytesAvailable() > 0) {
					curLine = this.portWrapper.readLine();
				} else {
					curLine = "";
				}
			}
			
			return processedLines;
		} finally {
			this.portWrapper.releaseLock();
		}
	}
	
	@Override
	public void close() throws IOException {
		this.portWrapper.close();
	}
	
	
}
