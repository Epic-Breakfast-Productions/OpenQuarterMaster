package com.ebp.openQuarterMaster.baseStation.testResources.profiles;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NonDefaultTestProfile implements QuarkusTestProfile {


    private final String testProfile;
    private final Map<String, String> overrides = new HashMap<>();

    protected NonDefaultTestProfile(String testProfile) {
        this.testProfile = testProfile;
    }

    protected NonDefaultTestProfile(String testProfile, Map<String, String> configOverrides) {
        this(testProfile);
        this.overrides.putAll(configOverrides);
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        return new HashMap<>(this.overrides);
    }

    @Override
    public String getConfigProfile() {
        return testProfile;
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return QuarkusTestProfile.super.testResources();
    }
}
