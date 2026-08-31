package tech.ebp.oqm.core.baseStation.model.graph;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class GraphRequest {

	@NotNull
    @NotEmpty
    @UniqueElements
	@QueryParam("itemId")
	private List<String> itemId;

	@QueryParam("startDateTime")
	private ZonedDateTime startDateTime;

	@QueryParam("endDateTime")
	private ZonedDateTime endDateTime;
}
