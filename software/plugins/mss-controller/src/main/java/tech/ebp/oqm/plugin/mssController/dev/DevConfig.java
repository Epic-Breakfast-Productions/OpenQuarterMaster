package tech.ebp.oqm.plugin.mssController.dev;

import io.quarkus.arc.Unremovable;
import io.quarkus.arc.profile.IfBuildProfile;
import io.smallrye.config.ConfigMapping;

import java.util.List;

@Unremovable
@ConfigMapping(prefix = "moduleConfig.dev", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
@IfBuildProfile(anyOf = {"dev", "test"})
public interface DevConfig {

	List<DevModule> modules();

	interface DevModule {
		ModuleType type();
		ModuleInterfaceVersion interfaceVersion();
	}

	public static enum ModuleType {
		SERIAL, NET
	}

	public static enum ModuleInterfaceVersion {
		V1
	}




}
