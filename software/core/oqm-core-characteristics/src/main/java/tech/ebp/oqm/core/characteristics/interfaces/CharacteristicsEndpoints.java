package tech.ebp.oqm.core.characteristics.interfaces;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.ebp.oqm.core.characteristics.model.characteristics.Characteristics;
import tech.ebp.oqm.core.characteristics.services.CharacteristicsService;

import static tech.ebp.oqm.core.characteristics.interfaces.RestInterface.BASE_PATH;

@Path(BASE_PATH + "/characteristics")
public class CharacteristicsEndpoints extends RestInterface {
	
	@Inject
	CharacteristicsService characteristicsService;
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Characteristics getCharacteristics() {
		return this.characteristicsService.getCharacteristics();
	}
	
	//TODO:: logo/banner endpoints
}
