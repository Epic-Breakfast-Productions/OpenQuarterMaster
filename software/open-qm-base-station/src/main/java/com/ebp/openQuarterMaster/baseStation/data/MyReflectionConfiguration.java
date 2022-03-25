package com.ebp.openQuarterMaster.baseStation.data;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.rest.ErrorMessage;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import com.ebp.openQuarterMaster.lib.core.user.User;
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
		ErrorMessage.class,
		ZonedDateTime.class,
		User.class,
		StorageBlock.class,
		SearchResult.class,
	},
	classNames = { //proxy classes go here
		"com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService_ClientProxy"
	}
)
public final class MyReflectionConfiguration {
	
	/**
	 * Prevent instantiation
	 */
	private MyReflectionConfiguration() {
	}
	
}
