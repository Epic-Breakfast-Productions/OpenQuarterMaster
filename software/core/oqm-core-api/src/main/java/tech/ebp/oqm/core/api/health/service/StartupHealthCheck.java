package tech.ebp.oqm.core.api.health.service;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import tech.ebp.oqm.core.api.health.GenericHealthCheck;
import tech.ebp.oqm.core.api.health.utils.HasStartupCheck;

@Startup
@ApplicationScoped
public class StartupHealthCheck extends GenericHealthCheck<HasStartupCheck> {

    @Inject
    StartupHealthCheck(Instance<HasStartupCheck> providers) {
        super("Service Health - Startup", providers, HasStartupCheck::getStartupStatus);
    }
}
