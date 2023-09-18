package com.ebp.openQuarterMaster.plugin.moduleInteraction.service;

import com.ebp.openQuarterMaster.plugin.config.VoiceSearchConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.nio.file.Paths.get;

/**
 * https://www.baeldung.com/docker-java-api
 * <p>
 * For podman systems, might need to run systemctl --user start podman.socket
 */
@Slf4j
@ApplicationScoped
public class VoiceSearchService {
	
	private static final String SPEECH_RESOURCES = "voice2json";
	
	private static final String CONTAINER_WORKING_DIR = "/tmp/oqm/v2jhome";
	private static final String CONTAINER_NAME = "oqm_plugin_mss_controller_plugin_voice2json";
	private static final String CONTAINER_RESULT_OUTPUT = "/process.out";
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	VoiceSearchConfig moduleConfig;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	ObjectMapper objectMapper;
	
	String sentencesFileLoc;
	String slotsDirLoc;
	String slotProgsDirLoc;
	
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
	
	private String performVoice2JsonCommand(DockerClient dockerClient, String command) throws IOException {
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
		
		
		//TODO:: don't do this, complete the above todo to do this through the lib
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
					--device /dev/snd:/dev/snd \
					-w /tmp/oqm/v2jhome \
					-e HOME=/tmp/oqm/v2jhome \
					--security-opt label=disable \
					synesthesiam/voice2json \
					""" + command
			)
									 .replaceAll("\\$\\{localV2jDir}", this.moduleConfig.container().volumeLoc())
									 .replaceAll("\\$\\{containerName}", CONTAINER_NAME);
			String[] splitCommand = fullCommand.split(" ");
			log.debug("Full command to run: \n{}", fullCommand);
			log.debug("Split command to run: \n{}", List.of(splitCommand));
			
			log.debug("executing voice2json command.");
			Process process = Runtime.getRuntime().exec(splitCommand);
			
			BufferedReader stdOutReader = process.inputReader();
			BufferedReader errOutReader = process.errorReader();
			log.debug("Waiting for command to complete.");
			resultCode = process.waitFor();
			log.debug("Done executing voice2json command. Exited with: {}", resultCode);
			
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
		return stdOut;
	}
	
	private void copyTrainingFile(String resourceFile, String destination) throws URISyntaxException, IOException {
		Path original = Paths.get(this.getClass().getClassLoader().getResource(SPEECH_RESOURCES + resourceFile).toURI());
		Path destinationPath = Paths.get(this.moduleConfig.container().volumeLoc() + destination);
		Files.copy(original, destinationPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	public void trainVoice2Text(DockerClient dockerClient) throws IOException {
		log.info("Training voice2text");
		
		//TODO:: modify contents of training
		
		this.performVoice2JsonCommand(dockerClient, "train-profile");
		
		log.info("Done training voice2text");
	}
	
	
	public void setupVoice2Text(DockerClient dockerClient) throws IOException {
		log.info("Setting up voice2Text profile.");
		this.performVoice2JsonCommand(dockerClient, "--help");
		//TODO:: nuke old profile?
		this.performVoice2JsonCommand(dockerClient, "-p en download-profile");
		
		this.performVoice2JsonCommand(dockerClient, "print-profile");//needs to be run twice, to avoid garbage in the first call
		ObjectNode profileJson = (ObjectNode) this.getObjectMapper().readTree(
			this.performVoice2JsonCommand(dockerClient, "print-profile")
		);
		
		this.sentencesFileLoc = profileJson.get("training").get("sentences-file").asText().replace(CONTAINER_WORKING_DIR, "");
		this.slotsDirLoc = profileJson.get("training").get("slots-directory").asText().replace(CONTAINER_WORKING_DIR, "");
		this.slotProgsDirLoc = profileJson.get("training").get("slot-programs-directory").asText().replace(CONTAINER_WORKING_DIR, "");
		
		log.debug(
			"Got profile dirs:  sentences file: {},  slots dir: {},  slot progs dir: {}",
			this.sentencesFileLoc,
			this.slotsDirLoc,
			this.slotProgsDirLoc
		);
		
		this.trainVoice2Text(dockerClient);
		
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
	
	public ObjectNode listenForIntent() throws IOException {
		ObjectNode output;
		try(
			DockerClient dockerClient = this.getDockerClient()
			){
			String listenData =  this.performVoice2JsonCommand(dockerClient, "transcribe-stream");
			output = (ObjectNode) this.getObjectMapper().readTree(listenData);
		}
		return output;
	}
	
}
