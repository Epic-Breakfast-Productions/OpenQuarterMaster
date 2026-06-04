package tech.ebp.oqm.core.api.health.utils;

import tech.ebp.oqm.core.api.health.HealthStatus;

public interface HasLivenessCheck {
    HealthStatus getLivenessStatus();
}
