package fi.vm.sade.conversion.hakemus.henkilogenerator;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// from haku-app
public class SHA2Encrypter {
    private final Logger logger = LoggerFactory.getLogger(SHA2Encrypter.class);
    public static final String ALGORITHM = "SHA-256";
    private final String salt;

    public SHA2Encrypter(final String salt) {
        logger.info("salt: {}", salt);
        this.salt = salt;
    }

    public String decrypt(String encrypt) {
        throw new UnsupportedOperationException("SHA-2 is irreversible");
    }

    public String encrypt(final String encrypt) {
        try {
            return countDigest(encrypt + salt);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String countDigest(final String encrypt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
        md.update(encrypt.getBytes());
        return new String(Hex.encodeHex(md.digest()));
    }
}
