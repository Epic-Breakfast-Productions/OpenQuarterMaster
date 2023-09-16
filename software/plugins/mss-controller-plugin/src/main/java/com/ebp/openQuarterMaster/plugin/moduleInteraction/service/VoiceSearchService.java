package com.ebp.openQuarterMaster.plugin.moduleInteraction.service;

import com.ebp.openQuarterMaster.plugin.config.VoiceSearchConfig;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * https://www.baeldung.com/docker-java-api
 * <p>
 * For podman systems, might need to run systemctl --user start podman.socket
 */
@Slf4j
@ApplicationScoped
public class VoiceSearchService {
	
	private static final String CONTAINER_WORKING_DIR = "/tmp/oqm/v2jhome";
	private static final String CONTAINER_NAME = "oqm_plugin_mss_controller_plugin_voice2json";
	private static final String CONTAINER_RESULT_OUTPUT = "/process.out";
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	VoiceSearchConfig moduleConfig;
	
	private DockerClient getDockerClient() {
		ZerodepDockerHttpClient client = new ZerodepDockerHttpClient.Builder()
											 .dockerHost(this.moduleConfig.container().engineUri())
											 .build();
		
		return DockerClientBuilder.getInstance()
				   .withDockerHttpClient(client)
				   .build();
	}
	
	@PostConstruct
	void init() throws IOException, InterruptedException {
		log.info(
			"Pulling image for voice search: {}:{}",
			this.getModuleConfig().container().image(),
			this.getModuleConfig().container().tag()
		);
		try (
			DockerClient dockerClient = this.getDockerClient();
		) {
			log.info(
				"Pulling image for voice search: {}:{}",
				this.getModuleConfig().container().image(),
				this.getModuleConfig().container().tag()
			);
			//pull image
			dockerClient.pullImageCmd(this.getModuleConfig().container().image())
				.withTag(this.getModuleConfig().container().tag())
				.exec(new PullImageResultCallback())
				.awaitCompletion(30, TimeUnit.SECONDS);
			log.info("Done pulling image for voice search.");
			this.setupVoice2Text(dockerClient);
		}
	}
	
	private void removeOldContainers(DockerClient client) {
		List<Container> result = client.listContainersCmd().withShowAll(true).withNameFilter(List.of(CONTAINER_NAME)).exec();
		
		log.info("Removing {} old matching containers.", result.size());
		
		for (Container curContainer : result) {
			log.debug("Removing old container {}", curContainer.getId());
			client.removeContainerCmd(curContainer.getId()).exec();
		}
		log.info("Done removing old containers.");
	}
	
	private void performVoice2JsonCommand(DockerClient dockerClient, String command) throws IOException {
		this.removeOldContainers(dockerClient);
		log.debug("Running new voice2json command \"{}\"", command);
		
		//TODO:: this should work, probably not working due to working on podman. Get working with this instead of hand-jamming console commands
		//		CreateContainerResponse container = dockerClient
		//												.createContainerCmd(this.moduleConfig.container().getFullImageRef())
		//												.withName(CONTAINER_NAME)
		//												.withWorkingDir(CONTAINER_WORKING_DIR)
		//												.withEnv("HOME=" + CONTAINER_WORKING_DIR)
		//												.withBinds(Bind.parse("/dev/shm:/dev/shm"))
		//												.withBinds(Bind.parse(this.moduleConfig.container().volumeLoc() + ":" + CONTAINER_WORKING_DIR))
		//												//.withUser()
		//												.withCmd(command)
		//												.exec();
		//		log.debug("Created new container, id: {}", container.getId());
		//		if(container.getWarnings().length != 0){
		//			log.warn(
		//				"Got {} warnings from container creation: {}",
		//				container.getWarnings().length,
		//				container.getWarnings()
		//			);
		//		}
		//		dockerClient.startContainerCmd(container.getId()).exec();
		//		dockerClient.logContainerCmd(container.getId()).exec(ResultCall);
		
		int resultCode;
		String stdOut;
		String errOut;
		try {
			String fullCommand = (
				"""
					docker run \
					--name ${containerName} \
					-v ${localV2jDir}:/tmp/oqm/v2jhome/ \
					-v /dev/shm/:/dev/shm/ \
					-w /tmp/oqm/v2jhome \
					-e HOME=/tmp/oqm/v2jhome \
					synesthesiam/voice2json \
					""" + command)
									 .replaceAll("\\$\\{localV2jDir}", this.moduleConfig.container().volumeLoc())
									 .replaceAll("\\$\\{containerName}", CONTAINER_NAME);
			String[] splitCommand = fullCommand.split(" ");
			log.debug("Full command to run: \n{}", fullCommand);
			log.debug("Split command to run: \n{}", List.of(splitCommand));
			
			Process process = Runtime.getRuntime().exec(splitCommand);
			BufferedReader stdOutReader = process.inputReader();
			BufferedReader errOutReader = process.errorReader();
			
			resultCode = process.waitFor();
			
			stdOut = stdOutReader.lines().collect(Collectors.joining());
			errOut = errOutReader.lines().collect(Collectors.joining());
		} catch(IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		log.info("Return code from running container: {}", resultCode);
		log.debug("stdout from running container: \n{}", stdOut);
		log.debug("stderr from running container: \n{}", errOut);
		
		if (resultCode != 0) {
			throw new RuntimeException("Failed to run container command! Code: " + resultCode);
		}
		try (
			FileOutputStream os = new FileOutputStream(
				this.moduleConfig.container().volumeLoc() + CONTAINER_RESULT_OUTPUT
			)
		) {
			os.write(stdOut.getBytes(StandardCharsets.UTF_8));
		}
	}
	
	public void setupVoice2Text(DockerClient dockerClient) throws IOException {
		log.info("Setting up voice2Text profile.");
		this.performVoice2JsonCommand(dockerClient, "--help");
		
		log.info("Done setting up voice2Text profile.");
	}
	
	public String getCurImageInformation() throws IOException {
		try (
			DockerClient dockerClient = this.getDockerClient();
		) {
			List<Image> images = dockerClient.listImagesCmd().withImageNameFilter(this.getModuleConfig().container().image()).exec();
			
			Image image = images.get(0);
			
			return image.toString();
		}
	}
	
	
}
