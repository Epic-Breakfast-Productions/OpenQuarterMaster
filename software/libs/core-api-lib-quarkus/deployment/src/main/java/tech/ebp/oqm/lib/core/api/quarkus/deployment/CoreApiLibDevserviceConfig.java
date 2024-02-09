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
	 * Enable logging to a file.
	 */
	@WithDefault("true")
	public boolean enable;
	
	/**
	 * The name of the file in which logs will be written.
	 */
	@ConfigItem(name="certPath")
	public Optional<File> certPath;
	/**
	 * The name of the file in which logs will be written.
	 */
	@ConfigItem(name="certKeyPath")
	public Optional<File> certKeyPath;
}
