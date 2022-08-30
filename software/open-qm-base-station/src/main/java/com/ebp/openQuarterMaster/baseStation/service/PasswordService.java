package com.ebp.openQuarterMaster.baseStation.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.wildfly.common.codec.DecodeException;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;
import org.wildfly.security.password.util.ModularCrypt;
import tech.ebp.oqm.lib.core.object.user.User;
import tech.ebp.oqm.lib.core.rest.user.UserLoginRequest;

import javax.enterprise.context.ApplicationScoped;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * <a href="https://www.javatips.net/api/wildfly-security-master/wildfly-elytron-master/src/test/java/org/wildfly/security/password/impl/BCryptPasswordTest.java">https://www.javatips.net/api/wildfly-security-master/wildfly-elytron-master/src/test/java/org/wildfly/security/password/impl/BCryptPasswordTest.java</a>
 */
@ApplicationScoped
@Slf4j
@Traced
public class PasswordService {
	
	private static SecureRandom SECURE_RANDOM = null;
	private static final String ALGORITHM = BCryptPassword.ALGORITHM_BCRYPT;
	private static final int ITERATIONS = 11;
	
	private final PasswordFactory passwordFactory;
	
	/**
	 * Initialized at runtime, required for native builds.
	 *
	 * @return A Secure Random for use while creating salts
	 */
	private static synchronized SecureRandom getSecureRandom() {
		if (SECURE_RANDOM == null) {
			SECURE_RANDOM = new SecureRandom();
		}
		return SECURE_RANDOM;
	}
	
	public PasswordService() {
		WildFlyElytronPasswordProvider provider = WildFlyElytronPasswordProvider.getInstance();
		
		try {
			this.passwordFactory = PasswordFactory.getInstance(ALGORITHM, provider);
		} catch(NoSuchAlgorithmException e) {
			log.error("Somehow got an exception when setting up password factory. Error: ", e);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Makes a hash for the password given.
	 *
	 * @param password The password to hash
	 * @param iterations The number of iterations to perform (4-34)
	 *
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
		} catch(InvalidKeySpecException e) {
			log.error("Somehow got an invalid key spec. This should not happen. Error: ", e);
			throw new IllegalStateException("Somehow got an invalid key spec. This should not happen.", e);
		}
	}
	
	/**
	 * Makes a hash for the password given.
	 * <p>
	 * Wrapper for {@link #createPasswordHash(String, int)}, giving {@link #ITERATIONS} as the number of iterations.
	 *
	 * @param password The password to hash
	 *
	 * @return The hash for the password
	 */
	public String createPasswordHash(String password) {
		return this.createPasswordHash(password, ITERATIONS);
	}
	
	/**
	 * Determines if the password given matches what is represented by the hash.
	 *
	 * @param encodedPass The hash of the user's password
	 * @param pass The password given to check
	 *
	 * @return If the password given matched the hashed password
	 */
	public boolean passwordMatchesHash(String encodedPass, String pass) {
		BCryptPassword original = null;
		try {
			original = (BCryptPassword) passwordFactory.translate(ModularCrypt.decode(encodedPass));
		} catch(DecodeException e) {
			log.error("Was unable to decode the password. Error: ", e);
			throw new IllegalStateException("Was unable to decode the password, indicates corrupted password.", e);
		} catch(InvalidKeySpecException | InvalidKeyException e) {
			log.error("Somehow got an invalid key/spec. This should not happen. Error: ", e);
			throw new IllegalStateException("Somehow got an invalid key/spec. This should not happen.", e);
		}
		try {
			return passwordFactory.verify(original, pass.toCharArray()); // throws the invalid key exception
		} catch(InvalidKeyException e) {
			log.error("Somehow got an invalid key. This probably shouldn't happen? Error: ", e);
			throw new IllegalStateException("Somehow got an invalid key. This should not happen.", e);
		}
	}
	
	/**
	 * Checks to see if the password given in the login request matches the hashed pasword of the user.
	 * <p>
	 * Wrapper for {@link #passwordMatchesHash(String, String)}.
	 *
	 * @param user The user to get the pw hash from
	 * @param loginRequest The request sent to try to authenticate a user
	 *
	 * @return If the request contained the correct password for the user.
	 */
	public boolean passwordMatchesHash(User user, UserLoginRequest loginRequest) {
		return this.passwordMatchesHash(user.getPwHash(), loginRequest.getPassword());
	}
	
	/**
	 * Gets a random salt.
	 *
	 * @return A random salt
	 */
	private static byte[] getSalt() {
		byte[] salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];
		getSecureRandom().nextBytes(salt);
		return salt;
	}
}
