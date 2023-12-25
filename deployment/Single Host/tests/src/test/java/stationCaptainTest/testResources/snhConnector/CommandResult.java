package stationCaptainTest.testResources.snhConnector;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.testcontainers.containers.Container;

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
}
