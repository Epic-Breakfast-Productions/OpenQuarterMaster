package tech.ebp.oqm.core.api.exception.mutex;

/**
 * Exception to use when a wait for a mutex lock has timed out.
 *
 * TODO:: add mutex specific data
 */
public class MutexNotRegisteredException extends IllegalStateException {
	public MutexNotRegisteredException(String mutexId) {
		super("Mutex with id " + mutexId + " was not registered when lock attempt made.");
	}
}
