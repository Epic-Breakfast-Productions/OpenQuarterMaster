package tech.ebp.oqm.core.api.exception.mutex;

/**
 * Exception to use when a wait for a mutex lock has timed out.
 *
 * TODO:: add mutex specific data
 */
public class MutexWaitInterruptedException extends IllegalStateException {
	public MutexWaitInterruptedException(InterruptedException e) {
		super("Mutex wait interrupted.", e);
	}
}
