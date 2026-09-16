package tech.ebp.oqm.core.api.health.utils;

public interface HasReadinessCheck extends HasHealthCheck {
    HealthStatus getReadinessStatus();
}
