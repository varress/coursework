package fi.secureprogramming.gateway.service;

import fi.secureprogramming.gateway.model.Device;
import fi.secureprogramming.gateway.repository.DeviceRepository;
import fi.secureprogramming.gateway.services.DeviceService;
import fi.secureprogramming.gateway.services.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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
    public void testVerifyDeviceOnValidSignature() throws Exception {
        String uuid = "device-123";
        String secret = "c2VjcmV0";
        String timestamp = "1617812345";
        String data = uuid + ":" + timestamp;

        String signature = createSignature(secret, data);

        Device device = new Device(uuid, secret, true);

        when(deviceRepository.findById(uuid)).thenReturn(Optional.of(device));
        when(encryptionService.decrypt(anyString())).thenReturn(secret);

        Device result = deviceService.verifyDevice(uuid, signature, timestamp);

        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        verify(deviceRepository, times(1)).findById(uuid);
    }

    @Test
    public void testVerifyDeviceOnValidSignatureButUnactivatedDevice() throws AuthenticationException, NoSuchAlgorithmException, InvalidKeyException {
        String uuid = "device-123";
        String secret = "c2VjcmV0";
        String timestamp = "1617812345";
        String data = uuid + ":" + timestamp;
        String signature = createSignature(secret, data);

        Device device = new Device(uuid, secret, false);

        when(deviceRepository.findById(uuid)).thenReturn(Optional.of(device));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            deviceService.verifyDevice(uuid, signature, timestamp);
        });
    }

    @Test
    public void testVerifyDeviceOnInvalidSignature() throws Exception {
        String uuid = "device-123";
        String secret = "c2VjcmV0";
        String timestamp = "1617812345";
        String signature = "invalid-signature";

        Device device = new Device(uuid, secret, true);

        when(deviceRepository.findById(uuid)).thenReturn(Optional.of(device));
        when(encryptionService.decrypt(anyString())).thenReturn(secret);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            deviceService.verifyDevice(uuid, signature, timestamp);
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
            deviceService.verifyDevice(uuid, signature, timestamp);
        });

        assertEquals("Device not found", exception.getMessage());
        verify(deviceRepository, times(1)).findById(uuid);
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