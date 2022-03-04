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
 * If running in native mode and get errors about reflection, etc, add the erring class here
 */
@RegisterForReflection(
	targets = {
		ErrorMessage.class,
		ZonedDateTime.class,
		User.class,
		StorageBlock.class,
		SearchResult.class
		//TODO:: test in native mode and go through to include all needed classes
	}
)
public final class MyReflectionConfiguration {
	
	/**
	 * Prevent instantiation
	 */
	private MyReflectionConfiguration() {
	}
	
}
