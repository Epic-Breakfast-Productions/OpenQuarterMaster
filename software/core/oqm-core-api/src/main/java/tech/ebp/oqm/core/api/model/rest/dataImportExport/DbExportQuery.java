package tech.ebp.oqm.core.api.model.rest.dataImportExport;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class DbExportQuery {
	@QueryParam("dbs")
	Set<String> databases = new HashSet<>();
	
	@QueryParam("includeHistory") @DefaultValue("true") boolean includeHistory = true;
}
