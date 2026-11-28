package tech.ebp.oqm.plugin.alertMessenger.utils;

public enum ConnectionType {
    EMAIL,
    SLACK("hooks.slack.com"),
    ZULIP,
    DISCORD("discord.com");

    private final String[] allowedWebhookHosts;

    ConnectionType(String... allowedWebhookHosts) {
        this.allowedWebhookHosts = allowedWebhookHosts;
    }

    public boolean isAllowedWebhookHost(String host) {
        for (String allowedHost : allowedWebhookHosts) {
            if (allowedHost.equalsIgnoreCase(host)) {
                return true;
            }
        }
        return false;
    }
}
