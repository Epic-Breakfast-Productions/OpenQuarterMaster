package tech.ebp.oqm.plugin.alertMessenger.utils.discord;

public record DiscordField(
    String name,
    String value,
    boolean inline
) {
}
