package tech.ebp.oqm.plugin.alertMessenger.utils.discord;

import java.util.List;

public record DiscordWebhookMessage(
    String username,
    String avatar_url,
    String content,
    List<DiscordEmbed> embeds
) {
}
