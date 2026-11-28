package tech.ebp.oqm.plugin.alertMessenger.health;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetailsService;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferencesService;
import tech.ebp.oqm.plugin.alertMessenger.utils.EncryptedResetMode;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class EncryptionKeyStartupCheck {
	@Inject
	ConnectionDetailsService connectionDetailsService;

	@Inject
	UserPreferencesService userPreferencesService;

	@Inject
	EncryptionKeyState state;

	@ConfigProperty(name = "service.encrypted-secret")
	String secret;

	@ConfigProperty(name = "service.old-encrypted-secret")
	Optional<String> oldSecret;

	@ConfigProperty(name = "service.reset-mode", defaultValue = "none")
	EncryptedResetMode resetMode;

	private static final String WEBHOOK_PREFIX = "https://";

	void onStartup(@Observes StartupEvent ev) {
		try {
			if (!isSecretValid(secret)) {
				state.markInvalid("service.encrypted-secret is missing or has invalid length (16/24/32 bytes)");
				return;
			}

			if (this.canDecrypt()) {
				if (oldSecret.isPresent()) {
					log.info("Encryption key is valid, may remove old-encrypted-secret from the configuration.");
				}
				state.markValid();
				return;
			}

			switch (resetMode) {
				case EncryptedResetMode.NONE -> {
					state.markInvalid("Stored connections cannot be decrypted with the configured key. Set the correct key or use reset-mode=reset/migrate");
					log.error("Stored connections cannot be decrypted with the configured key. Set the correct key or use reset-mode=reset/migrate");
					throw new IllegalStateException(state.getReason());
				}
				case EncryptedResetMode.RESET -> {
					log.warn("Cannot decrypt connections, resetting all of them.");
					this.resetConnections();
					state.markValid();
				}
				case EncryptedResetMode.MIGRATE -> {
					String old = oldSecret.filter(EncryptionKeyStartupCheck::isSecretValid).orElse(null);
					if (old == null) {
						state.markInvalid("reset-mode=migrate, but old-encrypted-secret is missing or invalid");
						throw new IllegalStateException(state.getReason());
					}
					connectionDetailsService.migrateConnections(oldSecret.get());
					state.markValid();
				}
				default -> {
					state.markInvalid("Unknown reset-mode: " + resetMode);
					log.error("Unknown reset-mode: {}", resetMode);
				}
			}
		} catch (Exception e) {
			log.error("Encryption key check failed", e);
			state.markInvalid(e.getMessage());
			throw new IllegalStateException(state.getReason());
		}
	}

	private static boolean isSecretValid(String s) {
		if (s == null || s.isBlank()) return false;
		int len = s.getBytes(StandardCharsets.UTF_8).length;
		return len == 16 || len == 24 || len == 32;
	}

	private boolean canDecrypt() {
		try {
			Optional<String> destination = connectionDetailsService.getLastWebhookUrl();
			if (destination.isEmpty()) {
				log.debug("No webhook connections found, skipping encryption health check.");
				return true;
			}
			return destination.get().startsWith(WEBHOOK_PREFIX);
		} catch (RuntimeException e) {
			log.debug("Configured encryption key cannot decrypt stored webhook URL.", e);
			return false;
		}
	}
	@Transactional
	private void resetConnections() {
		Set<ObjectId> deletedIds = connectionDetailsService.deleteConnections();
		userPreferencesService.removeConnectionDetailIds(deletedIds);
		log.info("All webhook connections have been deleted due to encryption key reset.");
	}
}
