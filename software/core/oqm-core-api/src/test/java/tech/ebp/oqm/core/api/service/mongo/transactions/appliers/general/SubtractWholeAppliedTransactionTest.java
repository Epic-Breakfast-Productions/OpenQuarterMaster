package tech.ebp.oqm.core.api.service.mongo.transactions.appliers.general;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.KafkaCompanionResource;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.service.mongo.transactions.appliers.AppliedTransactionServiceTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
@QuarkusTestResource(value = KafkaCompanionResource.class, restrictToAnnotatedClass = true)
public class SubtractWholeAppliedTransactionTest extends AppliedTransactionServiceTest {
	//TODO:: Success - bulk
	//TODO:: Success - amt list
	//TODO:: Success - unique list
	//TODO:: Success - unique single
	//TODO:: fail - mismatched item/stored/block
	//TODO:: any more?
}
