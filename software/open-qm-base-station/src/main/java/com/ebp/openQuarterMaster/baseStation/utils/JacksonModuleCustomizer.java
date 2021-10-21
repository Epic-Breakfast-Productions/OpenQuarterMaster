package com.ebp.openQuarterMaster.baseStation.utils;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;

import javax.inject.Singleton;

@Singleton
public class JacksonModuleCustomizer implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        Utils.setupObjectMapper(mapper);
    }
}
