package com.ebp.openQuarterMaster.driverServer;
import com.fazecast.jSerialComm.SerialPort;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.ZonedDateTime;

/**
 * Required to tell GraalVm to keep classes around.
 * <p>
 * If running in native mode and get errors about classes, reflection, etc, add the erring class here
 *
 * https://quarkus.io/guides/writing-native-applications-tips#including-resources
 *
 * TODO:: test in native mode and go through to include all needed classes
 */
@RegisterForReflection(
	targets = { // Classes we know about go here
		SerialPort.class,
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
