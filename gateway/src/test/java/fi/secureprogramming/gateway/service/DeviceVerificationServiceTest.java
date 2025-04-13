package fi.secureprogramming.gateway.service;

import fi.secureprogramming.gateway.services.DeviceVerificationService;
import fi.secureprogramming.model.Device;
import fi.secureprogramming.repository.DeviceRepository;
import fi.secureprogramming.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.sasl.AuthenticationException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeviceVerificationServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    private EncryptionService encryptionService;

    private DeviceVerificationService deviceVerificationService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        String mockEncryptionKey = Base64.getEncoder().encodeToString("mock-key-16bytes".getBytes(StandardCharsets.UTF_8));
        deviceVerificationService = new DeviceVerificationService(deviceRepository, mockEncryptionKey);

        encryptionService = new EncryptionService(mockEncryptionKey);
    }

    @Test
    public void testVerifyDeviceOnValidSignature() throws Exception {
        String encryptedSecret = encryptionService.encrypt("valid-secret-16");

        String uuid = "device-123";
        String timestamp = "1617812345";
        String data = uuid + ":" + timestamp;

        String signature = createSignature("valid-secret-16", data);

        Device device = new Device(uuid, encryptedSecret, true);
        when(deviceRepository.findById(uuid)).thenReturn(Optional.of(device));

        Device result = deviceVerificationService.verifyDevice(uuid, signature, timestamp);

        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        verify(deviceRepository, times(1)).findById(uuid);
    }

    @Test
    public void testVerifyDeviceOnValidSignatureButUnactivatedDevice() throws Exception {
        String encryptedSecret = encryptionService.encrypt("valid-secret-16");

        String uuid = "device-123";
        String timestamp = "1617812345";
        String data = uuid + ":" + timestamp;

        String signature = createSignature("valid-secret-16", data);

        Device device = new Device(uuid, encryptedSecret, false);

        when(deviceRepository.findById(uuid)).thenReturn(Optional.of(device));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            deviceVerificationService.verifyDevice(uuid, signature, timestamp);
        });
    }

    @Test
    public void testVerifyDeviceOnInvalidSignature() throws Exception {
        String encryptedSecret = encryptionService.encrypt("valid-secret-16");

        String uuid = "device-123";
        String timestamp = "1617812345";
        String data = uuid + ":" + timestamp;

        String signature = createSignature("valid-secret-19", data);

        Device device = new Device(uuid, encryptedSecret, true);

        when(deviceRepository.findById(uuid)).thenReturn(Optional.of(device));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            deviceVerificationService.verifyDevice(uuid, signature, timestamp);
        });

        assertEquals("Invalid signature", exception.getMessage());
        verify(deviceRepository, times(1)).findById(uuid);
    }

    @Test
    public void testVerifyDeviceOnDeviceNotFound() {
        String uuid = "device-123";
        String signature = "some-signature";
        String timestamp = "1617812345";

        when(deviceRepository.findById(uuid)).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            deviceVerificationService.verifyDevice(uuid, signature, timestamp);
        });

        assertEquals("Device not found", exception.getMessage());
        verify(deviceRepository, times(1)).findById(uuid);
    }

    private String createSignature(String secret, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(key);
        byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }
}