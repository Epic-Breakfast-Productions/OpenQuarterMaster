package tech.ebp.oqm.plugin.storagotchi.model.usersettings;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserSettingsService implements PanacheRepository<UserSettings> {
	
	
	public UserSettings getByIdOrDefault(String id) {
		return this.find("id", id)
				   .firstResultOptional()
				   .orElse(
					   UserSettings.builder().id(id).build()
				   );
	}
	
	public UserSettings addOrUpdateFromRequest(String id, ObjectNode updates){
		UserSettings settings = this.getByIdOrDefault(id);
		
		settings.setVolume(updates.get("volume").asDouble());
		settings.setMusic(updates.get("music").asBoolean());
		settings.setSoundfx(updates.get("soundfx").asBoolean());
		
		settings.setStoragotchiName(updates.get("storagotchiName").asText());
		
		this.persist(settings);
		
		return settings;
	}
	
}
