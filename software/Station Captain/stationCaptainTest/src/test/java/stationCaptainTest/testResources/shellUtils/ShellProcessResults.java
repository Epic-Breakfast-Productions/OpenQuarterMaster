package stationCaptainTest.testResources.shellUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShellProcessResults {
	private int exitCode;
	private String stdOut;
	private String errOut;
	
	
	public static ShellProcessResults.ShellProcessResultsBuilder builderFomProcess(Process p) throws IOException, InterruptedException {
		ShellProcessResults.ShellProcessResultsBuilder builder = ShellProcessResults.builder();
		
		builder.exitCode(p.waitFor());
		builder.stdOut(IOUtils.toString(p.getInputStream(), Charset.defaultCharset()));
		builder.errOut(IOUtils.toString(p.getErrorStream(), Charset.defaultCharset()));
		
		return builder;
	}
	public static ShellProcessResults.ShellProcessResultsBuilder builderFromProcessBuilder(ProcessBuilder builder) throws IOException, InterruptedException {
		return builderFomProcess(builder.start());
	}
	
}
