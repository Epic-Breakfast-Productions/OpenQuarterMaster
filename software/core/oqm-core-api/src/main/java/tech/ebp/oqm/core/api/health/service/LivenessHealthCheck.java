package tech.ebp.oqm.core.api.health.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Liveness;
import tech.ebp.oqm.core.api.health.utils.GenericHealthCheck;
import tech.ebp.oqm.core.api.health.utils.HasLivenessCheck;
import tech.ebp.oqm.core.api.health.utils.HealthStatus;

@Liveness
@ApplicationScoped
public class LivenessHealthCheck extends GenericHealthCheck<HasLivenessCheck> {

    @Inject
    LivenessHealthCheck(Instance<HasLivenessCheck> providers) {
        super("Service Health - Liveness", providers);
    }

    public LivenessHealthCheck() {
        super("Service Health - Liveness", null);
    }

    @Override
    protected HealthStatus getStatus(HasLivenessCheck provider) {
        return provider.getLivenessStatus();
    }
}
