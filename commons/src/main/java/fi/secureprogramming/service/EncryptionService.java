package fi.secureprogramming.service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionService {
    private final String algorithm = "AES";
    private final byte[] encryptionKeyBytes;

    public EncryptionService(String encryptionKey) {
        this.encryptionKeyBytes = Base64.getDecoder().decode(encryptionKey);
    }

    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec key = new SecretKeySpec(encryptionKeyBytes, algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        SecretKeySpec key = new SecretKeySpec(encryptionKeyBytes, algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
    }
}
