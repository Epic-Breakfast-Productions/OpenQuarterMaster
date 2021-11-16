package com.ebp.openQuarterMaster.baseStation.testResources.service;

import com.ebp.openQuarterMaster.baseStation.service.JwtService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@ApplicationScoped
@Alternative
public class TestExternalJwtService extends JwtService {
    public TestExternalJwtService(
            String privateKeyLocation,
            long defaultExpiration,
            long extendedExpiration,
            String issuer
    ) throws Exception {
        super(privateKeyLocation, defaultExpiration, extendedExpiration, issuer);
    }
}
