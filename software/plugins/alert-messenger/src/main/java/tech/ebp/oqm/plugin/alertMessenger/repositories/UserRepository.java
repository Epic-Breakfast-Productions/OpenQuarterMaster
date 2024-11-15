package tech.ebp.oqm.plugin.alertMessenger.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserInfo> {

    public UserInfo findByUserId(String userId) {
        return find("userId", userId).firstResult();
    }

    public UserInfo findByEmail(String email) {
        return find("email", email).firstResult();
    }
}