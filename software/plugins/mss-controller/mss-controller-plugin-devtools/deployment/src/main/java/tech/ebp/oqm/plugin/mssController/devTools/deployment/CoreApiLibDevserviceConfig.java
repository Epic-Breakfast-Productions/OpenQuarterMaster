package tech.ebp.oqm.plugin.mssController.devTools.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.smallrye.config.WithDefault;

import java.io.File;
import java.util.Optional;

@ConfigGroup
public class CoreApiLibDevserviceConfig {
	/**
	 * Enable devservices
	 */
	@WithDefault("true")
	public boolean enable;
	
	/**
	 * The path of the public key file
	 */
	@ConfigItem(name="certPath")
	public Optional<File> certPath;

	/**
	 * The path of the private key file
	 */
	@ConfigItem(name="certKeyPath")
	public Optional<File> certKeyPath;

	/**
	 * The version/ tag of the core api container image
	 */
	@ConfigItem(name="coreApiVersion", defaultValue = "2.1.2")
	public String coreApiVersion;
}
