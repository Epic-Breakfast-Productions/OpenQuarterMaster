package com.ebp.openQuarterMaster.plugin.moduleInteraction.service;

import com.ebp.openQuarterMaster.plugin.config.VoiceSearchConfig;
import com.ebp.openQuarterMaster.plugin.moduleInteraction.ItemVoiceSearchResults;
import com.ebp.openQuarterMaster.plugin.restClients.KcClientAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
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
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.InventoryItemSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.nio.file.Paths.get;

/**
 * https://www.baeldung.com/docker-java-api
 * <p>
 * For podman systems, need to run systemctl --user start podman.socket
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
	VoiceSearchConfig voiceSearchConfig;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	ObjectMapper objectMapper;
	
	@RestClient
	@Getter(AccessLevel.PRIVATE)
	OqmCoreApiClientService coreApiClient;
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	ItemSearchService itemSearchService;
	@Inject
	@Getter(AccessLevel.PRIVATE)
	KcClientAuthService kcClientAuthService;
	
	String sentencesFileLoc;
	String slotsDirLoc;
	String slotProgsDirLoc;
	
	private DockerClient getDockerClient() {
		ZerodepDockerHttpClient client = new ZerodepDockerHttpClient.Builder()
											 .dockerHost(this.voiceSearchConfig.container().engineUri())
											 .build();
		
		return DockerClientBuilder.getInstance()
				   .withDockerHttpClient(client)
				   .build();
	}
	
	@PostConstruct
	void init() throws IOException, InterruptedException, URISyntaxException {
		if (!this.voiceSearchConfig.enabled()) {
			log.info("Voice search is disabled in configuration.");
			return;
		}
		
		log.info(
			"Pulling image for voice search: {}:{}",
			this.getVoiceSearchConfig().container().image(),
			this.getVoiceSearchConfig().container().tag()
		);
		try (
			DockerClient dockerClient = this.getDockerClient();
		) {
			log.info(
				"Pulling image for voice search: {}:{}",
				this.getVoiceSearchConfig().container().image(),
				this.getVoiceSearchConfig().container().tag()
			);
			//pull image
			dockerClient.pullImageCmd(this.getVoiceSearchConfig().container().image())
				.withTag(this.getVoiceSearchConfig().container().tag())
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
	
	private String performCommandLineCommand(String... splitCommand) throws IOException {
		log.debug("Executing command: \n{}", List.of(splitCommand));
		int resultCode;
		String stdOut;
		String errOut;
		try {
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
				this.voiceSearchConfig.container().volumeLoc() + CONTAINER_RESULT_OUTPUT
			)
		) {
			os.write(stdOut.getBytes(StandardCharsets.UTF_8));
		}
		return stdOut;
	}
	
	private String command(String command) throws IOException {
		String[] splitCommand = command.split(" ");
		log.debug("Full command to run: \n{}", command);
		log.debug("Split command to run: \n{}", List.of(splitCommand));
		return this.performCommandLineCommand(splitCommand);
	}
	
	private String performVoice2JsonCommand(DockerClient dockerClient, String... commandArgs) throws IOException {
		this.removeOldContainers(dockerClient);
		log.debug("Running new voice2json command \"{}\"", (Object[]) commandArgs);
		
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
		
		List<String> commandArgsList = new ArrayList<>();
		commandArgsList.add("docker");
		commandArgsList.add("run");
		commandArgsList.add("--name");
		commandArgsList.add(CONTAINER_NAME);
		commandArgsList.add("-v");
		commandArgsList.add(this.voiceSearchConfig.container().volumeLoc() + ":/tmp/oqm/v2jhome/");
		commandArgsList.add("-v");
		commandArgsList.add("/dev/shm/:/dev/shm/");
		commandArgsList.add("--device");
		commandArgsList.add("/dev/snd:/dev/snd");
		commandArgsList.add("-w");
		commandArgsList.add(CONTAINER_WORKING_DIR);
		commandArgsList.add("-e");
		commandArgsList.add("HOME=" + CONTAINER_WORKING_DIR);
		commandArgsList.add("--security-opt");
		commandArgsList.add("label=disable");
		commandArgsList.add("synesthesiam/voice2json");
		
		commandArgsList.addAll(List.of(commandArgs));
		
		//		String fullCommand = (
		//			"""
		//				docker run \
		//				--name ${containerName} \
		//				-v ${localV2jDir}:/tmp/oqm/v2jhome/ \
		//				-v /dev/shm/:/dev/shm/ \
		//				--device /dev/snd:/dev/snd \
		//				-w /tmp/oqm/v2jhome \
		//				-e HOME=/tmp/oqm/v2jhome \
		//				--security-opt label=disable \
		//				synesthesiam/voice2json \
		//				""" + command
		//		)
		//								 .replaceAll("\\$\\{localV2jDir}", this.moduleConfig.container().volumeLoc())
		//								 .replaceAll("\\$\\{containerName}", CONTAINER_NAME);
		
		//TODO:: don't do this, complete the above todo to do this through the lib
		return this.performCommandLineCommand(commandArgsList.toArray(new String[0]));
	}
	
	private void copyTrainingFile(String resourceFile, String destination) throws URISyntaxException, IOException {
		
		Path original = Paths.get(this.getClass().getClassLoader().getResource(SPEECH_RESOURCES + resourceFile).toURI());
		Path destinationPath = Paths.get(this.voiceSearchConfig.container().volumeLoc() + destination);
		Files.copy(original, destinationPath, StandardCopyOption.REPLACE_EXISTING);
	}
	
	private void assertEnabled() {
		if (!this.enabled()) {
			throw new VoiceDisabledException();
		}
	}
	
	public boolean enabled() {
		return this.getVoiceSearchConfig().enabled();
	}
	
	public void trainVoice2Text(DockerClient dockerClient) throws IOException, URISyntaxException {
		this.assertEnabled();
		log.info("Training voice2text");
		
		this.copyTrainingFile("/sentences.ini", this.sentencesFileLoc);
		
		log.debug("Creating items slot file.");
		Set<String> nameSet = new HashSet<>();
		{
			//TODO:: this should be done better; do paging
			ObjectNode allItems = this.getCoreApiClient().invItemSearch(this.kcClientAuthService.getAuthString(), InventoryItemSearch.builder().build()).await().indefinitely();
			for (Iterator<JsonNode> it = allItems.get("results").elements(); it.hasNext(); ) {
				ObjectNode curItem = (ObjectNode) it.next();
				String curItemName = curItem.get("name").asText();
				
				for (String curItemNameSection : curItemName.split(" ")) {
					curItemNameSection = curItemNameSection
											 .replaceAll("[^a-zA-Z ]", "")
											 .toLowerCase()
											 .strip();
					if (curItemNameSection.isEmpty()) {
						continue;
					}
					
					nameSet.add(curItemNameSection);
					
					if (!curItemNameSection.endsWith("s")) {
						nameSet.add(curItemNameSection + "s");
					} else {
						curItemNameSection = curItemNameSection.replaceFirst(".$", "");
						if (!curItemNameSection.isEmpty()) {
							nameSet.add(curItemNameSection);
						}
						
					}
				}
			}
		}
		File itemsFile = new File(this.voiceSearchConfig.container().volumeLoc() + this.slotsDirLoc + "/item");
		log.debug("Creating items list file: {}", itemsFile);
		log.trace("Got items: {}", nameSet);
		itemsFile.getParentFile().mkdirs();
		itemsFile.createNewFile();
		try (
			FileOutputStream os = new FileOutputStream(itemsFile);
		) {
			os.write(String.join(System.lineSeparator(), nameSet).getBytes(StandardCharsets.UTF_8));
			os.flush();
		}
		log.debug("Done creating items slot file.");
		
		this.performVoice2JsonCommand(dockerClient, "train-profile");
		
		log.info("Done training voice2text");
	}
	
	
	public void setupVoice2Text(DockerClient dockerClient) throws IOException, URISyntaxException {
		this.assertEnabled();
		log.info("Setting up voice2Text profile.");
		
		if (new File(this.voiceSearchConfig.container().volumeLoc() + "/.local/share/").mkdirs()) {
			log.debug("Made initial dirs for profile.");
		} else {
			log.debug("Initial dirs for profile were already present.");
		}
		
		this.performVoice2JsonCommand(dockerClient, "--help");
		//TODO:: nuke old profile?
		this.performVoice2JsonCommand(dockerClient, "-p", "en", "download-profile");
		
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
		this.assertEnabled();
		try (
			DockerClient dockerClient = this.getDockerClient();
		) {
			List<Image> images = dockerClient.listImagesCmd().withImageNameFilter(this.getVoiceSearchConfig().container().image()).exec();
			
			Image image = images.get(0);
			
			return image.toString();
		}
	}
	
	public ObjectNode listenForIntent() throws IOException {
		this.assertEnabled();
		ObjectNode output;
		
		//TODO:: we should not need to do this extra component, but docker + audio is a huge PIA.
		// Not having to do this would enable much faster recognition
		//TODO:: think about taking recording from web/having the option?
		String voiceRecordOutput = this.performCommandLineCommand(
			"arecord",
			"--format=cd",
			"-d", "4",
			this.voiceSearchConfig.container().volumeLoc() + "/record.wav"
		);
		
		try (
			DockerClient dockerClient = this.getDockerClient()
		) {
			//TODO:: see above. Should be able to do "transcribe-stream"
			String listenData = this.performVoice2JsonCommand(dockerClient, "transcribe-wav", "record.wav");
			try (
				FileOutputStream os = new FileOutputStream(this.voiceSearchConfig.container().volumeLoc() + "/record-out.json");
			) {
				os.write(listenData.getBytes(StandardCharsets.UTF_8));
				os.flush();
			}
			
			String intentData = this.performVoice2JsonCommand(dockerClient, "recognize-intent", listenData);
			
			output = (ObjectNode) this.getObjectMapper().readTree(intentData);
		}
		return output;
	}
	
	public ItemVoiceSearchResults searchForItems() throws IOException {
		this.assertEnabled();
		
		ObjectNode intentResult = this.listenForIntent();
		
		//TODO:: ensure intent result is valid
		
		
		InventoryItemSearch search = InventoryItemSearch.builder()
										 .name(intentResult.get("slots").get("item_name").asText())
										 .build();
		
		return ItemVoiceSearchResults.from(
			this.getItemSearchService().searchForItemLocations(
				search,
				true
			),
			intentResult,
			search
		);
	}
	
}
