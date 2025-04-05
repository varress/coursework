package fi.secureprogramming.gateway.services;

import fi.secureprogramming.gateway.model.Device;
import fi.secureprogramming.gateway.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    public boolean existsById(String uuid) {
        return deviceRepository.existsById(uuid);
    }

    public void save(Device device) {
        deviceRepository.save(device);
    }

    public void inactivateDevice(String uuid) {
        Device device = deviceRepository.findById(uuid).orElseThrow(() -> new RuntimeException("Device not found"));
        device.setActive(false);

        deviceRepository.save(device);
    }

    public void activateDevice(String uuid) {
        Device device = deviceRepository.findById(uuid).orElseThrow(() -> new RuntimeException("Device not found"));
        device.setActive(true);

        deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
}
