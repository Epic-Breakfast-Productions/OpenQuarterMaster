package tech.ebp.oqm.plugin.alertMessenger.utils;

import jakarta.inject.Singleton;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.wildfly.security.encryption.CipherUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.nio.charset.StandardCharsets;

@Singleton
public class EncryptedStringCodec implements Codec<EncryptedString> {

    private final SecretKey secretKey;

    public EncryptedStringCodec(@ConfigProperty(name = "oqm.alert-messenger.encrypted-secret") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
    }

    @Override
    public EncryptedString decode(BsonReader reader, DecoderContext decoderContext) {
        String encrypted = reader.readString();
        try {
            String plain = CipherUtil.decrypt(encrypted, secretKey);
            return new EncryptedString(plain);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void encode(BsonWriter writer, EncryptedString value, EncoderContext encoderContext) {
        try {
            String encrypted = CipherUtil.encrypt(value.value(), secretKey);
            writer.writeString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<EncryptedString> getEncoderClass() {
        return EncryptedString.class;
    }
}