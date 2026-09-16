package stationCaptainTest.testResources.threads;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data
@Builder
public class PerformanceTestResult {
	private int threadNum;
	private long numCalls;
	private long numErrors;
	private Duration overallDuration;
	
	public Duration getAverageTimePerCall(){
		return this.getOverallDuration().dividedBy(numCalls);
	}
}
