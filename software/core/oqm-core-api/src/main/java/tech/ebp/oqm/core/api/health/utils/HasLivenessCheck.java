package tech.ebp.oqm.core.api.health.utils;

public interface HasLivenessCheck extends HasHealthCheck {
    HealthStatus getLivenessStatus();
}
