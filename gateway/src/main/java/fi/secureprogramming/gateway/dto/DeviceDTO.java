package fi.secureprogramming.gateway.dto;

import fi.secureprogramming.gateway.model.Device;
import lombok.Data;

@Data
public class DeviceDTO {
    private String uuid;
    private String secret;

    public DeviceDTO() {
    }

    public DeviceDTO(String uuid, String secret) {
        this.uuid = uuid;
        this.secret = secret;
    }

    public static DeviceDTO fromEntity(Device device) {
        if (device == null) {
            return null;
        }

        return new DeviceDTO(
                device.getUuid(),
                device.getSecret()
        );
    }

    public Device toEntity() {
        return new Device(this.uuid, this.secret, true);
    }
}
