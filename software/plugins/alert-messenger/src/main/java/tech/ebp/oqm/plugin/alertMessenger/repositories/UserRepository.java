package tech.ebp.oqm.plugin.alertMessenger.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserInfo> {

    /**
     * Find a user by their unique ID.
     *
     * @param id the unique user ID
     * @return the UserInfo object, or null if not found
     */
    public UserInfo findById(UUID id) {
        return find("id", id).firstResult();
    }

    /**
     * Find a user by their email.
     *
     * @param email the user's email address
     * @return the UserInfo object, or null if not found
     */
    public UserInfo findByEmail(String email) {
        return find("email", email).firstResult();
    }

    @Inject
    EntityManager entityManager; // Inject EntityManager for persistence operations

    public UserInfo merge(UserInfo userInfo) {
        return entityManager.merge(userInfo); // Use EntityManager to merge detached entities
    }
}