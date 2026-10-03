package tech.ebp.oqm.plugin.alertMessenger.utils.discord;

import java.util.List;

public record DiscordEmbed(
    String title,
    String description,
    int color,
    List<DiscordField> fields
) {
}
