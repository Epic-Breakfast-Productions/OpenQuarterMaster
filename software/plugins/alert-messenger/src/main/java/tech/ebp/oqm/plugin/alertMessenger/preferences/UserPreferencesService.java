package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetailsService;

import java.util.Iterator;
import java.util.List;

@ApplicationScoped
public class UserPreferencesService {

    @Inject
    UserPreferencesRepository repository;

    @Inject
    ConnectionDetailsService connectionDetailsService;

    public List<UserPreferences> get(int pageIndex, int pageSize) {
        PanacheQuery<UserPreferences> result = this.repository.findAllPaged(pageIndex, pageSize);
        return result.list();
    }

    public Iterator<UserPreferences> getIterator() {
        return this.repository.findAll().stream().iterator();
    }

    public UserPreferences get(String userId) {
        return repository.findByObjectId(userId).orElseThrow(() -> new RuntimeException("User preferences not found for userId: " + userId));
    }

    public UserPreferences getByUserId(String userId) {
        return repository.findByUserId(userId).orElseThrow(() -> new RuntimeException("User preferences not found for userId: " + userId));
    }

    public UserPreferences create(UserPreferences entity, String id) {
        entity.userId = id;
        if (entity.connectionDetailIds == null) {
            entity.connectionDetailIds = List.of();
        }
        this.repository.persist(entity);
        return entity;
    }

    public UserPreferences update(UserPreferences request, String id) {
        UserPreferences entity = this.get(id);
        entity.objectTypes = request.objectTypes;
        entity.eventTypes = request.eventTypes;
        entity.connectionDetailIds = request.connectionDetailIds == null ? List.of() : request.connectionDetailIds;
        this.repository.update(entity);
        return entity;
    }

    public List<ConnectionDetails> getConnectionDetails(UserPreferences preferences) {
        return connectionDetailsService.getByIds(preferences.connectionDetailIds == null ? List.of() : preferences.connectionDetailIds);
    }

    public void deleteById(String id) {
        this.repository.delete(this.get(id));
    }
}
