package fi.secureprogramming.dto;

import fi.secureprogramming.model.Device;
import lombok.Data;

@Data
public class DeviceDTO {
    private String uuid;

    private boolean active;

    public DeviceDTO() {
    }

    public DeviceDTO(String uuid) {
        this.uuid = uuid;
    }

    public DeviceDTO(String uuid, boolean active) {
        this.uuid = uuid;
        this.active = active;
    }

    public static DeviceDTO fromEntity(Device device) {
        if (device == null) {
            return null;
        }

        return new DeviceDTO(
                device.getUuid(),
                device.isActive()
        );
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isActive() {
        return active;
    }
}
