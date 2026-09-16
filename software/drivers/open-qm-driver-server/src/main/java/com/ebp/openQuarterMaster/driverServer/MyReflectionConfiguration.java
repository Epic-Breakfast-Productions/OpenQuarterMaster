package com.ebp.openQuarterMaster.driverServer;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortDataListenerWithExceptions;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortIOException;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.fazecast.jSerialComm.SerialPortMessageListener;

import com.fazecast.jSerialComm.SerialPortPacketListener;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import io.quarkus.runtime.annotations.RegisterForReflection;


/**
 * Required to tell GraalVm to keep classes around.
 * <p>
 * If running in native mode and get errors about classes, reflection, etc, add the erring class here
 * <p>
 * https://quarkus.io/guides/writing-native-applications-tips#including-resources
 * <p>
 * TODO:: test in native mode and go through to include all needed classes
 */
@RegisterForReflection(
	targets = { // Classes we know about go here
		SerialPort.class,
		SerialPortPacketListener.class,
		SerialPortMessageListener.class,
		SerialPortEvent.class,
		SerialPortDataListener.class,
		SerialPortIOException.class,
		SerialPortDataListenerWithExceptions.class,
		SerialPortInvalidPortException.class,
		SerialPortTimeoutException.class
	}
	//	classNames = { //proxy classes go here
	//		"com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService_ClientProxy"
	//	}
)
public final class MyReflectionConfiguration {
	
	/**
	 * Prevent instantiation
	 */
	private MyReflectionConfiguration() {
	}
	
}
