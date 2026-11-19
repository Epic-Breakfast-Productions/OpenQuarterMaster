package tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.discord;

import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.WebhookMessage;

import java.util.List;
public record DiscordWebhookMessage(
    String username,
    String avatar_url,
    String content,
    List<DiscordEmbed> embeds
) implements WebhookMessage {}
