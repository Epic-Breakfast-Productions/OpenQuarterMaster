package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class ImageSearch extends FileSearchObject {
	@QueryParam("title") String imageTitle;
	@QueryParam("source") String imageSource;
	
}
