package tech.ebp.oqm.core.api.model.rest.dataImportExport;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;
import tech.ebp.oqm.core.api.service.importExport.importing.options.DataImportOptions;

import java.io.InputStream;

public class ImportBundleFileBody {
	
	@FormParam("file")
	@PartType(MediaType.APPLICATION_OCTET_STREAM)
	public InputStream file;

	@FormParam("fileName")
	@PartType(MediaType.TEXT_PLAIN)
	public String fileName;

	@FormParam("options")
	@PartType(MediaType.APPLICATION_JSON)
	public DataImportOptions options;
}
