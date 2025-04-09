package fi.secureprogramming.gateway.services;

import fi.secureprogramming.gateway.model.Device;
import fi.secureprogramming.gateway.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.sasl.AuthenticationException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private EncryptionService encryptionService;

    public void register(String uuid, String secret) throws Exception {
        deviceRepository.findById(uuid).ifPresent(device -> {
            throw new IllegalArgumentException("Device already registered");
        });

        String encryptedSecret = encryptionService.encrypt(secret);
        Device device = new Device(uuid, encryptedSecret, true);
        deviceRepository.save(device);
    }

    public void inactivateDevice(String uuid) {
        Device device = deviceRepository.findById(uuid).orElseThrow(() -> new NotFoundException("Device not found"));
        device.setActive(false);

        deviceRepository.save(device);
    }

    public void activateDevice(String uuid) {
        Device device = deviceRepository.findById(uuid).orElseThrow(() -> new NotFoundException("Device not found"));
        device.setActive(true);

        deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device verifyDevice(String uuid, String signature, String timestamp) throws Exception {
        Device device = deviceRepository.findById(uuid).orElseThrow(() -> new AuthenticationException("Device not found"));

        if (!device.isActive()) {
            throw new AuthenticationException("Device is not active");
        }

        String decryptedSecret = encryptionService.decrypt(device.getSecret());
        String data = uuid + ":" + timestamp;
        String expectedSig = hmacSha256(decryptedSecret, data);

        if (!expectedSig.equals(signature)) {
            throw new AuthenticationException("Invalid signature");
        }

        return device;
    }

    private String hmacSha256(String signature, String data) {
        try {
            SecretKeySpec key = new SecretKeySpec(Base64.getDecoder().decode(signature), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("HMAC failed", e);
        }
    }

}
