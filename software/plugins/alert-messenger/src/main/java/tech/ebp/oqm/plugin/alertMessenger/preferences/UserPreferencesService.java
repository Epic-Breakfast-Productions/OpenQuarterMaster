package tech.ebp.oqm.plugin.alertMessenger.preferences;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetailsService;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class UserPreferencesService {

    @Inject
    UserPreferencesRepository repository;

    @Inject
    ConnectionDetailsService connectionDetailsService;

    public List<UserPreferences> getForUser(String userId, int pageIndex, int pageSize) {
        return this.repository.findAllPagedByUserId(userId, pageIndex, pageSize).list();
    }

    public List<UserPreferences> getAll(int pageIndex, int pageSize) {
        return this.repository.findAllPaged(pageIndex, pageSize).list();
    }

    public Iterator<UserPreferences> getForUser(String userId) {
        return this.repository.findAllByUserId(userId).stream().iterator();
    }

    public Iterator<UserPreferences> getIterator() {
        return this.repository.findAll().stream().iterator();
    }

    public UserPreferences get(String id, String userId) {
        return repository.findByObjectId(id)
            .filter(preferences -> userId.equals(preferences.userId))
            .orElseThrow(() -> new RuntimeException("User preferences not found"));
    }

    public UserPreferences create(UserPreferences entity, String id) {
        entity.userId = id;
        if (entity.connectionDetailIds == null) {
            throw new IllegalArgumentException("Connection detail IDs cannot be null");
        }
        this.validateThrottleTime(entity.throttleTime);

        validateConnectionOwnership(entity.connectionDetailIds, id);
        this.repository.persist(entity);
        return entity;
    }

    public UserPreferences update(UserPreferences request, String id, String userId) {
        this.validateThrottleTime(request.throttleTime);

        UserPreferences entity = this.get(id, userId);
        entity.name = request.name;
        entity.description = request.description;
        entity.enabled = request.enabled;
        entity.databaseId = request.databaseId;
        entity.throttleTime = request.throttleTime;
        entity.objectTypes = request.objectTypes;
        entity.eventTypes = request.eventTypes;

        if (request.connectionDetailIds == null) {
            throw new IllegalArgumentException("Connection detail IDs cannot be null");
        }

        entity.connectionDetailIds = request.connectionDetailIds;
        validateConnectionOwnership(entity.connectionDetailIds, userId);
        this.repository.update(entity);
        return entity;
    }

    public List<ConnectionDetails> getConnectionDetails(UserPreferences preferences) {
        return connectionDetailsService.getByIds(preferences.connectionDetailIds == null ? List.of() : preferences.connectionDetailIds);
    }

    public void deleteById(String id, String userId) {
        this.repository.delete(this.get(id, userId));
    }

    private void validateConnectionOwnership(List<ObjectId> connectionIds, String userId) {
        if (connectionIds.isEmpty()) {
            return;
        }
        List<ConnectionDetails> connections = connectionDetailsService.getByIds(connectionIds);
        if (connections.size() != connectionIds.size()
            || connections.stream().anyMatch(connection -> !Objects.equals(userId, connection.getUserId()))) {
            throw new IllegalArgumentException("Connection details must belong to the current user");
        }
    }

    private void validateThrottleTime(Duration throttleTime) {
        if(!throttleTime.isNegative() && throttleTime.compareTo(Duration.ofDays(30)) <= 0) {
            throw new IllegalArgumentException("Throttle time must be between 0 and 30 days");
        }
    }
}
