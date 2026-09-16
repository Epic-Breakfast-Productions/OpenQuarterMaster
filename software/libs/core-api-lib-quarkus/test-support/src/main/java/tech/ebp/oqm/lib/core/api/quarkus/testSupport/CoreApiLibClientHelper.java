package tech.ebp.oqm.lib.core.api.quarkus.testSupport;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import lombok.Getter;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.net.URI;

public abstract class CoreApiLibClientHelper {

	@Getter
	private static final OqmCoreApiClientService coreApiClientService;

	static {
		coreApiClientService = QuarkusRestClientBuilder.newBuilder()
			.baseUri(ConfigProvider.getConfig().getValue("oqm.core.api.baseUri", URI.class))
			.build(OqmCoreApiClientService.class);
	}

}
