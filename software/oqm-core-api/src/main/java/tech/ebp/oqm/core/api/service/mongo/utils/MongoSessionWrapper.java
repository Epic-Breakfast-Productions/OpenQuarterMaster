package tech.ebp.oqm.core.api.service.mongo.utils;

import com.mongodb.client.ClientSession;
import jakarta.annotation.Nullable;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.service.mongo.MongoService;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Slf4j
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class MongoSessionWrapper implements Closeable {

	@NonNull
	private ClientSession clientSession;
	/** If the client session was originally provided. */
	private boolean provided;

	public MongoSessionWrapper(@Nullable ClientSession csOptional, @NonNull MongoService<?, ?, ?> service) {
		this(
			csOptional != null ?
				csOptional :
				service.getNewClientSession()
			,
			csOptional != null
		);
	}

	public MongoSessionWrapper(MongoService<?, ?, ?> service) {
		this(service.getNewClientSession(), false);
	}

	public void runTransaction(boolean commit, Consumer<ClientSession> runnable) {
		if (!this.clientSession.hasActiveTransaction()) {
			this.clientSession.startTransaction();
		}

		try {
			runnable.accept(this.clientSession);
		} catch (Exception e) {
			this.clientSession.abortTransaction();
			throw e;
		}
		if (commit) {
			this.clientSession.commitTransaction();
		}
	}

	public void runTransaction(Consumer<ClientSession> runnable) {
		this.runTransaction(!this.provided, runnable);
	}

	public <T> T runTransaction(boolean commit, Callable<T> runnable) throws Exception {
		if (!this.clientSession.hasActiveTransaction()) {
			this.clientSession.startTransaction();
		}
		T output;
		try {
			output = runnable.call();
		} catch (Exception e) {
			log.warn("Error executing transaction", e);
			if(commit) {
				this.clientSession.abortTransaction();
			}
			throw e;
		}
		if (commit) {
			this.clientSession.commitTransaction();
		}
		return output;
	}

	public <T> T runTransaction(Callable<T> runnable) throws Exception {
		return this.runTransaction(!this.provided, runnable);
	}

	@Override
	public void close() {
		if(!provided) {
			this.clientSession.close();
		}
	}
}
