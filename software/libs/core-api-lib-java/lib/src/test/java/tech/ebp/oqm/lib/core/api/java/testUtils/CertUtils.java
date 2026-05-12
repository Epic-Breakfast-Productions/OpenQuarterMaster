package tech.ebp.oqm.lib.core.api.java.testUtils;

import lombok.NoArgsConstructor;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CertUtils {
	
	public static final Path httpsPrivateKey = Path.of("dev/testCert.key");
	public static final Path httpsPublicKey = Path.of("dev/testCert.crt");
	public static final Path keystore = Path.of("dev/testKeystore.p12");
	public static final String keystorePass = "mypassword";
	
	public static KeyPair getNewKeypair() {
		KeyPairGenerator keyPairGenerator = null;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		} catch(NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		
		SecureRandom secureRandom = new SecureRandom();
		keyPairGenerator.initialize(2048, secureRandom);
		
		return keyPairGenerator.generateKeyPair();
	}
	
}
