package stationCaptainTest.scenarioUtils;

import io.cucumber.java.Scenario;
import org.apache.tika.Tika;
import org.testcontainers.containers.Container;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;

import java.net.http.HttpResponse;

public final class AttachUtils {
	private static final Tika TIKA = new Tika();
	
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
	
	public static void attach(HttpResponse<String> response, String title, Scenario scenario) {
		scenario.attach(response.statusCode()+"", "text/plain", title + " - status");
		scenario.attach(response.headers().toString(), "text/plain", title + " - headers");
		
		String mimeType = response.headers().firstValue("content-type").get();
		mimeType = mimeType.replace(" ", "");
		mimeType = mimeType.split(";")[0];

		scenario.attach(response.body(), mimeType, title + " - body - " + mimeType);
	}
}
