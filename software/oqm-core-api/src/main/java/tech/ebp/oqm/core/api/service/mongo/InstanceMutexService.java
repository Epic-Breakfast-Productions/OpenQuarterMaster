package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.exception.MutexWaitTimeoutException;
import tech.ebp.oqm.core.api.model.InstanceMutex;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.rest.search.InstanceMutexSearch;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.*;

/**
 * Provides an interface through which to provide mutex functionality between instances of the core api.
 * <p>
 * Use:
 * <ol>
 *     <li>Call {@link #register(String)} to ensure mutex exists.</li>
 *     <li>Call {@link #lock(String, Optional)} to grab the lock.</li>
 *     <li>Call {@link #free(String, Optional)} to free the lock.</li>
 * </ol>
 * <p>
 * Original inspiration: https://www.disk91.com/2018/technology/programming/springboot-mongo-create-a-mutex-to-synchronize-multiple-frontends/
 */
@ApplicationScoped
@Slf4j
public class InstanceMutexService extends TopLevelMongoService<InstanceMutex, InstanceMutexSearch, CollectionStats> {

	private TemporalAmount lockExpire = Duration.of(10, ChronoUnit.MINUTES);

	@ConfigProperty(name = "quarkus.uuid")
	protected String instanceUuid;

	protected InstanceMutexService() {
		super(InstanceMutex.class);
	}

	protected String getIdentity(Optional<String> additionalIdentity) {
		return this.instanceUuid + additionalIdentity.map(s -> "-" + s).orElse("");
	}

	private void clearDuplicateMutexes(String mutexId) {
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

		log.info("Existent mutexes: {}", existingMutexes);

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
	 * @param mutexId            The is of the mutex to get the lock for
	 * @param additionalIdentity If an additional identity is required. Only use if need the mutex to offer concurrency within the same instance of the app.
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
			log.info("Failed to reserve Mutex {} for {}", mutexId, identity);

			InstanceMutex lockedMutex = this.getTypedCollection().find(mutexIdEquals).first();
			log.debug("Locked mutex: {}", lockedMutex);

			if (lockedMutex == null) {
				log.warn("No mutex found. It needs to be registered first.");
			} else if (lockedMutex.getTakenAt() != null && ZonedDateTime.now().isAfter(lockedMutex.getTakenAt().plus(this.lockExpire))) {
				this.getTypedCollection().findOneAndUpdate(mutexIdEquals, Updates.set("taken", false));
				log.warn("Unlocked mutex that appeared deadlocked: {}", mutexId);
			} else {
				log.debug("Was locked. returning.");
			}

			return false;
		}
	}

	public boolean lock(@NonNull String mutexId) {
		return this.lock(mutexId, Optional.empty());
	}

	/**
	 * Free a mutex previously reserved.
	 *
	 * @param mutexId            The id of the mutex to free
	 * @param additionalIdentity If needed to mutex threads inside the same service, provide this
	 */
	public void free(@NonNull String mutexId, Optional<String> additionalIdentity) {
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
			log.info("Mutex NOT freed. Either not taken or not taken by this identity. Mutex: {}", mutex);
		} else {
			log.info("Mutex FREED: {}", mutexId);
		}
	}

	public void free(@NonNull String mutexId) {
		this.free(mutexId, Optional.empty());
	}

	/**
	 * Class to enable usage in a try-with-resources.
	 * <p>
	 * TODO:: test
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

		@NonNull
		@NotNull
		@lombok.Builder.Default
		private Optional<Duration> timeToWait = Optional.empty();

		/**
		 * Awaits for the lock to be acquired before returning this object.
		 *
		 * @return This object when the lock is acquired.
		 */
		public InstanceMutexResource awaitLock() throws MutexWaitTimeoutException, InterruptedException {
			Duration toWait = this.getTimeToWait().orElse(Duration.ofHours(1));
			LocalDateTime expires = LocalDateTime.now().plus(toWait);

			this.getMutexService().register(this.getMutexId());

			do {
				boolean result = this.getMutexService().lock(this.getMutexId(), this.getAdditionalIdentity());

				if (result) {
					return this;
				}
				Thread.sleep(50);//TODO:: make configurable wait time
			} while (LocalDateTime.now().isBefore(expires));
			throw new MutexWaitTimeoutException();
		}

		@Override
		public void close() throws IOException {
			this.getMutexService().free(this.getMutexId(), this.getAdditionalIdentity());
		}
	}
}
