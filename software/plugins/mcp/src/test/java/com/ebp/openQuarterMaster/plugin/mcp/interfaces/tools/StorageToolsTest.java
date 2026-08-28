package com.ebp.openQuarterMaster.plugin.mcp.interfaces.tools;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.ebp.openQuarterMaster.plugin.mcp.interfaces.tools.StorageTools.TN_GET_NUM_STORAGE_BLOCKS;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StorageToolsTest {


	@Test
	public void testGetNumStorageBlocksBasic(){
		try(
			McpAssured.McpStreamableTestClient client =
				McpAssured.newConnectedStreamableClient();
		) {
			client.when()
				.toolsCall(
					TN_GET_NUM_STORAGE_BLOCKS,
					Map.of(),
					response -> {
						assertFalse(response.isError());
						TextContent content = response.firstContent().asText();
						assertEquals("0", content.text());
					})
				.thenAssertResults();

			//client.disconnect();
		}
	}
}
