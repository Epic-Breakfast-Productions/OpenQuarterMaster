package tech.ebp.oqm.core.api.exception.mutex;

/**
 * Exception to use when a lock is attempted on a non registered mutex.
 */
public class MutexNotRegisteredException extends IllegalStateException {
	public MutexNotRegisteredException(String mutexId) {
		super("Mutex with id " + mutexId + " was not registered when lock attempt made.");
	}
}
