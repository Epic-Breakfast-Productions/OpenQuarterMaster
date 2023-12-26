package stationCaptainTest.testResources.snhConnector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.testcontainers.containers.Container;
import stationCaptainTest.testResources.shellUtils.ShellProcessResults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter(AccessLevel.PROTECTED)
public class CommandResult {
	
	private String stdOut;
	private String stdErr;
	private int returnCode;
	
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
}
