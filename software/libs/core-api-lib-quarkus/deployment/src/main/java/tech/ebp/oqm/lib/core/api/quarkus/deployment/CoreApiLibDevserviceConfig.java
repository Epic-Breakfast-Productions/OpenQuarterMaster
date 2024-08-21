package tech.ebp.oqm.lib.core.api.quarkus.deployment;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.WithDefault;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

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
	 * Enables kafka.
	 */
	@ConfigItem(name="enableKafka")
	@WithDefault("false")
	public boolean enableKafka;
	
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
