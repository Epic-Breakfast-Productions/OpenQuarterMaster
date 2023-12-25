package stationCaptainTest.testResources.snhConnector;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import stationCaptainTest.testResources.config.ConfigReader;
import stationCaptainTest.testResources.config.snhSetup.ContainerSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.ExistingSnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhSetupConfig;
import stationCaptainTest.testResources.config.snhSetup.SnhType;

import java.io.Closeable;
import java.io.IOException;

@Slf4j
@Data
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ContainerSnhSetupConfig.class, name = "CONTAINER"),
	@JsonSubTypes.Type(value = ExistingSnhSetupConfig.class, name = "EXISTING"),
})
public abstract class SnhConnector<C extends SnhSetupConfig> implements Closeable {
	
	protected C getSetupConfig(){
		try {
			//noinspection unchecked
			return (C) ConfigReader.getTestRunConfig().getSetupConfig();
		}catch(ClassCastException e){
			log.error("FAILED to cast config as appropriate: ", e);
			throw new RuntimeException("FAILED to cast config as appropriate.", e);
		}
	}
	
	public abstract void init();
	
	public abstract SnhType getType();
	
	@Override
	public void close() throws IOException {}
}
