package tech.ebp.oqm.lib.core.api.java.testUtils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.SneakyThrows;
import net.datafaker.Faker;
import tech.ebp.oqm.lib.core.api.java.auth.JwtCreds;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class JwtUtils {
	public static final KeyPair SIGNING_KEYPAIR = CertUtils.getNewKeypair();
	private static final Faker FAKER = new Faker();
	
	public static String generateJwtToken(boolean admin) {
		String username = FAKER.internet().username();
		List<String> roles = new ArrayList<>();
		
		roles.add("inventoryView");
		roles.add("inventoryEdit");
		
		if(admin) {
			roles.add("inventoryAdmin");
		}
		
		
		JwtBuilder builder = Jwts.builder()
								 .issuer("testJwtUtils")
								 .subject(UUID.randomUUID().toString())
								 .claim("name", FAKER.name().fullName())
								 .claim("email", FAKER.internet().emailAddress())
								 .claim("groups", roles)
								 .claim("upn", username)
								 .claim("username", username)
								 .claim("preferred_username", username)
								 .issuedAt(
									 Date.from(ZonedDateTime.now().minus(5, ChronoUnit.MINUTES).toInstant())
								 )
								 .expiration(Date.from(ZonedDateTime.now().plus(5, ChronoUnit.MINUTES).toInstant()))
								 .signWith(SIGNING_KEYPAIR.getPrivate());
		return builder.compact();
	}
	
	public static JwtCreds generateJwtCreds(boolean admin){
		return JwtCreds.builder()
					   .jwt(generateJwtToken(admin))
					   .build();
	}
}
