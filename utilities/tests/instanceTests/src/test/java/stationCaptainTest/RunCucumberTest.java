package stationCaptainTest;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.core.cli.CommandlineOptions.TAGS;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key=ANSI_COLORS_DISABLED_PROPERTY_NAME, value = "false")
@ConfigurationParameter(
	key = PLUGIN_PROPERTY_NAME,
	value = "pretty," +
			"html:target/test-reports/report.html," +
			"json:target/test-reports/report.json"
)
public class RunCucumberTest {
}
