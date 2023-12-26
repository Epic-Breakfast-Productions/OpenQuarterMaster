package stationCaptainTest.testResources.snhConnector;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import stationCaptainTest.constants.ContainerConstants;
import stationCaptainTest.testResources.config.snhSetup.ContainerSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhType;

import java.io.File;
import java.io.IOException;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class ContainerSnhConnector extends SnhConnector<ContainerSnhSetupConfig> {
	
	//TODO:: container resources
	private GenericContainer<?> runningContainer = null;
	
	@Override
	public void init(boolean install) {
		this.runningContainer = new GenericContainer<>(this.getSetupConfig().getDockerImageName());
		this.runningContainer.withCommand("tail -f /dev/null");
		this.runningContainer.withEnv("TZ", "UTC");
		this.runningContainer.withEnv("DEBIAN_FRONTEND", "noninteractive");
		//		container.withEnv("TERM", "xterm");
		//		container.setExposedPorts();
		this.runningContainer.withStartupTimeout(ContainerConstants.STARTUP_WAIT);
		
		this.runningContainer.start();
		
		Container.ExecResult result = null;
		try {
			result = this.runningContainer.execInContainer("pwd");
			log.info("Working directory: {}", result.getStdout());
			result = this.runningContainer.execInContainer("whoami");
			log.info("User when exec: {}", result.getStdout());
		} catch(Exception e) {
			throw new RuntimeException("FAILED to do simple checks after container start", e);
		}
		
//		this.runningContainer.copyFileToContainer(MountableFile.forHostPath(oqmCaptInstaller), FileLocationConstants.INSTALLER_CONTAINER_LOCATION);
//		context.getData().put(ContainerConstants.CONFIG_KEY_INSTALLER_LOCATION, FileLocationConstants.INSTALLER_CONTAINER_LOCATION + oqmCaptInstaller.getFileName().toString());
//
//		if(oqmCaptInstaller.toString().endsWith("deb")){
//			log.info("Deb, updating cache");
//
//			result = container.execInContainer("ln", "-fs", "/usr/share/zoneinfo/America/New_York /etc/localtime");
//			result = container.execInContainer("apt-get", "update");
//			log.info("Stdout of updating apt: {}", result.getStdout());
//			if(result.getExitCode() != 0){
//				throw new IllegalStateException("Failed to update apt; " + result.getExitCode() + " - " + result.getStderr());
//			}
//		}
		
		super.init(install);
	}
	
	@Override
	public SnhType getType() {
		return SnhType.CONTAINER;
	}
	
	@Override
	public CommandResult runCommand(String... command) {
		try {
			return CommandResult.from(this.runningContainer.execInContainer(command));
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void copyToHost(String destination, File localFile) {
		//TODO
	}
	
	@Override
	public void copyFromHost(String remoteFile, File destination) {
		//TODO
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		if(this.runningContainer != null){
			this.runningContainer.close();
		}
	}
}
