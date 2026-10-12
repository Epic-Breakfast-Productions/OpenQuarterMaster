package tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord;

public record DiscordField(
    String name,
    String value,
    boolean inline
) {}
