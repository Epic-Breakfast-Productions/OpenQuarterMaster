package tech.ebp.oqm.core.api.health;

import lombok.Getter;

@Getter
public class HealthStatus {
    private final String name;
    private volatile boolean up = false;
    private volatile String statusMessage = "Status not set";

    public HealthStatus(String name) {
        this.name = name;
    }

    public void markUp(String message) {
        up = true;
        statusMessage = message;
    }

    public void markDown(String message) {
        up = false;
        statusMessage = message;
    }
}