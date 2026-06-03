package tech.ebp.oqm.core.api.health;

public interface StatusProvider {
	String getName();
	boolean isReady();
	String getStatusMessage();
	void markStarted(String message);
	void markCompleted(String message);
	void markFailed(String message);
}

