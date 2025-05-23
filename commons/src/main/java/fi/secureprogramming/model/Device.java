package fi.secureprogramming.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Device {
    @Id
    @Column(nullable = false, length = 50)
    private String uuid;

    @Column(nullable = false)
    private String secret;

    private boolean active;

    public Device() {
    }

    public Device(String uuid, String secret, boolean active) {
        this.uuid = uuid;
        this.secret = secret;
        this.active = active;
    }

    public String getUuid() {
        return uuid;
    }

    public String getSecret() {
        return secret;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
