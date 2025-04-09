package fi.secureprogramming.gateway.service;

import fi.secureprogramming.gateway.services.EncryptionService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private static final String TEST_KEY = "PGLy9GW6Ie4ftAzWbU2Yfg==";
    private static final String TEST_DATA = "HelloWorld";
    private static EncryptionService encryptionService;

    @BeforeAll
    static void setUp() {
        encryptionService = new EncryptionService(TEST_KEY);
    }

    @Test
    void testEncryptAndDecryptConsistency() throws Exception {
        String encryptedData = encryptionService.encrypt(TEST_DATA);
        assertNotEquals(TEST_DATA, encryptedData, "Encrypted data should not be equal to original data");
        String decryptedData = encryptionService.decrypt(encryptedData);
        assertEquals(TEST_DATA, decryptedData);
    }
}