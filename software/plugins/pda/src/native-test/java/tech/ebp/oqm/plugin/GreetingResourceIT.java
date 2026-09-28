package tech.ebp.oqm.plugin;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import tech.ebp.oqm.plugin.pda.GreetingResourceTest;

@QuarkusIntegrationTest
class GreetingResourceIT extends GreetingResourceTest {
    // Execute the same tests but in packaged mode.
}
