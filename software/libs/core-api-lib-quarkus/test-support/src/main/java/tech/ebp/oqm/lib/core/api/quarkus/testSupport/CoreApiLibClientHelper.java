package tech.ebp.oqm.lib.core.api.quarkus.testSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.datafaker.Faker;
import org.eclipse.microprofile.config.ConfigProvider;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.net.URI;
import java.util.Random;
import java.util.random.RandomGenerator;

public abstract class CoreApiLibClientHelper {

	@Setter(AccessLevel.PROTECTED)
	@Getter(AccessLevel.PROTECTED)
	private static ObjectMapper objectMapper = new ObjectMapper();

	@Getter(AccessLevel.PROTECTED)
	private static final OqmCoreApiClientService coreApiClientService = null;

	@Getter(AccessLevel.PROTECTED)
	private static final Faker faker = new Faker();

	@Getter(AccessLevel.PROTECTED)
	private static final RandomGenerator rand = RandomGenerator.getDefault();


	static {
//		coreApiClientService = QuarkusRestClientBuilder.newBuilder()
//			.baseUri(ConfigProvider.getConfig().getValue("oqm.core.api.baseUri", URI.class))
//			.build(OqmCoreApiClientService.class);
	}

}
