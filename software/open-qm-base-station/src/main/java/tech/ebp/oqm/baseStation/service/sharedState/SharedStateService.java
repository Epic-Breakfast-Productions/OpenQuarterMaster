package tech.ebp.oqm.baseStation.service.sharedState;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Request;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SharedStateService {
	
	@Inject
	ReactiveRedisDataSource reactive;
	
	public void test(){
		// what to do to set and/or get to ensure multiple base station implementations can't run the expiry check at once?
//		reactive.getRedis().batchAndAwait(
//				Request.cmd(Command.SETNX, "")
//			)
//
//
//			.execute(
//			Command.MULTI,
//			"SETNX",
//			""
//		);
	}
	
	
	
	
}
