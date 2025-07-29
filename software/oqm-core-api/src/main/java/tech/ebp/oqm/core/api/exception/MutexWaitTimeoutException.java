package tech.ebp.oqm.core.api.exception;

/**
 * Exception to use when a wait for a mutex lock has timed out.
 *
 * TODO:: add mutex specific data
 */
public class MutexWaitTimeoutException extends IllegalStateException {
	public MutexWaitTimeoutException() {
	}

	public MutexWaitTimeoutException(String s) {
		super(s);
	}
}
