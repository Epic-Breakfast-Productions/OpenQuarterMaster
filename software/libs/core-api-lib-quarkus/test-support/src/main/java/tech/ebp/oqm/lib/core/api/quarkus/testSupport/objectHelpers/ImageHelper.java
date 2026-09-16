package tech.ebp.oqm.lib.core.api.quarkus.testSupport.objectHelpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.files.FileUploadBody;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibClientHelper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageHelper extends CoreApiLibClientHelper {

	public static ObjectNode newImage(
		String auth,
		String oqmDbIdOrName,
		FileUploadBody upload
	){
		return getCoreApiClientService()
				   .imageAdd(
					   auth,
					   oqmDbIdOrName,
					   upload
				   )
				   .await().indefinitely();
	}
}
