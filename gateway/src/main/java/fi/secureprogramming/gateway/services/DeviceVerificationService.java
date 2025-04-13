package fi.secureprogramming.gateway.services;

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

@Service
public class DeviceVerificationService {

    private final DeviceRepository deviceRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public DeviceVerificationService(DeviceRepository deviceRepository, @Value("${encryption.key}") String encryptionKey) {
        this.deviceRepository = deviceRepository;
        this.encryptionService = new EncryptionService(encryptionKey);
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
            SecretKeySpec key = new SecretKeySpec(signature.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("HMAC failed", e);
        }
    }

}
