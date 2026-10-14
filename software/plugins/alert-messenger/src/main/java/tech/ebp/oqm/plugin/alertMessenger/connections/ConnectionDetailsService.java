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

    public List<ConnectionDetails> getAll(int pageIndex, int pageSize) {
        PanacheQuery<ConnectionDetails> result = repository.findAllPaged(pageIndex, pageSize);
        return result.list();
    }

    public List<ConnectionDetails> getByIds(List<ObjectId> ids) {
        return repository.findAllByIds(ids);
    }

    public ConnectionDetails get(String id) {
        return repository.findByObjectId(id)
            .orElseThrow(() -> new RuntimeException("Connection details not found: " + id));
    }

    public ConnectionDetails create(ConnectionDetails details, String userId) {
        details.setUserId(userId);
        repository.persist(details);
        return details;
    }

    public ConnectionDetails update(String id, ConnectionDetails details) {
        ConnectionDetails existing = get(id);
        details.id = existing.id;
        details.setUserId(existing.getUserId());
        repository.update(details);
        return details;
    }

    public void delete(String id) {
        repository.delete(get(id));
    }
}
