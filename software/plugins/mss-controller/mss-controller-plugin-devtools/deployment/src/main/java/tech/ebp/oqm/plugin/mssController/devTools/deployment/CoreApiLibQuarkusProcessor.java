package tech.ebp.oqm.plugin.mssController.devTools.deployment;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import tech.ebp.oqm.plugin.mssController.devTools.deployment.config.DevModuleConfig;
import tech.ebp.oqm.plugin.mssController.devTools.deployment.config.MssControllerDevtoolBuildTimeConfig;
import tech.ebp.oqm.plugin.mssController.devTools.deployment.testModules.MssTestModule;
import tech.ebp.oqm.plugin.mssController.devTools.deployment.testModules.MssTestSerialModule;
import tech.ebp.oqm.plugin.mssController.lib.command.response.ModuleInfo;

import java.util.*;

class CoreApiLibQuarkusProcessor {

	private static final String FEATURE = "mss-controller-devtooling";

	@BuildStep
	FeatureBuildItem feature() {
		return new FeatureBuildItem(FEATURE);
	}

//	@BuildStep
//	List<RunTimeConfigurationDefaultBuildItem> addRestConfiguration() {
//		return List.of(
//			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client."+Constants.CORE_API_CLIENT_NAME+".url", "${quarkus." + Constants.CONFIG_ROOT_NAME + ".coreApiBaseUri}"),
//			new RunTimeConfigurationDefaultBuildItem("quarkus.rest-client."+Constants.CORE_API_CLIENT_OIDC_NAME+".url", "${quarkus.oidc.auth-server-url:}")
//		);
//	}

	@BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
	public List<DevServicesResultBuildItem> createContainer(
		LaunchModeBuildItem launchMode,
		MssControllerDevtoolBuildTimeConfig config
	) {
		List<DevServicesResultBuildItem> output = new ArrayList<>();

		for (DevModuleConfig curModuleConfig : config.devServices().modules()) {
			ModuleInfo newModuleInfo = new ModuleInfo(
				curModuleConfig.info().specVersion().orElse("1"),
				curModuleConfig.info().serialId().orElse(UUID.randomUUID().toString()),
				curModuleConfig.info().manufactureDate().orElse("today"),
				curModuleConfig.info().numBlocks()
			);

			MssTestModule<?> testModule = switch (curModuleConfig.type()) {
				case SERIAL -> new MssTestSerialModule(newModuleInfo);
				case NETWORK -> null;
			};

			testModule.start();

			output.add(new DevServicesResultBuildItem.RunningDevService(
					FEATURE,
					testModule.getContainerId(),
					testModule::close,
					testModule.getAppConfig()
				)
					.toBuildItem()
			);

		}

//		ModuleInfo newModuleInfo = new ModuleInfo(
//			"1",
//			UUID.randomUUID().toString(),
//			"today",
//			5
//		);
//
//		MssTestModule<?> testModule = new MssTestSerialModule(newModuleInfo);
//		testModule.start();
//		output.add(new DevServicesResultBuildItem.RunningDevService(
//				FEATURE,
//				testModule.getContainerId(),
//				testModule::close,
//				testModule.getAppConfig()
//			)
//				.toBuildItem()
//		);

		return output;
	}
}
