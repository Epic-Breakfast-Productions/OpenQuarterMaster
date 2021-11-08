package com.ebp.openQuarterMaster.baseStation.data.sanitizers;

import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerInfoBean {
    @ConfigProperty(name = "service.authMode")
    AuthMode authMode;
}
