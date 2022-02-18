package com.ebp.openQuarterMaster.baseStation.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import javax.enterprise.context.ApplicationScoped;

@Liveness
@ApplicationScoped
public class ServiceRunningHealthCheck implements HealthCheck {
	
	@Override
	public HealthCheckResponse call() {
		return HealthCheckResponse.up("Service is running");
	}
}