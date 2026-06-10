package tech.ebp.oqm.core.api.health.utils;

import jakarta.enterprise.inject.Instance;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

public abstract class GenericHealthCheck<T extends HasHealthCheck> implements HealthCheck {
    private final String healthCheckName;
    private final Instance<T> providers;

    public GenericHealthCheck(String healthCheckName, Instance<T> providers) {
        this.healthCheckName = healthCheckName;
        this.providers = providers;
    }

    protected abstract HealthStatus getStatus(T provider);

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(this.healthCheckName);
        boolean allUp = true;

        for (T provider : this.providers) {
            HealthStatus status = this.getStatus(provider);
            boolean up = status.isUp();
            allUp &= up;
            builder.withData(status.getName() + ".up", up);
            builder.withData(status.getName() + ".status", status.getStatusMessage());
        }

        return (allUp ? builder.up() : builder.down()).build();
    }
}
