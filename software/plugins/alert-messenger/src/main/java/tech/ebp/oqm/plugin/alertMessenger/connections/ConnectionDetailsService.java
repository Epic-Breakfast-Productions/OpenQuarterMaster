package tech.ebp.oqm.plugin.alertMessenger.connections;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.EncryptionService;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.WebhookConnection;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class ConnectionDetailsService {

    @Inject
    ConnectionDetailsRepository repository;

    @Inject
    EncryptionService encryptionService;

    public List<ConnectionDetails> getForUser(String userId, int pageIndex, int pageSize) {
        return repository.findAllPagedByUserId(userId, pageIndex, pageSize).list();
    }

    public List<ConnectionDetails> getAll(int pageIndex, int pageSize) {
        return repository.findAllPaged(pageIndex, pageSize).list();
    }

    public List<ConnectionDetails> getByIds(List<ObjectId> ids) {
        return repository.findAllByIds(ids);
    }

    public ConnectionDetails get(String id, String userId) {
        return repository.findByObjectId(id)
            .filter(details -> userId.equals(details.getUserId()))
            .orElseThrow(() -> new NotFoundException("Connection details not found: " + id));
    }

    public ConnectionDetails create(ConnectionDetails details, String userId) {
        details.setUserId(userId);
        repository.persist(details);
        return details;
    }

    public ConnectionDetails update(String id, ConnectionDetails details, String userId) {
        ConnectionDetails existing = get(id, userId);
        details.id = existing.id;
        details.setUserId(existing.getUserId());
        repository.update(details);
        return details;
    }

    public void delete(String id, String userId) {
        repository.delete(get(id, userId));
    }

    public void updateLastError(ConnectionDetails details, String lastError) {
        repository.updateLastError(details.id, lastError);
    }

	public Optional<String> getLastWebhookUrl() {
		return repository.getLastWebhookUrl();
	}

	public Set<ObjectId> deleteConnections() {
		Set<ObjectId> deletedIds = new HashSet<>();
		Iterator<WebhookConnection> connections = repository.findAllWebhooks();
		while (connections.hasNext()) {
			WebhookConnection webhook = connections.next();
			deletedIds.add(webhook.id);
			repository.delete(webhook);
		}
		return deletedIds;
	}

	public void migrateConnections(String oldSecret) {
		SecretKey oldKey = new SecretKeySpec(oldSecret.getBytes(StandardCharsets.UTF_8), "AES");
		Iterator<WebhookConnection> connections = repository.findAllWebhooks();
		while (connections.hasNext()) {
			WebhookConnection webhook = connections.next();
			String encryptedUrl = webhook.getWebhookUrl();
			String migratedUrl = encryptionService.migrate(encryptedUrl, oldKey);
			webhook.setWebhookUrl(migratedUrl);
			repository.update(webhook);
		}
	}
}
