package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.checkout.bumpers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.AccessLevel;
import lombok.Getter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.core.api.model.object.storage.checkout.ItemCheckout;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.AppliedTransaction;
import tech.ebp.oqm.core.api.model.object.storage.items.transactions.ItemStoredTransaction;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaVersionBumper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.UpgradingUtils;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.bumpers.InvItemBumper2;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.StoredSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.bumpers.StoredItemBumper2;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.bumpers.StoredItemBumper3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static tech.ebp.oqm.core.api.model.object.ObjectUtils.OBJECT_MAPPER;

public class CheckoutBumper2 extends ObjectSchemaVersionBumper<ItemCheckout> {
	
	@Getter(AccessLevel.PRIVATE)
	private final InvItemBumper2 invItemBumper = new InvItemBumper2();
	
	@Getter(AccessLevel.PRIVATE)
	private final StoredItemBumper2 storedSchemaUpgrader2 = new StoredItemBumper2();
	@Getter(AccessLevel.PRIVATE)
	private final StoredItemBumper3 storedSchemaUpgrader3 = new StoredItemBumper3();
	
	public CheckoutBumper2() {
		super(2);
	}
	
	private ObjectNode createSpoofProcessResults(TextNode itemId) {
		ObjectNode spoofProcessResults = OBJECT_MAPPER.createObjectNode();
		
		ObjectNode expiryLowStockResults = spoofProcessResults.putObject("expiryLowStockResults");
		expiryLowStockResults.set("item", itemId);
		
		ObjectNode stats = spoofProcessResults.putObject("stats");
		
		return spoofProcessResults;
	}
	
	private TextNode createCheckoutTransaction(UpgradeCreatedObjectsResults cor, ObjectNode checkout) {
		List<ObjectNode> transactionsCreated = cor.getOrDefault(AppliedTransaction.class, new ArrayList<>());
		
		ObjectNode transactionOut = OBJECT_MAPPER.createObjectNode();
		transactionOut.put("type", "CHECKOUT_WHOLE");
		
		transactionOut.set("checkoutDetails", checkout.get("checkOutDetails"));
		transactionOut.set("toCheckout", checkout.get("checkedOut"));
		
		
		ObjectNode appliedTransactionOut = OBJECT_MAPPER.createObjectNode();
		appliedTransactionOut.put("id", new ObjectId().toHexString());
		appliedTransactionOut.set("entity", checkout.get("checkedOutByEntity"));
		appliedTransactionOut.set("inventoryItem", checkout.get("item"));
		appliedTransactionOut.set("timestamp", checkout.get("checkoutDate"));
		appliedTransactionOut.set("transaction", transactionOut);
		appliedTransactionOut.set("postApplyResults", this.createSpoofProcessResults((TextNode) checkout.get("item")));
		
		checkout.set("checkOutTransaction", appliedTransactionOut.get("id"));
		
		return (TextNode) appliedTransactionOut.get("id");
	}
	
	private TextNode createCheckinTransaction(UpgradeCreatedObjectsResults cor, ObjectNode checkout) {
		if (checkout.get("checkInDetails") == null) {
			return null;
		}
		
		List<ObjectNode> transactionsCreated = cor.getOrDefault(AppliedTransaction.class, new ArrayList<>());
		
		ObjectNode transactionIn = OBJECT_MAPPER.createObjectNode();
		transactionIn.put("type", "CHECKIN_FULL");
		
		transactionIn.set("checkoutId", checkout.get("id"));
		transactionIn.set("details", checkout.get("checkInDetails"));
		transactionIn.set("toCheckout", checkout.get("checkedOut"));
		
		
		ObjectNode appliedTransactionIn = OBJECT_MAPPER.createObjectNode();
		appliedTransactionIn.put("id", new ObjectId().toHexString());
		appliedTransactionIn.set("entity", checkout.get("checkedInByEntity"));
		appliedTransactionIn.set("inventoryItem", checkout.get("item"));
		appliedTransactionIn.set("timestamp", checkout.get("checkoutDate"));
		appliedTransactionIn.set("transaction", transactionIn);
		appliedTransactionIn.set("postApplyResults", this.createSpoofProcessResults((TextNode) checkout.get("item")));
		
		checkout.set("checkInTransaction", appliedTransactionIn.get("id"));
		
		return (TextNode) appliedTransactionIn.get("id");
	}
	
	
	@Override
	protected SingleUpgradeResult bumpObjectSchema(ObjectNode oldObj) {
		UpgradeCreatedObjectsResults createdObjectsResults = new UpgradeCreatedObjectsResults();
		SingleUpgradeResult.SingleUpgradeResultBuilder resultBuilder = SingleUpgradeResult.builder()
														.createdObjects(createdObjectsResults)
														.upgradedObject(oldObj);
		ObjectNode checkedOut = (ObjectNode) oldObj.get("checkedOut");
		
		UpgradingUtils.normalizeObjectId(oldObj);
		UpgradingUtils.normalizeObjectId(oldObj, "item");
		UpgradingUtils.normalizeObjectId(oldObj, "checkedOutFrom");
		UpgradingUtils.dequoteString(oldObj, "checkoutDate");
		
		
		//enforcing all these as whole
		oldObj.put("type", "WHOLE");
		
		{
			oldObj.set(
				"checkedOut",
				this.getStoredSchemaUpgrader3().bumpObject(
					this.getStoredSchemaUpgrader2().bumpObject(
						this.getInvItemBumper()
							.adjustStored(oldObj.get("item").asText(), checkedOut, oldObj.get("checkedOutFrom").asText())
					).getUpgradedObject()).getUpgradedObject()
				);
		}
		
		//don't need to do anything with this for whole?
		TextNode fromBlock = (TextNode) oldObj.remove("checkedOutFrom");
		
		//checkout details
		ObjectNode checkoutDetails = oldObj.putObject("checkoutDetails");
		checkoutDetails.set("notes", oldObj.remove("notes"));
		checkoutDetails.set("reason", oldObj.remove("reason"));
		
		//checkedOutFor
		ObjectNode checkedOutFor = (ObjectNode) oldObj.get("checkedOutFor");
		oldObj.remove("checkedOutFor");
		checkoutDetails.set("checkedOutFor", checkedOutFor);
		if (checkedOutFor.has("entity")) {
			UpgradingUtils.normalizeObjectId(checkedOutFor, "entity");
		}
		checkedOutFor.remove("_t");
		if (checkedOutFor.get("type").asText().equals("OQM_ENTITY")) {
			oldObj.put("checkedOutByEntity", checkedOutFor.get("entity").asText());
		} else {
			oldObj.put("checkedOutByEntity", CoreApiInteractingEntity.BS_ID.toHexString());
		}
		
		if (oldObj.has("checkInDetails")) {
			ObjectNode checkinDetails = (ObjectNode) oldObj.get("checkInDetails");
			checkinDetails.remove("_t");
			UpgradingUtils.dequoteString(checkinDetails, "checkinDateTime");
			checkinDetails.set("type", checkinDetails.remove("checkinType"));
			checkinDetails.put("type", "RETURN_FULL");
			checkinDetails.remove("storageBlockCheckedInto");
			checkinDetails.set("checkedInBy", oldObj.get("checkoutDetails").get("checkedOutFor"));
			
			oldObj.set("checkedInByEntity", oldObj.get("checkedOutByEntity"));
		}
		
		oldObj.set("checkOutTransaction", this.createCheckoutTransaction(createdObjectsResults, oldObj));
		oldObj.set("checkInTransaction", this.createCheckinTransaction(createdObjectsResults, oldObj));
		
		
		return resultBuilder.build();
	}
}
