package fi.secureprogramming.gateway.service;

import fi.secureprogramming.gateway.model.Device;
import fi.secureprogramming.gateway.repository.DeviceRepository;
import fi.secureprogramming.gateway.services.DeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.sasl.AuthenticationException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testVerifyDeviceOnValidSignature() throws AuthenticationException, NoSuchAlgorithmException, InvalidKeyException {
        String uuid = "device-123";
        String secret = "c2VjcmV0"; // Base64 encoded "secret"
        String timestamp = "2023-01-01T12:00:00Z";
        String data = uuid + ":" + timestamp;
        String signature = getSecret(secret, data);

        Device device = new Device(uuid, secret, true);

        when(deviceRepository.findById(uuid)).thenReturn(Optional.of(device));

        Device result = deviceService.verifyDevice(uuid, signature, timestamp);

        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        verify(deviceRepository, times(1)).findById(uuid);
    }

    @Test
    public void testVerifyDeviceOnValidSignatureButUnactivatedDevice() throws AuthenticationException, NoSuchAlgorithmException, InvalidKeyException {
        String uuid = "device-123";
        String secret = "c2VjcmV0"; // Base64 encoded "secret"
        String timestamp = "2023-01-01T12:00:00Z";
        String data = uuid + ":" + timestamp;
        String signature = getSecret(secret, data);

        Device device = new Device(uuid, secret, false);

        when(deviceRepository.findById(uuid)).thenReturn(Optional.of(device));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            deviceService.verifyDevice(uuid, signature, timestamp);
        });
    }

    @Test
    public void testVerifyDeviceOnInvalidSignature() {
        String uuid = "device-123";
        String secret = "c2VjcmV0"; // Base64 encoded "secret"
        String timestamp = "2023-01-01T12:00:00Z";
        String signature = "invalid-signature";

        Device device = new Device(uuid, secret, true);

        when(deviceRepository.findById(uuid)).thenReturn(Optional.of(device));

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
        String timestamp = "2023-01-01T12:00:00Z";

        when(deviceRepository.findById(uuid)).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            deviceService.verifyDevice(uuid, signature, timestamp);
        });

        assertEquals("Device not found", exception.getMessage());
        verify(deviceRepository, times(1)).findById(uuid);
    }

    private String getSecret(String secret, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(secret), "HmacSHA256");
        mac.init(secretKeySpec);
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
    }
}