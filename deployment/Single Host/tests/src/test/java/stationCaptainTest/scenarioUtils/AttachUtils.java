package stationCaptainTest.scenarioUtils;

import io.cucumber.java.Scenario;
import org.testcontainers.containers.Container;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;
import stationCaptainTest.testResources.snhConnector.CommandResult;

public final class AttachUtils {
	public static void attach(ShellProcessResults results, String title, Scenario scenario){
		scenario.attach(results.getExitCode() + "", "text/plain", title + "; Exit code from running command.");
		scenario.attach(results.getStdOut(), "text/plain", title + "Std out from command");
		scenario.attach(results.getErrOut(), "text/plain", title + "Error out from command");
	}
	
	public static void attach(Container.ExecResult results, String title, Scenario scenario){
		scenario.attach(results.getExitCode() + "", "text/plain", title + "; Exit code from command.");
		scenario.attach(results.getStdout(), "text/plain", title + "; Std out from command");
		scenario.attach(results.getStderr(), "text/plain", title + "; Error out from command");
	}
	
	public static void attach(CommandResult results, String title, Scenario scenario){
		scenario.attach(results.getReturnCode() + "", "text/plain", title + "; Exit code from command.");
		scenario.attach(results.getStdOut(), "text/plain", title + "; Std out from command");
		scenario.attach(results.getStdErr(), "text/plain", title + "; Error out from command");
	}
}
