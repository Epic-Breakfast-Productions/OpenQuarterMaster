package stationCaptainTest.testResources.snhConnector;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import stationCaptainTest.testResources.config.snhSetup.ContainerSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.ExistingSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhType;

import java.io.Closeable;
import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class ContainerSnhConnector extends SnhConnector {
	
	//TODO:: container resources
	
	
	@Override
	public SnhType getType() {
		return SnhType.CONTAINER;
	}
	
	@Override
	public void close() throws IOException {
	
	}
}
