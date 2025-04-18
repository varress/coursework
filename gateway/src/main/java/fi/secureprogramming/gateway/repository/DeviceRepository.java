package fi.secureprogramming.gateway.repository;

import fi.secureprogramming.gateway.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, String> {

}
