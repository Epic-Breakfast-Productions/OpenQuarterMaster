package com.ebp.openQuarterMaster.baseStation.data;

import com.ebp.openQuarterMaster.lib.core.rest.ErrorMessage;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Required to tell GraalVm to keep classes around.
 * <p>
 * If running in native mode and get errors about reflection, etc, add the erring class here
 */
@RegisterForReflection(
	targets = {
		ErrorMessage.class,
		//TODO:: test in native mode and go through to include all needed classes
	}
)
public class MyReflectionConfiguration {

}
