package tech.ebp.oqm.core.api.exception.mutex;

/**
 * Exception to use when a wait for a mutex lock has timed out.
 *
 * TODO:: add mutex specific data
 */
public class MutexWaitTimeoutException extends IllegalStateException {
	public MutexWaitTimeoutException() {
		super("Mutex wait timed out. Could not acquire lock before timeout.");
	}

	public MutexWaitTimeoutException(String s) {
		super(s);
	}
}
