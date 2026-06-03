package tech.ebp.oqm.core.api.health;

import lombok.Getter;

@Getter
public abstract class StatusProviderService implements StatusProvider {
    private final String name;
    volatile boolean isReady = false;
    public volatile String statusMessage = "Status not set";

    public StatusProviderService(String name) {
        this.name = name;
    }

    public void markStarted(String message) {
        statusMessage = message;
    }

    public void markCompleted(String message) {
        isReady = true;
        statusMessage = message;
    }

    public void markFailed(String message) {
        isReady = false;
        statusMessage = message;
    }
}
