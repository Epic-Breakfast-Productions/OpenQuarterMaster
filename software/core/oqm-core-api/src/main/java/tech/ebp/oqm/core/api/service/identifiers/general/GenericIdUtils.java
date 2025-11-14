package tech.ebp.oqm.core.api.service.identifiers.general;

import lombok.NonNull;

public class GenericIdUtils {
	
	public static boolean isValidGenericId(@NonNull String id) {
		return !id.isBlank();
	}
	
}
