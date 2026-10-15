package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetailsService;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class UserPreferencesService {

    @Inject
    UserPreferencesRepository repository;

    @Inject
    ConnectionDetailsService connectionDetailsService;

    public List<UserPreferences> get(String userId, boolean admin, int pageIndex, int pageSize) {
        PanacheQuery<UserPreferences> result = admin
            ? this.repository.findAllPaged(pageIndex, pageSize)
            : this.repository.findAllPaged(userId, pageIndex, pageSize);
        return result.list();
    }

    public Iterator<UserPreferences> getIterator() {
        return this.repository.findAll().stream().iterator();
    }

    public UserPreferences get(String id, String userId, boolean admin) {
        return repository.findByObjectId(id)
            .filter(preferences -> admin || userId.equals(preferences.userId))
            .orElseThrow(() -> new RuntimeException("User preferences not found"));
    }

    public List<UserPreferences> getByUserId(String userId, String requesterId, boolean admin) {
        String ownerId = admin ? userId : requesterId;
        return repository.findByUserId(ownerId);
    }

    public UserPreferences create(UserPreferences entity, String id) {
        entity.userId = id;
        if (entity.connectionDetailIds == null) {
            entity.connectionDetailIds = List.of();
        }
        validateConnectionOwnership(entity.connectionDetailIds, id, false);
        this.repository.persist(entity);
        return entity;
    }

    public UserPreferences update(UserPreferences request, String id, String userId, boolean admin) {
        UserPreferences entity = this.get(id, userId, admin);
        entity.objectTypes = request.objectTypes;
        entity.eventTypes = request.eventTypes;
        entity.connectionDetailIds = request.connectionDetailIds == null ? List.of() : request.connectionDetailIds;
        validateConnectionOwnership(entity.connectionDetailIds, userId, admin);
        this.repository.update(entity);
        return entity;
    }

    public List<ConnectionDetails> getConnectionDetails(UserPreferences preferences) {
        return connectionDetailsService.getByIds(preferences.connectionDetailIds == null ? List.of() : preferences.connectionDetailIds);
    }

    public void deleteById(String id, String userId, boolean admin) {
        this.repository.delete(this.get(id, userId, admin));
    }

    private void validateConnectionOwnership(List<org.bson.types.ObjectId> connectionIds, String userId, boolean admin) {
        if (admin || connectionIds.isEmpty()) {
            return;
        }
        List<ConnectionDetails> connections = connectionDetailsService.getByIds(connectionIds);
        if (connections.size() != connectionIds.size()
            || connections.stream().anyMatch(connection -> !Objects.equals(userId, connection.getUserId()))) {
            throw new IllegalArgumentException("Connection details must belong to the current user");
        }
    }
}
