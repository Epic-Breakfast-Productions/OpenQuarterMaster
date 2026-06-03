package tech.ebp.oqm.core.api.health;

import jakarta.enterprise.inject.Instance;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

public abstract class AbstractHealthCheck<T> implements HealthCheck {
    abstract String getHealthCheckName();
    abstract Instance<T> getProviders();
    abstract HealthStatus getStatus(T provider);

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(getHealthCheckName());
        boolean allUp = true;

        for (T provider : getProviders()) {
            HealthStatus status = getStatus(provider);
            boolean up = status.isUp();
            allUp &= up;
            builder.withData(status.getName() + ".up", up);
            builder.withData(status.getName() + ".status", status.getStatusMessage());
        }

        return (allUp ? builder.up() : builder.down()).build();
    }
}
