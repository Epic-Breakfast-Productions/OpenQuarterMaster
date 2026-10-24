package tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord;

import java.util.List;

public record DiscordEmbed(
    String title,
    String description,
    int color,
    List<DiscordField> fields
) {}
