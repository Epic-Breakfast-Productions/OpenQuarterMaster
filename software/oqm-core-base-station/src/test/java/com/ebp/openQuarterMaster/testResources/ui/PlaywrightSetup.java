package com.ebp.openQuarterMaster.testResources.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.nio.file.Path;

@Slf4j
public class PlaywrightSetup implements Closeable {

	@Getter
	private static final PlaywrightSetup INSTANCE = new PlaywrightSetup();
	public static final Path RECORDINGS_DIR = Path.of("build/testRecordings/");


	@Getter
	private final Playwright playwright;
	@Getter
	private final Browser browser;
	@Getter
	private boolean closed = false;

	{
		log.info("Setting up playwright for UI tests.");
		playwright = Playwright.create();
		//TODO:: choose browser based on config
		browser = playwright.firefox().launch();

		log.info("DONE setting up playwright.");
	}

	@Override
	public void close() {
		if(this.isClosed()){
			return;
		}
		log.info("Cleaning up playwright.");
		browser.close();
		playwright.close();
		log.info("DONE cleaning up playwright.");
		this.closed = true;
	}
}
