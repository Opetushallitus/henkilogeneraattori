package fi.vm.sade.conversion.hakemus.henkilogenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

// from haku-app
public class AESEncrypter {
    private final Logger logger = LoggerFactory.getLogger(AESEncrypter.class);
    private static final int ITERATION_COUNT = 65436;
    private static final int KEY_LENGTH = 256;
    public static final int IV_SIZE = 16;
    private static final String RANDOM_ALGORITHM = "SHA1PRNG";
    public static final String AES = "AES";
    public static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    public static final String PBKDF_2_WITH_HMAC_SHA_1 = "PBKDF2WithHmacSHA1";
    public static final String CHARSET_NAME = "UTF-8";
    private final SecretKey secret;


    public AESEncrypter(final String passPhrase, final String salt)
            throws InvalidKeySpecException, NoSuchAlgorithmException, UnsupportedEncodingException {
        logger.info("pass & salt: {}, {}", passPhrase, salt);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_2_WITH_HMAC_SHA_1);
        KeySpec spec = new PBEKeySpec(passPhrase.toCharArray(), salt.getBytes("UTF-8"), ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        this.secret = new SecretKeySpec(tmp.getEncoded(), AES);
    }

    public String encrypt(String encrypt) {
        try {
            return encryptInternal(encrypt);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String encrypt) {
        try {
            return decryptInternal(encrypt);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private String encryptInternal(String encrypt)
            throws UnsupportedEncodingException, GeneralSecurityException {
        byte[] bytes = encrypt.getBytes("UTF-8");
        byte[] encrypted = encrypt(bytes);
        return DatatypeConverter.printBase64Binary(encrypted);
    }

    private byte[] encrypt(byte[] plain)
            throws GeneralSecurityException {
        byte[] iv = generateIv();

        Cipher ecipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);

        ecipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
        final byte[] bytes = ecipher.doFinal(plain);

        return merge(iv, bytes);
    }

    protected byte[] merge(byte[] iv, byte[] bytes) {
        final byte[] result = new byte[iv.length + bytes.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(bytes, 0, result, iv.length, bytes.length);
        return result;
    }

    private String decryptInternal(String encrypt) throws GeneralSecurityException, UnsupportedEncodingException {
        byte[] bytes = DatatypeConverter.parseBase64Binary(encrypt);
        byte[] decrypted = decrypt(bytes);
        return new String(decrypted, CHARSET_NAME);
    }

    protected byte[] generateIv() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
        byte[] iv = new byte[IV_SIZE];
        random.nextBytes(iv);
        return iv;
    }

    private byte[] decrypt(byte[] encrypt) throws GeneralSecurityException {
        Cipher dcipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        dcipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(Arrays.copyOfRange(encrypt, 0, IV_SIZE)));
        return dcipher.doFinal(Arrays.copyOfRange(encrypt, IV_SIZE, encrypt.length));
    }
}
