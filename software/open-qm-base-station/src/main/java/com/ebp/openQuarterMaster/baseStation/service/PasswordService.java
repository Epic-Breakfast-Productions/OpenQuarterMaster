package com.ebp.openQuarterMaster.baseStation.service;


import lombok.extern.slf4j.Slf4j;
import org.wildfly.common.codec.DecodeException;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;
import org.wildfly.security.password.util.ModularCrypt;

import javax.enterprise.context.ApplicationScoped;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * https://www.javatips.net/api/wildfly-security-master/wildfly-elytron-master/src/test/java/org/wildfly/security/password/impl/BCryptPasswordTest.java
 */
@ApplicationScoped
@Slf4j
public class PasswordService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String ALGORITHM = BCryptPassword.ALGORITHM_BCRYPT;
    private static final int ITERATIONS = 11;

    private final PasswordFactory passwordFactory;

    public PasswordService() {
        WildFlyElytronPasswordProvider provider = WildFlyElytronPasswordProvider.getInstance();

        try {
            this.passwordFactory = PasswordFactory.getInstance(ALGORITHM, provider);
        } catch (NoSuchAlgorithmException e) {
            log.error("Somehow got an exception when setting up password factory. Error: ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Validate/sanitizes the password given, and makes a hash out of it.
     *
     * @param password The password to hash
     * @return The hash for the password
     */
    public String createPasswordHash(String password, int iterations) {

        IteratedSaltedPasswordAlgorithmSpec iteratedAlgorithmSpec = new IteratedSaltedPasswordAlgorithmSpec(
                iterations,
                getSalt()
        );
        EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(
                password.toCharArray(),
                iteratedAlgorithmSpec
        );

        try {
            BCryptPassword original = (BCryptPassword) passwordFactory.generatePassword(encryptableSpec);
            return ModularCrypt.encodeAsString(original);
        } catch (InvalidKeySpecException e) {
            log.error("Somehow got an invalid key spec. This should not happen. Error: ", e);
            throw new IllegalStateException("Somehow got an invalid key spec. This should not happen.", e);
        }
    }

    public String createPasswordHash(String password) {
        return this.createPasswordHash(password, ITERATIONS);
    }

    public boolean passwordMatchesHash(String encodedPass, String pass) {
        BCryptPassword original = null;
        try {
            original = (BCryptPassword) passwordFactory.translate(ModularCrypt.decode(encodedPass));
        } catch (DecodeException e) {
            log.error("Was unable to decode the password. Error: ", e);
            throw new IllegalStateException("Was unable to decode the password, indicates corrupted password.", e);
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            log.error("Somehow got an invalid key/spec. This should not happen. Error: ", e);
            throw new IllegalStateException("Somehow got an invalid key/spec. This should not happen.", e);
        }
        try {
            return passwordFactory.verify(original, pass.toCharArray()); // throws the invalid key exception
        } catch (InvalidKeyException e) {
            log.error("Somehow got an invalid key. This probably shouldn't happen? Error: ", e);
            throw new IllegalStateException("Somehow got an invalid key. This should not happen.", e);
        }
    }

    public void assertPasswordMatchesHash(String encodedPass, String pass) {
        if (!this.passwordMatchesHash(encodedPass, pass)) {
            throw new IllegalArgumentException("Password given was incorrect.");
        }
    }

    /**
     * Gets a random salt.
     *
     * @return A random salt
     */
    private static byte[] getSalt() {
        byte[] salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];
        SECURE_RANDOM.nextBytes(salt);
        return salt;
    }
}
