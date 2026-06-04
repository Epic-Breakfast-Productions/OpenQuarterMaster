package tech.ebp.oqm.core.api.health;

import jakarta.enterprise.inject.Instance;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import java.util.function.Function;

public class GenericHealthCheck<T> implements HealthCheck {
    private final String healthCheckName;
    private final Instance<T> providers;
    private final Function<T, HealthStatus> statusGetter;

    public GenericHealthCheck(String healthCheckName, Instance<T> providers, Function<T, HealthStatus> statusGetter) {
        this.healthCheckName = healthCheckName;
        this.providers = providers;
        this.statusGetter = statusGetter;
    }

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.named(this.healthCheckName);
        boolean allUp = true;

        for (T provider : this.providers) {
            HealthStatus status = this.statusGetter.apply(provider);
            boolean up = status.isUp();
            allUp &= up;
            builder.withData(status.getName() + ".up", up);
            builder.withData(status.getName() + ".status", status.getStatusMessage());
        }

        return (allUp ? builder.up() : builder.down()).build();
    }
}
