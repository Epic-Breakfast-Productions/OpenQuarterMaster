package stationCaptainTest.testResources.snhConnector;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import stationCaptainTest.testResources.config.snhSetup.SnhType;

import java.io.IOException;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@NoArgsConstructor
public class ExistingSnhConnector extends SnhConnector {
	
	//TODO:: container resources
	
	
	@Override
	public SnhType getType() {
		return SnhType.EXISTING;
	}
	
	@Override
	public void close() throws IOException {
	
	}
}
