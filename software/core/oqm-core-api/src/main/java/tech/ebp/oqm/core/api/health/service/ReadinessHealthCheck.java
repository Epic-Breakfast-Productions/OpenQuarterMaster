package tech.ebp.oqm.core.api.health.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Readiness;
import tech.ebp.oqm.core.api.health.utils.GenericHealthCheck;
import tech.ebp.oqm.core.api.health.utils.HasReadinessCheck;
import tech.ebp.oqm.core.api.health.utils.HealthStatus;

@Readiness
@ApplicationScoped
public class ReadinessHealthCheck extends GenericHealthCheck<HasReadinessCheck> {

    @Inject
    ReadinessHealthCheck(Instance<HasReadinessCheck> providers) {
        super("Service Health - Readiness", providers);
    }

    public ReadinessHealthCheck() {
        super("Service Health - Readiness", null);
    }

    @Override
    protected HealthStatus getStatus(HasReadinessCheck provider) {
        return provider.getReadinessStatus();
    }
}
