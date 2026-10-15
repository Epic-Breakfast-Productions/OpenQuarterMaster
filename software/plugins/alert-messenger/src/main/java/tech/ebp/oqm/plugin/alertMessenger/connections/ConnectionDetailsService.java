package tech.ebp.oqm.plugin.alertMessenger.connections;

import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class ConnectionDetailsService {

    @Inject
    ConnectionDetailsRepository repository;

    public List<ConnectionDetails> getAll(String userId, boolean admin, int pageIndex, int pageSize) {
        PanacheQuery<ConnectionDetails> result = admin
            ? repository.findAllPaged(pageIndex, pageSize)
            : repository.findAllPaged(userId, pageIndex, pageSize);
        return result.list();
    }

    public List<ConnectionDetails> getByIds(List<ObjectId> ids) {
        return repository.findAllByIds(ids);
    }

    public ConnectionDetails get(String id, String userId, boolean admin) {
        return repository.findByObjectId(id)
            .filter(details -> admin || userId.equals(details.getUserId()))
            .orElseThrow(() -> new RuntimeException("Connection details not found: " + id));
    }

    public ConnectionDetails create(ConnectionDetails details, String userId) {
        details.setUserId(userId);
        repository.persist(details);
        return details;
    }

    public ConnectionDetails update(String id, ConnectionDetails details, String userId, boolean admin) {
        ConnectionDetails existing = get(id, userId, admin);
        details.id = existing.id;
        details.setUserId(existing.getUserId());
        repository.update(details);
        return details;
    }

    public void delete(String id, String userId, boolean admin) {
        repository.delete(get(id, userId, admin));
    }

    public void updateLastError(ConnectionDetails details, String lastError) {
        details.setLastError(lastError);
        repository.update(details);
    }
}
