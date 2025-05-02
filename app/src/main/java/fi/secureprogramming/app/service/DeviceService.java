package fi.secureprogramming.app.service;

import fi.secureprogramming.model.Device;
import fi.secureprogramming.repository.DeviceRepository;
import fi.secureprogramming.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, @Value("${encryption.key}") String encryptionKey) {
        this.deviceRepository = deviceRepository;
        this.encryptionService = new EncryptionService(encryptionKey);
    }

    public void register(String uuid, String secret) throws Exception {
        deviceRepository.findById(uuid).ifPresent(device -> {
            throw new IllegalArgumentException("Device already registered");
        });

        if (uuid == null || uuid.length() < 16) {
            throw new IllegalArgumentException("UUID must be at least 16 characters long");
        }

        byte[] decodedSecret;
        try {
            decodedSecret = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Secret must be a valid Base64-encoded string");
        }

        if (decodedSecret.length < 32) {
            throw new IllegalArgumentException("Insecure secret length");
        }

        String encryptedSecret = encryptionService.encrypt(new String(decodedSecret));
        Device device = new Device(uuid, encryptedSecret, true);
        deviceRepository.save(device);
    }

    public void inactivateDevice(String uuid) throws Exception {
        Device device = deviceRepository.findById(uuid).orElseThrow(() -> new Exception("Device not found"));
        device.setActive(false);

        deviceRepository.save(device);
    }

    public void activateDevice(String uuid) throws Exception {
        Device device = deviceRepository.findById(uuid).orElseThrow(() -> new Exception("Device not found"));
        device.setActive(true);

        deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

}
