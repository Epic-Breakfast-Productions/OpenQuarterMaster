package stationCaptainTest.constants;

import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public final class ContainerConstants {
	public static final Duration STARTUP_WAIT = Duration.of(5, ChronoUnit.MINUTES);
	public static final String CONFIG_KEY_INSTALLER_LOCATION = "installerLocation";
	
	// https://hub.docker.com/_/ubuntu
	public static final DockerImageName UBUNTU_20_04_IMAGE = DockerImageName.parse("ubuntu:20.04");
	public static final String UBUNTU_20_04_IMAGE_NS = UBUNTU_20_04_IMAGE.asCanonicalNameString();
	
	// https://hub.docker.com/_/fedora
	public static final DockerImageName FEDORA_37_IMAGE = DockerImageName.parse("fedora:37");
	public static final String FEDORA_37_IMAGE_NS = FEDORA_37_IMAGE.asCanonicalNameString();
	
}
