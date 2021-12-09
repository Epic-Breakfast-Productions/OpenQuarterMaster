package com.ebp.openQuarterMaster.plugin.testResources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO:: make better with configuration
 */
public class TestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {
    public static final String OTHER_PROFILE = "otherProfile";

	private boolean otherProfile = false;

    @Override
    public void init(Map<String, String> initArgs) {
        this.otherProfile = Boolean.parseBoolean(initArgs.getOrDefault(OTHER_PROFILE, Boolean.toString(this.otherProfile)));
    }

    @Override
    public Map<String, String> start() {

        Map<String, String> configOverride = new HashMap<>();

        if(this.otherProfile){
            configOverride.put("some.valueTwo", "something else");
        }


        return configOverride;
    }

    @Override
    public void stop() {

    }
}
