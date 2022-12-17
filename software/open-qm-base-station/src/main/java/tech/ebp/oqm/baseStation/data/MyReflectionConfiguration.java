package tech.ebp.oqm.baseStation.data;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.rest.ErrorMessage;

import java.time.ZonedDateTime;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)//prevent instantiation
public final class MyReflectionConfiguration {

}
