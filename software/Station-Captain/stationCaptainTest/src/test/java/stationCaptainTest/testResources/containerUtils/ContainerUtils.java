package stationCaptainTest.testResources.containerUtils;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import stationCaptainTest.constants.ContainerConstants;
import stationCaptainTest.constants.FileLocationConstants;
import stationCaptainTest.testResources.TestContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static stationCaptainTest.constants.ContainerConstants.UBUNTU_20_04_IMAGE;

@Slf4j
public final class ContainerUtils {
	
	public static Container.ExecResult installStationCaptain(TestContext context, GenericContainer<?> container, boolean check) throws IOException, InterruptedException {
		Container.ExecResult result;
		
		if(container.getDockerImageName().startsWith("ubuntu")){
			result = container.execInContainer("apt-get", "install", "-y", (String)context.getData().get(ContainerConstants.CONFIG_KEY_INSTALLER_LOCATION));
		} else if(container.getDockerImageName().startsWith("fedora")){
			result = container.execInContainer("yum", "install", "-y", (String)context.getData().get(ContainerConstants.CONFIG_KEY_INSTALLER_LOCATION));
		} else {
			throw new IllegalStateException("Unexpected value: " + container.getDockerImageName());
		}
		
		if(check){
			assertEquals(0, result.getExitCode());
		}
		
		return result;
	}
	public static Container.ExecResult installStationCaptain(TestContext context, GenericContainer<?> container) throws IOException, InterruptedException {
		return installStationCaptain(context, container, true);
	}
	
	public static GenericContainer<?> startContainer(TestContext context, Path oqmCaptInstaller, DockerImageName imageName, boolean install) throws IOException, InterruptedException {
		GenericContainer<?> container = new GenericContainer<>(imageName);
		container.withCommand("tail -f /dev/null");
		container.withEnv("TZ", "UTC");
		container.withEnv("DEBIAN_FRONTEND", "noninteractive");
//		container.withEnv("TERM", "xterm");
//		container.setExposedPorts();
		container.withStartupTimeout(ContainerConstants.STARTUP_WAIT);
		
		container.start();
		container.copyFileToContainer(MountableFile.forHostPath(oqmCaptInstaller), FileLocationConstants.INSTALLER_CONTAINER_LOCATION);
		context.getData().put(ContainerConstants.CONFIG_KEY_INSTALLER_LOCATION, FileLocationConstants.INSTALLER_CONTAINER_LOCATION + oqmCaptInstaller.getFileName().toString());
		
		Container.ExecResult result = container.execInContainer("pwd");
		log.info("Working directory: {}", result.getStdout());
		result = container.execInContainer("whoami");
		log.info("User when exec: {}", result.getStdout());
		
		if(oqmCaptInstaller.toString().endsWith("deb")){
			log.info("Deb, updating cache");
			
			result = container.execInContainer("ln", "-fs", "/usr/share/zoneinfo/America/New_York /etc/localtime");
			result = container.execInContainer("apt-get", "update");
			log.info("Stdout of updating apt: {}", result.getStdout());
			if(result.getExitCode() != 0){
				throw new IllegalStateException("Failed to update apt; " + result.getExitCode() + " - " + result.getStderr());
			}
		}
		
		if(install) {
			installStationCaptain(context, container);
		}
		
//		container.waitingFor(new WaitAllStrategy(WaitAllStrategy.Mode.WITH_MAXIMUM_OUTER_TIMEOUT));
		
		return container;
	}
	
	public static GenericContainer<?> startContainer(TestContext context, String installerType, String os, boolean install) throws IOException, InterruptedException {
		Path installerPath;
		
		try (
			Stream<Path> found = Files.find(
				FileLocationConstants.INSTALLER_OUTPUT_DIR,
				Integer.MAX_VALUE,
				(path, basicFileAttributes)->path.toFile().getName().matches(".*." + installerType)
			);
		) {
			Optional<Path> foundOp = found.findFirst();
			assertTrue(foundOp.isPresent(), "Installer not found for ." + installerType);
			installerPath = foundOp.get();
		}
		
		DockerImageName dockerImageName = switch (os){
			case "ubuntu", "ubuntu 20.04" -> UBUNTU_20_04_IMAGE;
			case "fedora", "fedora 37" -> ContainerConstants.FEDORA_37_IMAGE;
			default -> throw new IllegalArgumentException("Unsupported OS given: " + os);
		};
		
		return startContainer(context, installerPath, dockerImageName, install);
	}
}
