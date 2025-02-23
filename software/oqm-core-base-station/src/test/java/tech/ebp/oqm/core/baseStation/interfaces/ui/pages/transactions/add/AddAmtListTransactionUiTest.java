package tech.ebp.oqm.core.baseStation.interfaces.ui.pages.transactions.add;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.baseStation.testResources.testClasses.WebUiTest;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MainAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.assertions.MessageAssertions;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.ItemsPage;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.ItemsUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.StorageBlockUiUtils;
import tech.ebp.oqm.core.baseStation.testResources.ui.utilities.transaction.AddTransactionUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
public class AddAmtListTransactionUiTest extends WebUiTest {

	
	//TODO:: new amount list, amount
	//TODO:: new amount list, whole
	
	//TODO:: existing amount list, amount
	//TODO:: existing amount list, amount to existing
	//TODO:: existing amount list, whole
	
}
