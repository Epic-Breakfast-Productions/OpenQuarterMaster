package tech.ebp.oqm.plugin.storagotchi.interfaces.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
@Path("/api/usersettings")
public class UserSettings {
	
	
	
	@PUT
	public Response updateUserSettings(UserSettings userSettings) {
		
		
		
		
		
		log.info("UserSettings updated");
		return Response.ok().build();
	}
	
}
