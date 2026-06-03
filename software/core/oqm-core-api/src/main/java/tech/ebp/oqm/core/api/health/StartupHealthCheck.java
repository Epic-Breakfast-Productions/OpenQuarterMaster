package tech.ebp.oqm.core.api.health;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@Startup
@ApplicationScoped
public class StartupHealthCheck extends AbstractHealthCheck<HasStartupCheck> {

    @Inject
    Instance<HasStartupCheck> providers;

    @Override
    protected String getHealthCheckName() {
        return "Service Startup - Liveness";
    }

    @Override
    protected Instance<HasStartupCheck> getProviders() {
        return providers;
    }

    @Override
    protected HealthStatus getStatus(HasStartupCheck p) {
        return p.getStartupStatus();
    }
}
