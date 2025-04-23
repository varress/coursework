package fi.secureprogramming.app.service;

import fi.secureprogramming.model.Device;
import fi.secureprogramming.repository.DeviceRepository;
import fi.secureprogramming.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.sasl.AuthenticationException;
import java.nio.charset.StandardCharsets;
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

        byte[] decodedSecret = Base64.getDecoder().decode(secret);
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
