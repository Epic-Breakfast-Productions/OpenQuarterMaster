package tech.ebp.oqm.lib.core.api.quarkus.testSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import lombok.Getter;
import lombok.Setter;
import net.datafaker.Faker;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.net.URI;

public abstract class CoreApiLibClientHelper {

	@Setter
	@Getter
	private static ObjectMapper objectMapper = new ObjectMapper();

	@Getter
	private static final OqmCoreApiClientService coreApiClientService = null;

	@Getter
	private static final Faker faker = new Faker();


	static {
//		coreApiClientService = QuarkusRestClientBuilder.newBuilder()
//			.baseUri(ConfigProvider.getConfig().getValue("oqm.core.api.baseUri", URI.class))
//			.build(OqmCoreApiClientService.class);
	}

}
