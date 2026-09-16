package tech.ebp.oqm.plugin.alertMessenger.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;
import tech.ebp.oqm.plugin.alertMessenger.repositories.UserRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class UserUtils {

    @Inject
    UserRepository userRepository;

    /**
     * Retrieve a UserInfo object from the database based on the user ID.
     *
     * @param userId the unique user ID
     * @return UserInfo object if found, Optional.empty otherwise
     */
    public Optional<UserInfo> getUserInfo(UUID userId) {
        return Optional.ofNullable(userRepository.findById(userId));
    }

    /**
     * Retrieve the user's unique ID.
     *
     * @param userId the unique user ID
     * @return the user ID, or null if the user is not found
     */
    public String getId(UUID userId) {
        return getUserInfo(userId)
                .map(UserInfo::getUserId)
                .orElse(null);
    }

    /**
     * Retrieve the user's name.
     *
     * @param userId the unique user ID
     * @return the user's name, or null if the user is not found
     */
    public String getName(UUID userId) {
        return getUserInfo(userId)
                .map(UserInfo::getName)
                .orElse(null);
    }

    /**
     * Retrieve the user's email.
     *
     * @param userId the unique user ID
     * @return the user's email, or null if the user is not found
     */
    public String getEmail(UUID userId) {
        return getUserInfo(userId)
                .map(UserInfo::getEmail)
                .orElse(null);
    }

    /**
     * Retrieve the user's username.
     *
     * @param userId the unique user ID
     * @return the user's username, or null if the user is not found
     */
    public String getUserName(UUID userId) {
        return getUserInfo(userId)
                .map(UserInfo::getUsername)
                .orElse(null);
    }

    /**
     * Retrieve the user's roles.
     *
     * @param userId the unique user ID
     * @return a set of roles, or null if the user is not found
     */
    public Set<String> getRoles(UUID userId) {
        return getUserInfo(userId)
                .map(UserInfo::getRoles)
                .orElse(null);
    }
}