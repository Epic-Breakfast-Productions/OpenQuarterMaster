package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.appliedTransaction;

import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;

import java.util.TreeSet;

@Slf4j
public class AppliedTransactionSchemaUpgrader extends ObjectSchemaUpgrader<AppliedTransaction> {

	public AppliedTransactionSchemaUpgrader() {
		super(
			AppliedTransaction.class,
			new TreeSet<>()
		);
	}
}
