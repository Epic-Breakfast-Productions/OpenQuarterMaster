package com.ebp.openQuarterMaster.testResources.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;

import java.io.Closeable;
import java.io.IOException;

@Slf4j
public class PlaywrightSetup implements Closeable {

	@Getter
	private static final PlaywrightSetup INSTANCE = new PlaywrightSetup();


	@Getter
	private final Playwright playwright;
	@Getter
	private final Browser browser;
	@Getter
	private boolean closed = false;

	{
		log.info("Setting up playwright for UI tests.");
		playwright = Playwright.create();
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
