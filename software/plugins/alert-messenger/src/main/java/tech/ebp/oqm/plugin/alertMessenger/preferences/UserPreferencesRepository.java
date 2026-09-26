package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.Optional;

@ApplicationScoped
public class UserPreferencesRepository implements PanacheMongoRepository<UserPreferences> {

    public Optional<UserPreferences> findByObjectId(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }
        return findByIdOptional(new ObjectId(id));
    }

    public PanacheQuery<UserPreferences> findAllPaged(int pageIndex, int pageSize) {
        return findAll().page(Page.of(pageIndex, pageSize));
    }
}
