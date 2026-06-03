package tech.ebp.oqm.core.api.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class ReadinessHealthCheck extends AbstractHealthCheck<HasReadinessCheck> {

    @Inject
    Instance<HasReadinessCheck> providers;

    @Override
    protected String getHealthCheckName() {
        return "readiness";
    }

    @Override
    protected Instance<HasReadinessCheck> getProviders() {
        return providers;
    }

    @Override
    protected HealthStatus getStatus(HasReadinessCheck p) {
        return p.getReadinessStatus();
    }
}
