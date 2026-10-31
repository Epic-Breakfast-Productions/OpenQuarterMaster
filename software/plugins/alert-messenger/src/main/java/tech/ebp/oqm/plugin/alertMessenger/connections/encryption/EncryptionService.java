package tech.ebp.oqm.plugin.alertMessenger.connections.encryption;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.wildfly.security.encryption.CipherUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * Service for encrypting and decrypting strings using AES encryption.
 * Can be created new interface `EncryptionService` and rename this class to `AESEncryptionService` if needed in the future.
 * But for _1.0.0-SNAPSHOT_ this is enough. (don't want to over-engineer it for now)
 * Encryption needed for encryption webhooks in the database and decrypting them when sending messages to the channels.
 * It also can be applied to other sensitive data in the future if needed.
 */

@Slf4j
@ApplicationScoped
public class EncryptionService {

    private final SecretKey secretKey;

    public EncryptionService(@ConfigProperty(name = "oqm.alert-messenger.encrypted-secret") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
    }

    public String encrypt(String encrypted) {
        try {
            log.debug("Encoding string: {}", encrypted);
            return CipherUtil.encrypt(encrypted, secretKey);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String encrypted) {
        try {
            log.debug("Decoding encrypted string: {}", encrypted);
            return CipherUtil.decrypt(encrypted, secretKey);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}