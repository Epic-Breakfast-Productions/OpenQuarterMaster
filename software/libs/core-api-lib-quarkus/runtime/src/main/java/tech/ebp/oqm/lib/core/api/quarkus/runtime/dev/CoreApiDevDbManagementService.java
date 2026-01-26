package tech.ebp.oqm.lib.core.api.quarkus.runtime.dev;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;

@Slf4j
@IfBuildProfile(anyOf = {"dev", "test"})
@ApplicationScoped
public class CoreApiDevDbManagementService {
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClient;
	
	@Inject
	KcClientAuthService serviceAccountService;
	
	public String resetDb() {
		log.info("Resetting all OQM DB's.");
		
		this.oqmCoreApiClient.manageDbClearAll(this.serviceAccountService.getAuthString()).await().indefinitely();
		
		log.info("DONE resetting all OQM DB's.");
		
		return "OK";
	}
	
	public String resetAndPopulateDb(String db) {
		String resetResult = this.resetDb();
		
		log.info("Populating OQM DB: {}", db);
		//TODO:: populate from files
		
		log.info("DONE populating OQM DB: {}", db);
		return "OK";
	}
	
}
