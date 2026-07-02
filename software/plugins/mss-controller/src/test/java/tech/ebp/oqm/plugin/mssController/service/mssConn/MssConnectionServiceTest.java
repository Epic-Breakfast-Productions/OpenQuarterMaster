package tech.ebp.oqm.plugin.mssController.service.mssConn;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.commands.GetModuleStateCommand;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.command.response.CommandResponse;
import tech.ebp.oqm.plugin.mssController.service.mssConn.serial.SerialMssConnector;
import tech.ebp.oqm.plugin.mssController.testResources.modules.TestModuleResource;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(
	value = TestModuleResource.class,
	restrictToAnnotatedClass = true,
	initArgs = {
		@ResourceArg(name = TestModuleResource.NUM_SERIAL_MODULE_RES_NAME, value = "1")
	}
)
class MssConnectionServiceTest {

	@Inject
	MssConnectionService mssConnectionService;

	@Test
	public void testGetSerialModule() throws Exception {
		assertEquals(1, this.mssConnectionService.getConnectors().size());

		MssConnector connector = this.mssConnectionService.getConnectors().get(0);

		assertEquals(SerialMssConnector.class, connector.getClass());

		CommandResponse response = connector.sendCommand(new GetModuleStateCommand());

		log.info("Response: {}", response);
	}
}
