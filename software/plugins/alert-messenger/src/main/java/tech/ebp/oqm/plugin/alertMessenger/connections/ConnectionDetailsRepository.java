package tech.ebp.oqm.plugin.alertMessenger.connections;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.EncryptionService;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.WebhookConnection;
import tech.ebp.oqm.plugin.alertMessenger.utils.ConnectionType;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConnectionDetailsRepository implements PanacheMongoRepository<ConnectionDetails> {

	private static final String WEBHOOK_PREFIX = "https://";

	@Inject
	EncryptionService encryptionService;

    public List<ConnectionDetails> findAllByIds(List<ObjectId> ids) {
        return find("_id in ?1", ids).list();
    }

    public Optional<ConnectionDetails> findByObjectId(String id) {
        if (!ObjectId.isValid(id)) {
            return Optional.empty();
        }
        return findByIdOptional(new ObjectId(id));
    }

    public PanacheQuery<ConnectionDetails> findAllPagedByUserId(String userId, int pageIndex, int pageSize) {
        return find("userId", userId).page(Page.of(pageIndex, pageSize));
    }

    public PanacheQuery<ConnectionDetails> findAllPaged(int pageIndex, int pageSize) {
        return findAll().page(Page.of(pageIndex, pageSize));
    }

    public void updateLastError(ObjectId id, String lastError) {
        update("lastError = ?1, lastErrorTime = ?2", lastError, Instant.now())
            .where("_id", id);
    }

	@Override
	public void persist(ConnectionDetails entity) {
		encryptIfNeeded(entity);
		PanacheMongoRepository.super.persist(entity);
	}

	@Override
	public void update(ConnectionDetails entity) {
		encryptIfNeeded(entity);
		PanacheMongoRepository.super.update(entity);
	}

	@Override
	public void persistOrUpdate(ConnectionDetails entity) {
		encryptIfNeeded(entity);
		PanacheMongoRepository.super.persistOrUpdate(entity);
	}

	private void encryptIfNeeded(ConnectionDetails entity) {
		if (entity instanceof WebhookConnection webhook) {
			String url = webhook.getWebhookUrl();
			boolean isEncrypted = !url.regionMatches(true, 0, WEBHOOK_PREFIX, 0, WEBHOOK_PREFIX.length());
			if (!isEncrypted) {
				validateWebhookUrl(url, webhook.getType());
				webhook.setWebhookUrl(encryptionService.encrypt(url));
			}
		}
	}

	private void validateWebhookUrl(String url, ConnectionType type) {
		URI uri;
		try {
			uri = new URI(url).parseServerAuthority();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid webhook URL", e);
		}

		if (!"https".equalsIgnoreCase(uri.getScheme())
			|| !uri.isAbsolute()
			|| uri.getHost() == null
			|| uri.getUserInfo() != null
			|| !type.isAllowedWebhookHost(uri.getHost())) {
			throw new IllegalArgumentException("Webhook URL must use HTTPS and an expected host for " + type);
		}
	}
}
