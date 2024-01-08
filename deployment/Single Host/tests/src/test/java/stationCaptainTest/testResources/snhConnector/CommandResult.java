package stationCaptainTest.testResources.snhConnector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.testcontainers.containers.Container;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;

import java.io.IOException;
import java.nio.charset.Charset;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter(AccessLevel.PROTECTED)
public class CommandResult {
	
	private String stdOut;
	private String stdErr;
	private int returnCode;
	
	public CommandResult assertSuccess(String commandPurpose){
		if(this.returnCode != 0){
			throw new RuntimeException("Failed to run command to " + commandPurpose + ". Exit code: " + this.returnCode + " Stdout: " + this.stdOut + " / StdErr: " + this.stdErr);
		}
		return this;
	}
	
	public static CommandResult from(Container.ExecResult execResult) {
		return CommandResult.builder()
				   .stdOut(execResult.getStdout())
				   .stdErr(execResult.getStderr())
				   .returnCode(execResult.getExitCode())
				   .build();
	}
	
	public static CommandResult from(ShellProcessResults results){
		return CommandResult.builder()
				   .stdOut(results.getStdOut())
				   .stdErr(results.getErrOut())
				   .returnCode(results.getExitCode())
				   .build();
	}
	
	public static CommandResult from(Process process) throws IOException, InterruptedException {
		CommandResult.CommandResultBuilder builder = CommandResult.builder();
		
		builder.returnCode(process.waitFor());
		builder.stdOut(IOUtils.toString(process.getInputStream(), Charset.defaultCharset()));
		builder.stdErr(IOUtils.toString(process.getErrorStream(), Charset.defaultCharset()));
		
		return builder.build();
	}
	public static CommandResult from(ProcessBuilder processBuilder) throws IOException, InterruptedException {
		return from(processBuilder.start());
	}
}
