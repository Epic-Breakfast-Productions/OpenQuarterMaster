package tech.ebp.oqm.plugin.alertMessenger.connections;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConnectionDetailsRepository implements PanacheMongoRepository<ConnectionDetails> {

    public List<ConnectionDetails> findAllByIds(List<ObjectId> ids) {
        return find("_id in ?1", ids).list();
    }

    public Optional<ConnectionDetails> findByObjectId(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }
        return findByIdOptional(new ObjectId(id));
    }

    public PanacheQuery<ConnectionDetails> findAllPaged(String userId, int pageIndex, int pageSize) {
        return find("userId", userId).page(Page.of(pageIndex, pageSize));
    }

    public PanacheQuery<ConnectionDetails> findAllPaged(int pageIndex, int pageSize) {
        return findAll().page(Page.of(pageIndex, pageSize));
    }
}
