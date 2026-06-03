package tech.ebp.oqm.core.api.health;

public interface HasLivenessCheck {
    HealthStatus getLivenessStatus();
}
