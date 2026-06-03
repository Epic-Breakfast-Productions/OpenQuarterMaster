package tech.ebp.oqm.core.api.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Liveness;

@Liveness
@ApplicationScoped
public class LivenessHealthCheck extends AbstractHealthCheck<HasLivenessCheck> {

    @Inject
    Instance<HasLivenessCheck> providers;

    @Override
    String getHealthCheckName() {
        return "Service Health - Liveness";
    }

    @Override
    Instance<HasLivenessCheck> getProviders() {
        return providers;
    }

    @Override
    HealthStatus getStatus(HasLivenessCheck p) {
        return p.getLivenessStatus();
    }
}
