package tech.ebp.oqm.plugin.alertMessenger.preferences;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.plugin.alertMessenger.utils.UserPreferencesNotFoundException;

@ApplicationScoped
public class UserPreferencesService {

    @Inject
    UserPreferencesRepository repository;

    public UserPreferences get(String id) {
        return repository.findByObjectId(id)
            .orElseThrow(() -> new UserPreferencesNotFoundException(id));
    }

    public UserPreferences create(UserPreferences entity) {
        this.repository.persist(entity);
        return entity;
    }

    public UserPreferences update(String id, UserPreferences input) {
        UserPreferences entity = get(id);
        entity.userId = input.userId;
        entity.objectTypes = input.objectTypes;
        entity.eventTypes = input.eventTypes;
        entity.messageChannels = input.messageChannels;
        this.repository.update(entity);
        return entity;
    }

    public void delete(String id) {
        this.repository.delete(get(id));
    }

}
