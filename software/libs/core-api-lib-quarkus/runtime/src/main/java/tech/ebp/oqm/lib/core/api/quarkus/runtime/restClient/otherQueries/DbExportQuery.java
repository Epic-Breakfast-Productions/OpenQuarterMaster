package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.otherQueries;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class DbExportQuery {
	@QueryParam("dbs")
	Set<String> databases = new HashSet<>();
	
	@QueryParam("includeHistory") @DefaultValue("true") boolean includeHistory = true;
}
