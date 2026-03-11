package tech.ebp.oqm.lib.core.api.java.testUtils.testClases;

import lombok.Getter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import tech.ebp.oqm.lib.core.api.java.auth.OqmCredentials;
import tech.ebp.oqm.lib.core.api.java.testUtils.JwtUtils;

public class JwtAuthTest extends RunningServerTest {
	
	@Getter
	OqmCredentials credentials = JwtUtils.generateJwtCreds(true);
	
	public static void setupAndStart() {
		coreApiContainer.setupForPlainJwtAuth();
		startContainers();
	}
	
	@AfterAll
	public static void afterAll() {
		stopContainers();
	}
}
