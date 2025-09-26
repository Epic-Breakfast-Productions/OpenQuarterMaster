package tech.ebp.oqm.lib.core.api.java.testUtils;

import lombok.NoArgsConstructor;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.security.auth.x500.X500Principal;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Map;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CertUtils {
	
	private static final Path CERT_DIR = Path.of("build/");
	private static int fileCount = 0;
	
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
	
	
	
	
	
	
	public static void generateSelfSignedCertificateSecret(String name, Map<String, String> labels, String host) {
		Security.addProvider(new BouncyCastleProvider());
		
		X500Principal subject = new X500Principal("CN=" + host);
		X500Principal signedByPrincipal = subject;
		KeyPair keyPair = getNewKeypair();
		KeyPair signedByKeyPair = keyPair;
		
		long notBefore = System.currentTimeMillis();
		long notAfter = notBefore + (1000L * 3600L * 24 * 365);
		
		ASN1Encodable[] encodableAltNames = new ASN1Encodable[]{new GeneralName(GeneralName.dNSName, host)};
		KeyPurposeId[] purposes = new KeyPurposeId[]{KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth};
		
		
		
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static String keyToPem(Key key){
		
		PemObject pemObject = new PemObject("RSA " + (key instanceof PrivateKey? "PRIVATE KEY": "PUBLIC KEY"), key.getEncoded());
		
		StringWriter stringWriter = new StringWriter();
		try (PemWriter pemWriter = new PemWriter(stringWriter)) {
			pemWriter.writeObject(pemObject);
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println(stringWriter.toString());
		return stringWriter.toString();
	}
	
	public static String getPublicKeyPem(KeyPair keyPair) {
		return keyToPem(keyPair.getPublic());
	}
	public static String getPrivateKeyPem(KeyPair keyPair) {
		return keyToPem(keyPair.getPrivate());
	}
	
	private static Path writePem(Key key){
		String content = keyToPem(key);
		
		Path file = CERT_DIR.resolve("key" + fileCount++ + ".pem");
		
		try(
			OutputStream os = Files.newOutputStream(file)
			){
			os.write(content.getBytes());
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
		return file;
	}
	
	public static Path writePublicKeyPem(KeyPair key) {
		return writePem(key.getPublic());
	}
	public static Path writePrivateKeyPem(KeyPair key) {
		return writePem(key.getPrivate());
	}
	
}
