package com.ebp.openQuarterMaster.baseStation.testResources.service;

import com.ebp.openQuarterMaster.baseStation.service.JwtService;

public class TestExternalJwtService extends JwtService {
    public TestExternalJwtService(String privateKeyLocation, long defaultExpiration, long extendedExpiration, String issuer) throws Exception {
        super(privateKeyLocation, defaultExpiration, extendedExpiration, issuer);
    }
}
