package tech.ebp.oqm.core.api.exception.mutex;

/**
 * Exception to use when a wait for a mutex lock has been interrupted.
 */
public class MutexWaitInterruptedException extends IllegalStateException {
	public MutexWaitInterruptedException(InterruptedException e) {
		super("Mutex wait interrupted.", e);
	}
}
