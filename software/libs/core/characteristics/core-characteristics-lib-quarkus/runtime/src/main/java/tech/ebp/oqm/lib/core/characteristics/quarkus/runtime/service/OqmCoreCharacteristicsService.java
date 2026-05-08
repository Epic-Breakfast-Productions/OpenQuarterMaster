package tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.OqmCoreCharacteristicsRestClient;
import tech.ebp.oqm.lib.core.characteristics.quarkus.runtime.restClient.model.AllInfo;

@Named("OqmCoreCharacteristicsService")
@ApplicationScoped
public class OqmCoreCharacteristicsService {
	
	@RestClient
	OqmCoreCharacteristicsRestClient oqmCoreCharacteristicsClient;
	
	public Uni<ObjectNode> health() {
		return this.oqmCoreCharacteristicsClient.health();
	}
	
	public Uni<AllInfo> allInfo() {
		return this.oqmCoreCharacteristicsClient.allInfo();
	}
}
