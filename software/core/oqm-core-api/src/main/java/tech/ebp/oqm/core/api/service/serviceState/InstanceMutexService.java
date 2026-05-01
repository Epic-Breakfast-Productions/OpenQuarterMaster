package tech.ebp.oqm.core.api.service.serviceState;

import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import tech.ebp.oqm.core.api.config.MutexConfig;
import tech.ebp.oqm.core.api.exception.MutexWaitTimeoutException;
import tech.ebp.oqm.core.api.model.InstanceMutex;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.rest.search.InstanceMutexSearch;
import tech.ebp.oqm.core.api.service.mongo.TopLevelMongoService;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.mongodb.client.model.Filters.*;

/**
 * Provides an interface through which to provide mutex functionality between instances of the core api.
 * <p>
 * Important data:
 * <ul>
 *    <li>Mutex id: the id of the mutex to use; Is used to identify the specific mutex to work off of</li>
 *    <li>Identity: the identity of the entity trying to use the mutex, defaults to this instance's running uuid</li>
 *    <li>Additional ID; An additional identity to identify within one instance; added onto the base identity when doing lock/frees</li>
 * </ul>
 *
 * <p>
 * Use:
 * <ol>
 *     <li>Call {@link #register(String)} to ensure mutex exists. Always call at least once before locking.</li>
 *     <li>Call {@link #lock(String, Optional)} to grab the lock.</li>
 *     <li>Call {@link #free(String, Optional)} to free the lock.</li>
 *     <li>Use {@link #getResource(boolean, String, Optional)} to get a locking resource usable in a try-with-resources. Call {@link #register(String)} before using.</li>
 * </ol>
 * Details:
 * <ul>
 *     <li>NOT re-entrant safe. A second call to lock() on these mutexes from the same identity will fail to lock</li>
 * </ul>
 * <p>
 * Original inspiration: https://www.disk91.com/2018/technology/programming/springboot-mongo-create-a-mutex-to-synchronize-multiple-frontends/
 */
@ApplicationScoped
@Slf4j
public class InstanceMutexService extends TopLevelMongoService<InstanceMutex, InstanceMutexSearch, CollectionStats> {
	
	public static <T extends MainObject> String getMutexIdFor(T object) {
		if (object.getId() == null) {
			throw new IllegalArgumentException("Cannot get mutex id for object with null id");
		}
		return object.getClass().getSimpleName() + "-" + object.getId();
	}
	
	
	@Inject
	@Getter(AccessLevel.PRIVATE)
	MutexConfig mutexConfig;
	
	protected InstanceMutexService() {
		super(InstanceMutex.class);
	}
	
	/**
	 * Builds the identity for the mutex. returned value used as final value for identity in mutex mongo collection.
	 * @param additionalIdentity If there is an additional identity to provide.
	 * @return
	 */
	protected String getIdentity(Optional<String> additionalIdentity) {
		return this.getMutexConfig().instanceId() + additionalIdentity.map(s->"-" + s).orElse("");
	}
	
	/**
	 * Clears any duplicate mutexes for the given mutex id. This is to prevent multiple instances of the same mutex from being created.
	 * @param mutexId The id of the mutex to clear duplicates for.
	 */
	private void clearDuplicateMutexes(String mutexId) {
		//TODO:: cache, to only do one check for any one mutex?
		List<InstanceMutex> mutexes = this.getTypedCollection().find(eq("mutexId", mutexId)).into(new ArrayList<>());
		if (mutexes.size() > 1) {
			log.info("Multiple mutex objects for {} detected ({} total mutexes). Deleting extra.", mutexId, mutexes.size());
			mutexes.removeFirst();
			
			for (InstanceMutex mutex : mutexes) {
				this.getTypedCollection().deleteOne(eq("_id", mutex.getId()));
			}
		}
	}
	
	/**
	 * Registers the mutex with the given id. Always call this before starting to use the mutex.
	 *
	 * @param mutexId The id of the mutex to register.
	 */
	public void register(String mutexId) {
		// Verify if exists
		List<InstanceMutex> existingMutexes = this.getTypedCollection().find(eq("mutexId", mutexId)).into(new ArrayList<>());
		
		log.debug("Existent mutexes: {}", existingMutexes);
		
		if (existingMutexes.isEmpty()) {
			// create it assuming no collision at this point
			InstanceMutex newMutex = new InstanceMutex(mutexId);
			log.info("Creating new mutex {}", newMutex);
			
			UpdateResult result = this.getTypedCollection().updateOne(
				eq("mutexId", mutexId),
				Updates.set("mutexId", mutexId),
				new UpdateOptions().upsert(true)
			);
			
			if (result.getUpsertedId() != null) {
				log.info("Mutex {} created.", mutexId);
			} else {
				log.info("Mutex {} was added in another thread before we could get to it.", mutexId);
			}
		} else if (existingMutexes.size() > 1) {
			// problem
			log.error("multiple mutex for id {}", mutexId);
			this.clearDuplicateMutexes(mutexId);
		}
	}
	
	/**
	 * Locks the mutex with the given id.
	 * <p>
	 * Non blocking.
	 * <p>
	 * Call {@link #register(String)} before using this method.
	 *
	 * @param mutexId The is of the mutex to get the lock for
	 * @param additionalIdentity If an additional identity is required. Only use if need the mutex to offer concurrency within the same instance of the app.
	 *
	 * @return true when reserved, false when not.
	 */
	public boolean lock(@NonNull String mutexId, Optional<String> additionalIdentity) {
		String identity = this.getIdentity(additionalIdentity);
		Bson mutexIdEquals = eq("mutexId", mutexId);
		
		//ensure only one mutex object
		this.clearDuplicateMutexes(mutexId);
		
		// try an update
		InstanceMutex old = this.getTypedCollection().findOneAndUpdate(
			and(
				mutexIdEquals,
				or(
					not(exists("taken")),
					eq("taken", false)
				)
			
			),
			Updates.combine(
				Updates.set("taken", true),
				Updates.set("takenAt", ZonedDateTime.now()),
				Updates.set("takenBy", identity)
			)
		);
		
		if (old != null) {
			// success ...
			// Update the information
			log.debug("Got lock for {} on mutex (showing old data) {}", identity, old);
			log.info("Acquired lock for {} on mutex {}", identity, mutexId);
			return true;
		} else {
			log.debug("Failed to reserve Mutex {} for {}", mutexId, identity);
			
			InstanceMutex lockedMutex = this.getTypedCollection().find(mutexIdEquals).first();
			log.trace("Locked mutex: {}", lockedMutex);
			
			if (lockedMutex == null) {
				throw new IllegalStateException("Mutex was not registered before usage: " + mutexId);
			} else if (lockedMutex.getTakenAt() != null && ZonedDateTime.now().isAfter(lockedMutex.getTakenAt().plus(this.getMutexConfig().lockExpireDuration()))) {
				this.getTypedCollection().findOneAndUpdate(mutexIdEquals, Updates.set("taken", false));
				log.warn("Unlocked mutex that appeared deadlocked: {}", mutexId);
			} else {
				log.trace("Was locked. returning.");
			}
			
			return false;
		}
	}
	
	/**
	 * Convenience wrapper for {@link #lock(String, Optional)}
	 *
	 * @param mutexId
	 *
	 * @return
	 */
	public boolean lock(@NonNull String mutexId) {
		return this.lock(mutexId, Optional.empty());
	}
	
	/**
	 * Awaits for the lock to be acquired via looping with calls to {@link #lock(String, Optional)} until lock acquired or timeout.
	 *
	 * WILL block the thread, waiting/sleeping in the loop between calls to lock.
	 *
	 * @param mutexId The id of the mutex to get the lock for
	 * @param additionalIdentity If an additional identity is required. Only use if need the mutex to offer concurrency within the same instance of the app.
	 * @throws MutexWaitTimeoutException If the lock is not acquired within the timeout period.
	 * @throws InterruptedException If the thread is interrupted while waiting for the lock.
	 */
	public void awaitLock(@NonNull String mutexId, Optional<String> additionalIdentity) throws MutexWaitTimeoutException, InterruptedException {
		Duration toWait = this.getMutexConfig().await().timeout();
		LocalDateTime expires = LocalDateTime.now().plus(toWait);
		long maitTimeMin = this.getMutexConfig().await().loopPauseMin().toMillis();
		long maitTimeMax = this.getMutexConfig().await().loopPauseMax().toMillis();
		Random random = new Random();
		
		//			this.getMutexService().register(this.getMutexId());
		
		log.debug("Awaiting lock for {} with timeout of {}. Max wait time: {}. Min wait time: {}", mutexId, toWait, maitTimeMax, maitTimeMin);
		
		do {
			boolean result = this.lock(mutexId, additionalIdentity);
			
			if (result) {
				return;
			}
			
			Thread.sleep(
				random.nextLong(maitTimeMin, maitTimeMax)
			);
		} while (LocalDateTime.now().isBefore(expires));
		throw new MutexWaitTimeoutException();
	}
	
	/**
	 * Free a mutex previously reserved.
	 *
	 * @param mutexId The id of the mutex to free
	 * @return true if freed, false if not.
	 * @param additionalIdentity If needed to mutex threads inside the same service, provide this
	 */
	public boolean free(@NonNull String mutexId, Optional<String> additionalIdentity) {
		String identity = this.getIdentity(additionalIdentity);
		
		InstanceMutex mutex = this.getTypedCollection().findOneAndUpdate(
			and(
				eq("mutexId", mutexId),
				eq("taken", true),
				eq("takenBy", identity)
			),
			Updates.combine(
				Updates.set("taken", false),
				Updates.set("takenAt", null),
				Updates.set("takenBy", null)
			)
		);
		
		if (mutex == null) {
			log.warn("Mutex NOT freed. Either not taken or not taken by this identity. Mutex: {}", mutex);
			return false;
		} else {
			log.info("Mutex FREED: {}", mutexId);
			return true;
		}
	}
	
	/**
	 * Convenience wrapper for {@link #free(String, Optional)}
	 * @param mutexId The id of the mutex to free
	 * @return true if freed, false if not.
	 */
	public boolean free(@NonNull String mutexId) {
		return this.free(mutexId, Optional.empty());
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return InstanceMutex.CUR_SCHEMA_VERSION;
	}
	
	/**
	 * Gets a new resource to use for locking in try-with-resources.
	 *
	 * @param startLocked if to start the lock immediately. If true, awaits for the lock to be acquired before returning.
	 * @param mutexId the id of the mutex to use
	 * @param additionalId an additional identity to use if needed
	 *
	 * @return The resource to use in a try-with-resources block.
	 * @throws MutexWaitTimeoutException If set to start locked and the lock is not acquired within the timeout period.
	 * @throws InterruptedException If the thread is interrupted while waiting for the lock.
	 */
	public InstanceMutexResource getResource(boolean startLocked, String mutexId, Optional<String> additionalId) throws MutexWaitTimeoutException, InterruptedException {
		InstanceMutexResource resource = InstanceMutexResource.builder()
											 .mutexService(this)
											 .mutexId(mutexId)
											 .additionalIdentity(additionalId)
											 .build();
		if (startLocked) {
			resource.awaitLock();
		}
		
		return resource;
	}
	
	/**
	 * Convenience wrapper for {@link #getResource(boolean, String, Optional)} with startLocked set to true.
	 *
	 * @param mutexId the id of the mutex to use
	 * @param additionalId an additional identity to use if needed
	 *
	 * @return The resource to use in a try-with-resources block.
	 * @throws MutexWaitTimeoutException If the lock is not acquired within the timeout period.
	 * @throws InterruptedException If the thread is interrupted while waiting for the lock.
	 */
	public InstanceMutexResource getResource(String mutexId, Optional<String> additionalId) throws MutexWaitTimeoutException, InterruptedException {
		return this.getResource(true, mutexId, additionalId);
	}
	
	
	/**
	 * Class to enable usage in a try-with-resources.
	 */
	@Data
	@Builder
	public static class InstanceMutexResource implements Closeable {
		
		@NonNull
		@NotNull
		private InstanceMutexService mutexService;
		
		@NonNull
		@NotNull
		@NotBlank
		private String mutexId;
		
		@NonNull
		@NotNull
		@lombok.Builder.Default
		private Optional<String> additionalIdentity = Optional.empty();
		
		/**
		 * Attempts to lock the mutex.
		 * <p>
		 * Wrapper for {@link #InstanceMutexService#lock(String, Optional)}
		 *
		 * @return If the lock was acquired or not
		 */
		public boolean lock() {
			return this.mutexService.lock(this.getMutexId(), this.getAdditionalIdentity());
		}
		
		/**
		 * Awaits for the lock to be acquired before returning this object.
		 * <p>
		 * Wrapper for {@link #InstanceMutexService#awaitLock(String, Optional)}
		 *
		 * @throws MutexWaitTimeoutException If the lock is not acquired within the timeout period.
		 * @throws InterruptedException If the thread is interrupted while waiting for the lock.
		 */
		public void awaitLock() throws MutexWaitTimeoutException, InterruptedException {
			this.mutexService.awaitLock(this.getMutexId(), this.getAdditionalIdentity());
		}
		
		/**
		 * Frees the lock.
		 */
		@Override
		public void close() {
			this.getMutexService().free(this.getMutexId(), this.getAdditionalIdentity());
		}
	}
}
