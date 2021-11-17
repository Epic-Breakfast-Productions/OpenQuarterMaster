package com.ebp.openQuarterMaster.baseStation.data;

import com.ebp.openQuarterMaster.lib.core.rest.ErrorMessage;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(
//        classNames = {
//                "com.ebp.openQuarterMaster.lib.core.*"
//        }
        targets = {
                ErrorMessage.class,
                //TODO:: test in native mode and go through to include all needed classes
        }
)
public class MyReflectionConfiguration {
}
