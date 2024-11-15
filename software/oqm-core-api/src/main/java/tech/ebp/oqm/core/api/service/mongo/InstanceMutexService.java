package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.InstanceMutex;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.rest.search.InstanceMutexSearch;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mongodb.client.model.Filters.*;

/**
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
		List<InstanceMutex> mutexes = this.getCollection().find(eq("mutexId", mutexId)).into(new ArrayList<>());
		if (mutexes.size() > 1) {
			log.info("Multiple mutex objects for {} detected ({} total mutexes). Deleting extra.", mutexId, mutexes.size());
			mutexes.removeFirst();

			for (InstanceMutex mutex : mutexes) {
				this.getCollection().deleteOne(eq("_id", mutex.getId()));
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
		List<InstanceMutex> existingMutexes = this.getCollection().find(eq("mutexId", mutexId)).into(new ArrayList<>());

		log.info("Existent mutexes: {}", existingMutexes);

		if (existingMutexes.isEmpty()) {
			// create it assuming no collision at this point
			InstanceMutex newMutex = new InstanceMutex(mutexId);
			log.info("Creating new mutex {}", newMutex);

			UpdateResult result = this.getCollection().updateOne(
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
		InstanceMutex old = this.getCollection().findOneAndUpdate(
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

			InstanceMutex lockedMutex = this.getCollection().find(mutexIdEquals).first();
			log.debug("Locked mutex: {}", lockedMutex);

			if (lockedMutex == null) {
				log.warn("No mutex found. It needs to be registered first.");
			} else if (lockedMutex.getTakenAt() != null && ZonedDateTime.now().isAfter(lockedMutex.getTakenAt().plus(this.lockExpire))) {
				this.getCollection().findOneAndUpdate(mutexIdEquals, Updates.set("taken", false));
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
	 * @param mutexId The id of the mutex to free
	 */
	public void free(@NonNull String mutexId, Optional<String> additionalIdentity) {
		String identity = this.getIdentity(additionalIdentity);

		InstanceMutex mutex = this.getCollection().findOneAndUpdate(
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
}
