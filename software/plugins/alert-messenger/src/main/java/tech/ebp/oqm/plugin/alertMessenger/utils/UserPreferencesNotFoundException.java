package tech.ebp.oqm.plugin.alertMessenger.utils;

public class UserPreferencesNotFoundException extends RuntimeException {

    public UserPreferencesNotFoundException(String id) {
        super("User preferences not found for id: " + id);
    }
}
