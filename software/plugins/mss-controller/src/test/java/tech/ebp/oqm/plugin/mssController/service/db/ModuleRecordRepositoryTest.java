package tech.ebp.oqm.plugin.mssController.service.db;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.plugin.mssController.model.moduleComm.moduleRecord.ModuleRecord;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
class ModuleRecordRepositoryTest {

	@Inject
	ModuleRecordRepository mrr;

	@Test
	@TestTransaction
	public void testCreate(){

		ModuleRecord r = ModuleRecord.builder()
			.serialId(UUID.randomUUID().toString())
			.build();

		this.mrr.persist(r);

		assertEquals(r, this.mrr.findById(r.getSerialId()));
	}
}
