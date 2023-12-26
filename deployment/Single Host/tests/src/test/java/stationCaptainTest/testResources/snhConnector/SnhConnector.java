package stationCaptainTest.testResources.snhConnector;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.testResources.config.ConfigReader;
import stationCaptainTest.testResources.config.snhSetup.ContainerSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.ExistingSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhType;
import stationCaptainTest.testResources.config.snhSetup.installType.RepoInstallTypeConfig;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;

@Slf4j
@Data
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ContainerSnhSetupConfig.class, name = "CONTAINER"),
	@JsonSubTypes.Type(value = ExistingSnhSetupConfig.class, name = "EXISTING"),
})
public abstract class SnhConnector<C extends SnhSetupConfig> implements Closeable {
	
	protected C getSetupConfig() {
		try {
			//noinspection unchecked
			return (C) ConfigReader.getTestRunConfig().getSetupConfig();
		} catch(ClassCastException | IOException e) {
			log.error("FAILED to cast config as appropriate: ", e);
			throw new RuntimeException("FAILED to cast config as appropriate.", e);
		}
	}
	
	public void init(boolean install) {
		this.setupForInstall();
		if (install) {
			this.installOqm();
		}
	}
	
	public abstract SnhType getType();
	
	public abstract CommandResult runCommand(String... command);
	
	public abstract void copyToHost(String destination, File localFile);
	
	public abstract void copyFromHost(String remoteFile, File destination);
	
	public void copyToHost(String destinationDir, File... localFiles) {
		log.info("Copying files to host into {}: {}", destinationDir, (Object) localFiles);
		for (File curFile : localFiles) {
			this.copyToHost(destinationDir, curFile);
		}
	}
	
	public void setupForInstall() {
		switch (this.getSetupConfig().getInstallTypeConfig().getType()){
			case REPO -> {
				log.info("Setting up host for repo install.");
				RepoInstallTypeConfig config = (RepoInstallTypeConfig) this.getSetupConfig().getInstallTypeConfig();
				
				CommandResult result =
					this.runCommand("wget", "-q", "-O", "/tmp/repoSetup.sh",
									"https://deployment.openquartermaster.com/repos/"+config.getRepoBranch()+"/"+config.getInstallerType().name()+"/setup-repo.sh"
					);
				if(result.getReturnCode()!= 0){
					log.error("FAILED to run command to download repo setup script. Error: {} / std out: {}", result.getStdErr(), result.getStdOut());
					throw new RuntimeException("FAILED to run command to download repo setup script (returned "+result.getReturnCode()+"): " + result.getStdErr() + " / " + result.getStdOut());
				}
				result = this.runCommand("/tmp/repoSetup.sh");
				if(result.getReturnCode()!= 0){
					log.error("FAILED to run command to setup repo on host. Error: {}", result.getStdErr());
					throw new RuntimeException("FAILED to run command to setup repo on host (returned "+result.getReturnCode()+"): " + result.getStdErr());
				}
			}
			case FILES -> {
				//TODO:: this
			}
		}
	}
	
	public void installOqm() {
		switch (this.getSetupConfig().getInstallTypeConfig().getInstallerType()) {
			case DEB -> {
				//TODO
			}
			case RPM -> {
				//TODO
			}
		}
	}
	
	
	@Override
	public void close() throws IOException {
	}
	
	public static SnhConnector<?> fromConfig() throws IOException {
		switch (ConfigReader.getTestRunConfig().getSetupConfig().getType()){
			case EXISTING -> {
				return new ExistingSnhConnector();
			}
			case CONTAINER -> {
				return new ContainerSnhConnector();
			}
		}
		throw new RuntimeException("Invalid SNH type given.");
	}
}
