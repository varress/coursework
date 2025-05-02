package fi.secureprogramming.dto;

import fi.secureprogramming.model.Device;
import lombok.Data;

@Data
public class DeviceDTO {
    private String uuid;

    public DeviceDTO() {
    }

    public DeviceDTO(String uuid) {
        this.uuid = uuid;
    }

    public static DeviceDTO fromEntity(Device device) {
        if (device == null) {
            return null;
        }

        return new DeviceDTO(
                device.getUuid()
        );
    }

    public String getUuid() {
        return uuid;
    }
}
