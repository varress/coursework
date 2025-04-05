package fi.secureprogramming.gateway.repository;

import fi.secureprogramming.gateway.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {
    Optional<Device> findByUuidAndActiveTrue(String uuid);
}

