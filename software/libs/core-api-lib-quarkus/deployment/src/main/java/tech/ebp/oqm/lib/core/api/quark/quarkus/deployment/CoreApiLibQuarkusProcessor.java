package tech.ebp.oqm.lib.core.api.quark.quarkus.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class CoreApiLibQuarkusProcessor {

    private static final String FEATURE = "core-api-lib-quarkus";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
