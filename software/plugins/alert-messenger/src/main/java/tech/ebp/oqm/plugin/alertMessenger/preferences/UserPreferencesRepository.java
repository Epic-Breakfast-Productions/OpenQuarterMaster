package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserPreferencesRepository implements PanacheMongoRepository<UserPreferences> {

    public Optional<UserPreferences> findByObjectId(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }
        return findByIdOptional(new ObjectId(id));
    }

    public List<UserPreferences> findByUserId(String userId) {
        return find("userId", userId).list();
    }

    public PanacheQuery<UserPreferences> findAllPaged(String userId, int pageIndex, int pageSize) {
        return find("userId", userId).page(Page.of(pageIndex, pageSize));
    }

    public PanacheQuery<UserPreferences> findAllPaged(int pageIndex, int pageSize) {
        return findAll().page(Page.of(pageIndex, pageSize));
    }

    public List<UserPreferences> findAllByUserId(String userId) {
        return find("userId", userId).list();
    }
}
