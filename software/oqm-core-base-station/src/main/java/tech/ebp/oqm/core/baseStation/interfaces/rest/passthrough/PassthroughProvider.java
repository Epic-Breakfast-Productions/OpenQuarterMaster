package tech.ebp.oqm.core.baseStation.interfaces.rest.passthrough;


import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.baseStation.interfaces.rest.ApiProvider;

@Tags({@Tag(name = "Passthrough")})
public abstract class PassthroughProvider extends ApiProvider {
	public static final String PASSTHROUGH_API_ROOT = API_ROOT + "/passthrough";
}
