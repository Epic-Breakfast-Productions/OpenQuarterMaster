package tech.ebp.oqm.core.baseStation.model.graph;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class GraphRequest {

	@NotNull
    @NotEmpty
	@QueryParam("itemId")
	private List<String> itemId;

	@QueryParam("startDateTime")
	private ZonedDateTime startDateTime;

	@QueryParam("endDateTime")
	private ZonedDateTime endDateTime;
}
