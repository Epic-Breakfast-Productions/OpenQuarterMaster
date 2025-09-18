package tech.ebp.oqm.lib.core.api.java.testUtils;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.ebp.oqm.lib.core.api.java.OqmCoreApiClient;
import tech.ebp.oqm.lib.core.api.java.config.CoreApiConfig;

import java.net.URI;
import java.net.URISyntaxException;

@Getter
public abstract class RunningServerTest {
	
}
