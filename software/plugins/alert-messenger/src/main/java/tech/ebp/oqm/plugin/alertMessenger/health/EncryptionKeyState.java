package tech.ebp.oqm.plugin.alertMessenger.health;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApplicationScoped
public class EncryptionKeyState {
	private volatile boolean valid = false;
	private volatile String reason = "Encryption key not checked yet";

	public void markValid() {
		this.valid = true;
		this.reason = "OK";
	}

	public void markInvalid(String reason) {
		this.valid = false;
		this.reason = reason;
	}
}
