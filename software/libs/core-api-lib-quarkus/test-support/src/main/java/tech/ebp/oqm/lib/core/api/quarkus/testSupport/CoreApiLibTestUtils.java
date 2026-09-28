package tech.ebp.oqm.lib.core.api.quarkus.testSupport;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.ConfigProvider;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoreApiLibTestUtils {


	public static String getCoreApiBaseUri() {
		return ConfigProvider.getConfig().getValue("oqm.core.api.baseUri", String.class);
	}

}
