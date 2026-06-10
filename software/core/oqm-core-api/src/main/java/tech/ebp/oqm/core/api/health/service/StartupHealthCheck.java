package tech.ebp.oqm.core.api.health.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.Startup;
import tech.ebp.oqm.core.api.health.utils.GenericHealthCheck;
import tech.ebp.oqm.core.api.health.utils.HasStartupCheck;
import tech.ebp.oqm.core.api.health.utils.HealthStatus;

@Startup
@ApplicationScoped
public class StartupHealthCheck extends GenericHealthCheck<HasStartupCheck> {

    @Inject
    StartupHealthCheck(Instance<HasStartupCheck> providers) {
        super("Service Health - Startup", providers);
    }

    public StartupHealthCheck() {
        super("Service Health - Startup", null);
    }

    @Override
    protected HealthStatus getStatus(HasStartupCheck provider) {
        return provider.getStartupStatus();
    }
}
