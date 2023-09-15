package com.ebp.openQuarterMaster.plugin.moduleInteraction.service;

import com.ebp.openQuarterMaster.plugin.config.VoiceSearchConfig;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * https://www.baeldung.com/docker-java-api
 */
@Slf4j
@ApplicationScoped
public class VoiceSearchService {
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	VoiceSearchConfig moduleConfig;

	private DockerClient getDockerClient() {
		ZerodepDockerHttpClient client = new ZerodepDockerHttpClient.Builder()
								  .dockerHost(URI.create("unix:///var/run/docker.sock"))
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
		}
	}

	public String getCurImageVersion() throws IOException {
//		try (
//			DockerClient dockerClient = this.getDockerClient();
//		) {
//			List<Image> images = dockerClient.listImagesCmd().withImageNameFilter(this.getModuleConfig().container().image()).exec();
//
//			Image image = images.get(0);
//
//			return image.toString();
//		}
		return "";
	}
	
	
}
