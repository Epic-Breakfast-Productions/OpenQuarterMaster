package tech.ebp.oqm.plugin.alertMessenger.connections;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.plugin.alertMessenger.connections.encryption.EncryptedCredentials;

import java.util.List;

@Slf4j
@ApplicationScoped
public class ConnectionDetailsService {

    @Inject
    ConnectionDetailsRepository repository;

    public List<ConnectionDetails> getForUser(String userId, int pageIndex, int pageSize) {
        return repository.findAllPaged(userId, pageIndex, pageSize).list();
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
            .orElseThrow(() -> new RuntimeException("Connection details not found: " + id));
    }

    public ConnectionDetails create(ConnectionDetails details, String userId) {
        this.verifyEncryptedCredentials(details);
        details.setUserId(userId);
        repository.persist(details);
        return details;
    }

    public ConnectionDetails update(String id, ConnectionDetails details, String userId) {
        ConnectionDetails existing = get(id, userId);
        details.id = existing.id;
        this.verifyEncryptedCredentials(details);
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

    private void verifyEncryptedCredentials(ConnectionDetails details) {
        if(details instanceof EncryptedCredentials encryptedCredentialsFlag) {
            encryptedCredentialsFlag.encryptCredentials();
            log.debug("Encrypted credentials for connection details: {}", details.id);
        }
    }
}
