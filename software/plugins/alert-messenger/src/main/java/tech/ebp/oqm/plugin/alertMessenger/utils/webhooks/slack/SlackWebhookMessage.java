package tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.slack;

import tech.ebp.oqm.plugin.alertMessenger.utils.webhooks.WebhookMessage;

public record SlackWebhookMessage(String text) implements WebhookMessage {}
