package tech.ebp.oqm.plugin.alertMessenger.connections;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class ConnectionDetailsService {

    @Inject
    ConnectionDetailsRepository repository;

    public List<ConnectionDetails> getForUser(String userId, int pageIndex, int pageSize) {
        return repository.findAllPaged(userId, pageIndex, pageSize).list();
    }

    public List<ConnectionDetails> getAll(int pageIndex, int pageSize) {
        return repository.findAllPaged(pageIndex, pageSize).list();
    }

    public List<ConnectionDetails> getByIds(List<ObjectId> ids) {
        return repository.findAllByIds(ids);
    }

    public ConnectionDetails get(String id, String userId) {
        return repository.findByObjectId(id)
            .filter(details -> userId.equals(details.getUserId()))
            .orElseThrow(() -> new RuntimeException("Connection details not found: " + id));
    }

    public ConnectionDetails create(ConnectionDetails details, String userId) {
        details.setUserId(userId);
        repository.persist(details);
        return details;
    }

    public ConnectionDetails update(String id, ConnectionDetails details, String userId) {
        ConnectionDetails existing = get(id, userId);
        details.id = existing.id;
        details.setUserId(existing.getUserId());
        repository.update(details);
        return details;
    }

    public void delete(String id, String userId) {
        repository.delete(get(id, userId));
    }

    public void updateLastError(ConnectionDetails details, String lastError) {
        repository.updateLastError(details.id, lastError);
    }
}
