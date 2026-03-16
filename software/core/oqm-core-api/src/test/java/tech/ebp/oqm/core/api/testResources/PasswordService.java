package tech.ebp.oqm.core.api.testResources;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.wildfly.common.codec.DecodeException;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.spec.EncryptablePasswordSpec;
import org.wildfly.security.password.spec.IteratedSaltedPasswordAlgorithmSpec;
import org.wildfly.security.password.util.ModularCrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

/**
 * <a
 * href="https://www.javatips.net/api/wildfly-security-master/wildfly-elytron-master/src/test/java/org/wildfly/security/password/impl/BCryptPasswordTest.java">https://www.javatips.net/api/wildfly-security-master/wildfly-elytron-master/src/test/java/org/wildfly/security/password/impl/BCryptPasswordTest.java</a>
 */
@ApplicationScoped
@Slf4j
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
	 * Gets a random salt.
	 *
	 * @return A random salt
	 */
	private static byte[] getSalt() {
		byte[] salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];
		getSecureRandom().nextBytes(salt);
		return salt;
	}
	
	private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
	private static final String NUMBER_CHARS = "0123456789";
	// https://www.owasp.org/index.php/Password_special_characters
	private static final String SPECIAL_CHARS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
	private static final String ALL = UPPERCASE_CHARS + LOWERCASE_CHARS + NUMBER_CHARS + SPECIAL_CHARS;
	
	public String getRandString(int size) {
		Random rand = getSecureRandom();
		StringBuilder randomTokens = new StringBuilder();
		
		for (int i = 0; i < size; i++) {
			randomTokens.append(ALL.charAt(rand.nextInt(ALL.length())));
		}
		return randomTokens.toString();
	}
	
	public String getRandString(int sizeMin, int sizeMax) {
		return this.getRandString(getSecureRandom().nextInt(sizeMax - sizeMin) + sizeMin);
	}
}
