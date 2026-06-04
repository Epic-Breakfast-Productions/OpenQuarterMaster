package tech.ebp.oqm.core.api.health.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Liveness;
import tech.ebp.oqm.core.api.health.GenericHealthCheck;
import tech.ebp.oqm.core.api.health.utils.HasLivenessCheck;

@Liveness
@ApplicationScoped
public class LivenessHealthCheck extends GenericHealthCheck<HasLivenessCheck> {

    @Inject
    LivenessHealthCheck(Instance<HasLivenessCheck> providers) {
        super("Service Health - Liveness", providers, HasLivenessCheck::getLivenessStatus);
    }
}
