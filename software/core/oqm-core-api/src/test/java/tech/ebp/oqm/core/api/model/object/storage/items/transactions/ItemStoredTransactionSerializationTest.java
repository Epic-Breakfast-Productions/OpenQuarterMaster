package tech.ebp.oqm.core.api.model.object.storage.items.transactions;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckoutDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.ReturnFullCheckinDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.ReturnPartCheckinDetails;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.checkedInBy.CheckedInBy;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.checkedInBy.CheckedInByOqmEntity;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutFor;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkoutFor.CheckoutForOqmEntity;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.GenericLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StorageBlockLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.StoredLocale;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.add.AddAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.add.AddWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkin.CheckinFullTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkin.CheckinPartTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkout.CheckoutAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.checkouts.checkout.CheckoutWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddAmountInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.add.AddWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.cancel.CancelInTransitWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.cancel.CancelType;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveAmountInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.receive.ReceiveWholeInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit.StoredToInTransitAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.storedToInTransit.StoredToInTransitWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.inTransit.update.UpdateInTransitTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.set.SetAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.subtract.SubAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.subtract.SubWholeTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.transfer.TransferAmountTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.transactions.general.transfer.TransferWholeTransaction;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
class ItemStoredTransactionSerializationTest extends ObjectSerializationTest<ItemStoredTransaction> {


	protected ItemStoredTransactionSerializationTest() {
		super(ItemStoredTransaction.class);
	}

	public static Stream<Arguments> getObjects() {
		Stored stored = UniqueStored.builder().item(ObjectId.get()).build();

		CheckedInBy checkinBy = CheckedInByOqmEntity.builder().entity(ObjectId.get()).build();
		CheckoutFor checkoutFor = CheckoutForOqmEntity.builder().entity(ObjectId.get()).build();

		InTransit inTransit = InTransit.builder()
			.from(GenericLocale.builder().name("foo").build())
			.build();



		return Stream.of(

			/*
			 * "Regular" Transactions
			 */

			// add amount transaction
			Arguments.of(AddAmountTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .build()),
			Arguments.of(AddAmountTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .toBlock(ObjectId.get())
							 .toStored(ObjectId.get())
							 .build()),

			//add whole transaction
			Arguments.of(AddWholeTransaction.builder()
							 .toAdd(stored)
							 .toBlock(ObjectId.get())
							 .build()),

			//checkin full transaction
			Arguments.of(CheckinFullTransaction.builder()
							 .details(ReturnFullCheckinDetails.builder().checkedInBy(checkinBy).build())
							 .build()),
			//checkin loss transaction TODO:: not implemented yet
//			Arguments.of(CheckinLossTransaction.builder()
//							 .details(ReturnPartCheckinDetails.builder().build())
//							 .build()),
			//checkin part transaction
			Arguments.of(CheckinPartTransaction.builder()
							 .details(ReturnPartCheckinDetails.builder().checkedInBy(checkinBy).build())
							 .build()),

			//checkout amount transaction
			Arguments.of(CheckoutAmountTransaction.builder()
							 .all(false)
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .checkoutDetails(CheckoutDetails.builder().checkedOutFor(checkoutFor).build())
							 .build()),
			Arguments.of(CheckoutAmountTransaction.builder()
							 .all(true)
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .checkoutDetails(CheckoutDetails.builder().checkedOutFor(checkoutFor).build())
							 .fromBlock(ObjectId.get())
							 .fromStored(ObjectId.get())
							 .build()),
			//checkout whole transaction
			Arguments.of(CheckoutWholeTransaction.builder()
							 .toCheckout(ObjectId.get())
							 .checkoutDetails(CheckoutDetails.builder().checkedOutFor(checkoutFor).build())
							 .build()),

			//set amount transaction
			Arguments.of(SetAmountTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .build()),
			Arguments.of(SetAmountTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .block(ObjectId.get())
							 .stored(ObjectId.get())
							 .build()),

			//subtract amount transaction
			Arguments.of(SubAmountTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .build()),
			Arguments.of(SubAmountTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .fromBlock(ObjectId.get())
							 .fromStored(ObjectId.get())
							 .build()),

			//subtract whole transaction
			Arguments.of(SubWholeTransaction.builder()
							 .toSubtract(ObjectId.get())
							 .build()),

			//transfer amount transaction
			Arguments.of(TransferAmountTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .build()),
			Arguments.of(TransferAmountTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .fromBlock(ObjectId.get())
							 .fromStored(ObjectId.get())
							 .toBlock(ObjectId.get())
							 .toStored(ObjectId.get())
							 .build()),

			//subtract whole transaction
			Arguments.of(TransferWholeTransaction.builder()
							 .storedToTransfer(ObjectId.get())
							 .toBlock(ObjectId.get())
							 .build()),
			Arguments.of(TransferWholeTransaction.builder()
							 .fromBlock(ObjectId.get())
							 .toBlock(ObjectId.get())
							 .build()),


			/*
			 * In Transit Transactions
			 */

			//add amount in transit transaction
			Arguments.of(AddAmountInTransitTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .details(inTransit)
							 .build()),
			//add whole in transit transaction
			Arguments.of(AddWholeInTransitTransaction.builder()
							 .toAdd(stored)
							 .details(inTransit)
							 .build()),

			//cancel whole in transit transaction
			Arguments.of(CancelInTransitWholeTransaction.builder()
							 .cancelType(CancelType.CANCEL)
							 .inTransitStored(ObjectId.get())
							 .build()),

			//receive amount in transit transaction
			Arguments.of(ReceiveAmountInTransitTransaction.builder()
							 .all(true)
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .inTransitStored(ObjectId.get())
							 .build()),
			Arguments.of(ReceiveAmountInTransitTransaction.builder()
							 .all(true)
							 .inTransitStored(ObjectId.get())
							 .build()),
			Arguments.of(ReceiveAmountInTransitTransaction.builder()
							 .all(true)
							 .toStored(ObjectId.get())
							 .toBlock(ObjectId.get())
							 .inTransitStored(ObjectId.get())
							 .build()),

			//receive whole in transit transaction
			Arguments.of(ReceiveWholeInTransitTransaction.builder()
							 .toBlock(ObjectId.get())
							 .inTransitStored(ObjectId.get())
							 .build()),
			Arguments.of(ReceiveWholeInTransitTransaction.builder()
							 .inTransitStored(ObjectId.get())
							 .build()),

			//stored to in transit amount transaction
			Arguments.of(StoredToInTransitAmountTransaction.builder()
							 .build()),
			Arguments.of(StoredToInTransitAmountTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .all(true)
							 .toInTransitStored(ObjectId.get())
							 .subtractFromStored(ObjectId.get())
							 .build()),

			//stored to in transit whole transaction
			Arguments.of(StoredToInTransitWholeTransaction.builder()
							 .toTransit(ObjectId.get())
							 .build()),

			//update in transit whole transaction
			Arguments.of(UpdateInTransitTransaction.builder()
							 .inTransitStored(ObjectId.get())
							 .inTransitUpdates(inTransit)
							 .build()),

			//in transit / details tests
			Arguments.of(AddAmountInTransitTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .details(InTransit.builder()
										  .from(GenericLocale.builder().name("foo").build())
										  .build())
							 .build()),
			Arguments.of(AddAmountInTransitTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .details(InTransit.builder()
										  .from(GenericLocale.builder()
													.name("foo")
													.description("foo bar")
													.build())
										  .build())
							 .build()),
			Arguments.of(AddAmountInTransitTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .details(InTransit.builder()
										  .attachedFiles(Set.of(ObjectId.get()))
										  .imageIds(List.of(ObjectId.get()))
										  .keywords(List.of("foo"))
										  .attributes(Map.of("foo", "bar"))
										  .from(GenericLocale.builder().name("foo").build())
										  .to(GenericLocale.builder().name("foo").build())
										  .build())
							 .build()),
			Arguments.of(AddAmountInTransitTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .details(InTransit.builder()
										  .from(StorageBlockLocale.builder().storageBlock(ObjectId.get()).build())
										  .build())
							 .build()),
			Arguments.of(AddAmountInTransitTransaction.builder()
							 .amount(UnitUtils.Quantities.UNIT_ONE)
							 .details(InTransit.builder()
										  .from(StoredLocale.builder().stored(ObjectId.get()).build())
										  .build())
							 .build())

		);
	}

}
