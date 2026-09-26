package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.mongodb.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.plugin.alertMessenger.utils.JwtUtils;

import java.util.Iterator;
import java.util.List;

@ApplicationScoped
public class UserPreferencesService {

    @Inject
    UserPreferencesRepository repository;

    public List<UserPreferences> get(int pageIndex, int pageSize) {
        PanacheQuery<UserPreferences> result = this.repository.findAllPaged(pageIndex, pageSize);
        return result.list();
    }

    public Iterator<UserPreferences> getIterator() {
        return this.repository.findAll().stream().iterator();
    }

    public UserPreferences get(String id) {
        return repository.findByObjectId(id)
            .orElseThrow(() -> new RuntimeException("User preferences not found for id: " + id));
    }

    public UserPreferences create(UserPreferences entity, JsonWebToken jwt) {
        entity.userId = JwtUtils.getId(jwt);
        this.repository.persist(entity);
        return entity;
    }

    public UserPreferences update(UserPreferences request, JsonWebToken jwt) {
        UserPreferences entity = this.get(JwtUtils.getId(jwt));
        entity.objectTypes = request.objectTypes;
        entity.eventTypes = request.eventTypes;
        entity.messageChannels = request.messageChannels;
        this.repository.update(entity);
        return entity;
    }

    public void delete(JsonWebToken jwt) {
        String id = JwtUtils.getId(jwt);
        this.repository.delete(this.get(id));
    }
}
