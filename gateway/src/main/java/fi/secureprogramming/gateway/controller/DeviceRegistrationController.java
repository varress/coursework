package fi.secureprogramming.gateway.controller;

import fi.secureprogramming.gateway.dto.DeviceDTO;
import fi.secureprogramming.gateway.services.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/device")
public class DeviceRegistrationController {

    @Autowired
    private DeviceService deviceService;

    private Logger logger = LoggerFactory.getLogger(DeviceRegistrationController.class);

    //TODO error handling

    @PostMapping("/register")
    public ResponseEntity<Void> registerDevice(@RequestBody DeviceDTO device) {
        if (device.getSecret() == null || device.getSecret().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (device.getUuid() == null || device.getUuid().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            deviceService.register(device.getUuid(), device.getSecret());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error registering device", e); //fixme
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/inactivate")
    public ResponseEntity<Void> unregisterDevice(@RequestBody DeviceDTO device) {
        deviceService.inactivateDevice(device.getUuid());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activateDevice(@RequestBody DeviceDTO device) {
        deviceService.activateDevice(device.getUuid());

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        List<DeviceDTO> devices = deviceService.getAllDevices().stream().map(DeviceDTO::fromEntity).toList();
        return ResponseEntity.ok(devices);
    }
}

