package tech.ebp.oqm.core.baseStation.model.graph;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class GraphRequest {

    @QueryParam("dbIdOrName")
    private String dbIdOrName;

    @QueryParam("itemId")
    private String itemId;

    @QueryParam("startDateTime")
    private OffsetDateTime startDateTime;

    @QueryParam("endDateTime")
    private OffsetDateTime endDateTime;
}
