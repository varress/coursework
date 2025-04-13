import fi.secureprogramming.app.service.DeviceService;
import fi.secureprogramming.model.Device;
import fi.secureprogramming.repository.DeviceRepository;
import fi.secureprogramming.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private DeviceService deviceService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(encryptionService.encrypt(anyString())).thenReturn("encrypted-secret");
    }

    @Test
    public void testRegisteringWithUUIDThatAlreadyExists() {
        Device device = new Device("device-123", "c2VjcmV0", true);
        when(deviceRepository.findById(device.getUuid())).thenReturn(Optional.of(device));

        Exception e = assertThrows(Exception.class, () -> {
            deviceService.register(device.getUuid(), device.getSecret());
        });

        assertEquals("Device already registered", e.getMessage());
    }

    private String createSignature(String secret, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(key);
        byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }
}