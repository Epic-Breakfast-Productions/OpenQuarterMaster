package tech.ebp.oqm.plugin.alertMessenger.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.plugin.alertMessenger.model.UserPreferences;
import java.util.UUID;

@ApplicationScoped
public class UserPreferencesRepository implements PanacheRepositoryBase<UserPreferences, UUID> {

    /**
     * Find preferences by user ID.
     *
     * @param id the unique user ID
     * @return the UserPreferences object, or null if not found
     */
    public UserPreferences findById(UUID id) {
        return find("id", id).firstResult();
    }
}
