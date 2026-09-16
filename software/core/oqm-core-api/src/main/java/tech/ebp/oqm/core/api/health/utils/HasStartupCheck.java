package tech.ebp.oqm.core.api.health.utils;

public interface HasStartupCheck extends HasHealthCheck {
    HealthStatus getStartupStatus();
}
