package tech.ebp.oqm.core.baseStation.testResources.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.RecordVideoSize;
import com.microsoft.playwright.options.ScreenSize;
import com.microsoft.playwright.options.ViewportSize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;

@Builder(toBuilder = true)
@Data
@Setter(AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class PlaywrightOptions {

	@Builder.Default
	private PlaywrightSetup.BrowserChoice browser = PlaywrightSetup.BrowserChoice.FIREFOX;

	@Builder.Default
	private boolean isMobile = false;

	@Builder.Default
	private ScreenSize screenSize = new ScreenSize(1920, 1080);

	@Builder.Default
	private ColorScheme colorScheme = ColorScheme.NO_PREFERENCE;


	public Browser.NewContextOptions newContextOptions(Path testResultDir) {
		return new Browser.NewContextOptions()
				   .setRecordVideoDir(testResultDir)
				   .setRecordHarPath(testResultDir.resolve("har.json"))
				   .setScreenSize(this.getScreenSize())
				   .setIsMobile(this.isMobile())
				   .setRecordVideoSize(new RecordVideoSize(this.getScreenSize().width, this.getScreenSize().height))
				   .setViewportSize(new ViewportSize(this.getScreenSize().width, this.getScreenSize().height))
				   .setColorScheme(this.getColorScheme())
			;
	}

}
