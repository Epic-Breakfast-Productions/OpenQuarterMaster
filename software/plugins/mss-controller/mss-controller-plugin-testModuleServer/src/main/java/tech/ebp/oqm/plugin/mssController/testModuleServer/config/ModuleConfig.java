package tech.ebp.oqm.plugin.mssController.testModuleServer.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix="moduleConfig", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface ModuleConfig {
	String specVersion();
	String serialId();
	String manufactureDate();
	int numBlocks();

	TestModuleType type();


	enum TestModuleType{
		SERIAL, NETWORK
	}
}
