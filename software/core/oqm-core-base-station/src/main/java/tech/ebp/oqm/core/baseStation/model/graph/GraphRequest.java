package tech.ebp.oqm.core.baseStation.model.graph;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
public class GraphRequest {

	@NotNull
	@QueryParam("itemId")
	private String itemId;

	@QueryParam("startDateTime")
	private ZonedDateTime startDateTime;

	@QueryParam("endDateTime")
	private ZonedDateTime endDateTime;
}
