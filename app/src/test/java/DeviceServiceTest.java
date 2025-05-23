import fi.secureprogramming.app.service.DeviceService;
import fi.secureprogramming.model.Device;
import fi.secureprogramming.repository.DeviceRepository;
import fi.secureprogramming.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    private EncryptionService encryptionService;

    private DeviceService deviceService;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        String mockEncryptionKey = Base64.getEncoder().encodeToString("mock-key-16bytes".getBytes(StandardCharsets.UTF_8));
        deviceService = new DeviceService(deviceRepository, mockEncryptionKey);

        encryptionService = new EncryptionService(mockEncryptionKey);
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

    @Test
    public void testRegisteringWithTooShortUUID() {
        Device device = new Device("device-123", "c2VjcmV0", true);
        when(deviceRepository.findById(device.getUuid())).thenReturn(Optional.empty());

        Exception e = assertThrows(Exception.class, () -> {
            deviceService.register(device.getUuid(), device.getSecret());
        });

        assertEquals("UUID must be at least 16 characters long", e.getMessage());
    }

    @Test
    public void testRegisteringWithTooShortSecret() {
        Device device = new Device("device-123456789000", "c2VjcmV0", true);
        when(deviceRepository.findById(device.getUuid())).thenReturn(Optional.empty());

        Exception e = assertThrows(Exception.class, () -> {
            deviceService.register(device.getUuid(), device.getSecret());
        });

        assertEquals("Insecure secret length", e.getMessage());
    }
}