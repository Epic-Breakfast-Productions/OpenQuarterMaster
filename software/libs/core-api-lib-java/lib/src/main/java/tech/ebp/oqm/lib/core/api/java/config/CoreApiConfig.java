package tech.ebp.oqm.lib.core.api.java.config;

import lombok.Builder;
import lombok.Data;

import java.net.URI;

@Data
@Builder
public class CoreApiConfig {

	private URI baseUri;
}
