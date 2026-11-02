package tech.ebp.oqm.core.baseStation.testResources.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ColorScheme;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.params.provider.Arguments;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class PlaywrightSetup implements Closeable {

	public static final Path RECORDINGS_DIR = Path.of("build/test-results/addendum/ui/"+ ConfigProvider.getConfig().getValue("quarkus.profile", String.class) + "/");
	private static PlaywrightSetup INSTANCE = null;

	public static synchronized PlaywrightSetup getInstance() {
		if(INSTANCE == null || INSTANCE.isClosed()){
			INSTANCE = new PlaywrightSetup();
		}
		return INSTANCE;
	}

	@Getter
	private final Playwright playwright;
	@Getter
	private final Browser browser;
	@Getter
	private boolean closed = false;

	{
		log.info("Setting up playwright for UI tests.");
		try {
			this.playwright = Playwright.create();
		} catch (Throwable e){
			log.error("Failed creating new playwright: ", e);
			throw e;
		}
		//TODO:: choose browser based on config
		this.browser = playwright.firefox()
						   .launch();

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


	private static final List<ColorScheme> COLOR_SCHEMES = List.of(ColorScheme.LIGHT, ColorScheme.DARK);
	private static final List<BrowserType> BROWSER_TYPES = Arrays.stream(BrowserChoice.values()).map(
		(c)->switch (c){
			case FIREFOX -> getInstance().getPlaywright().firefox();
			case CHROME -> getInstance().getPlaywright().chromium();
		}
	).toList();

	public static Stream<PlaywrightOptions> getBrowserOps(){
		Stream.Builder<PlaywrightOptions> builder = Stream.builder();

		for(ColorScheme colorScheme : COLOR_SCHEMES){
			for(BrowserType browserType : BROWSER_TYPES){
				builder.add(
					PlaywrightOptions.builder()
						.colorScheme(colorScheme)
//						.browser(browserType)//not dealing with this yet. Need to update code and tests too slow
						.build()
				);
			}
		}

		return builder.build();
	}

	public static enum BrowserChoice {
		FIREFOX,
		CHROME
	}
}
