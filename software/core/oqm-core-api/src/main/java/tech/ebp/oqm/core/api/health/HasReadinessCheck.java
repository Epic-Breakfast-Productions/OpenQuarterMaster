package tech.ebp.oqm.core.api.health;

public interface HasReadinessCheck {
    HealthStatus getReadinessStatus();
}
