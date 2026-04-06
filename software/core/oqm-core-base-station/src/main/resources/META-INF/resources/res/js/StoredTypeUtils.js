export class StoredTypeUtils {
	static types= ["AMOUNT", "UNIQUE"];
	static typeFromStored(stored) {
		return stored.type;
	}
	static runForType(
		storedType,
		whenAmount,
		whenUnique,
	) {
		if (typeof storedType !== "string" && !(storedType instanceof String)) {
			storedType = this.typeFromStored(storedType);
		}

		if (storedType === "AMOUNT") {
			if (whenAmount !== null) {
				return whenAmount();
			}
		} else if (storedType === "UNIQUE") {
			if (whenUnique !== null) {
				return whenUnique();
			}
		}
	}
	static typeToDisplay(storedType) {
		if (storedType === "AMOUNT") {
			return "Amount";
		} else if (storedType === "UNIQUE") {
			return "Unique";
		}
		return "FAIL";
	}
}

export class StorageTypeUtils {
	static types = ["BULK", "AMOUNT_LIST", "UNIQUE_MULTI", "UNIQUE_SINGLE"];
	static runForType(
		storedType,
		whenBulk,
		whenAmountList,
		whenUniqueMulti,
		whenUniqueSingle
	) {
		//If item data, get from data
		if (typeof storedType !== "string" && !(storedType instanceof String)) {
			console.debug("Getting storage type from item: ", storedType);
			storedType = storedType.storageType;
			console.debug("Got storage type from item: ", storedType);
		}

		if (storedType === "BULK") {
			if (whenBulk !== null) {
				return whenBulk();
			}
		} else if (storedType === "AMOUNT_LIST") {
			if (whenAmountList !== null) {
				return whenAmountList();
			}
		} else if (storedType === "UNIQUE_MULTI") {
			if (whenUniqueMulti !== null) {
				return whenUniqueMulti();
			}
		} else if (storedType === "UNIQUE_SINGLE") {
			if (whenUniqueSingle !== null) {
				return whenUniqueSingle();
			}
		} else {
			console.warn("Storage type was not valid; ", storedType);
		}
	}
	static runForStoredType(
		storedType,
		whenAmount,
		whenUnique,
	) {
		this.runForType(storedType,
			whenAmount,
			whenAmount,
			whenUnique,
			whenUnique
		);
	}
	static typeToDisplay(storedType) {
		if (storedType === "BULK") {
			return "Bulk"
		} else if (storedType === "AMOUNT_LIST") {
			return "Amount List"
		} else if (storedType === "UNIQUE_MULTI") {
			return "Unique - Multiple"
		} else if (storedType === "UNIQUE_SINGLE") {
			return "Unique - Single"
		}
		return "FAIL";
	}
	static storageToStoredType(itemStorageType) {
		if (typeof itemStorageType !== "string" && !(itemStorageType instanceof String)) {
			itemStorageType = itemStorageType.storageType;
		}
		switch (itemStorageType) {
			case "BULK":
			case "AMOUNT_LIST":
				return "AMOUNT";
			case "UNIQUE_MULTI":
			case "UNIQUE_SINGLE":
				return "UNIQUE";
		}
		console.warn("item storage type not mappable to stored type: ", itemStorageType);
	}
}

export class CheckoutTypeUtils {
	static types= ["AMOUNT", "WHOLE"];

	static typeFromCheckout(checkout) {
		return checkout.type;
	}
	static runForType(
		type,
		whenAmount,
		whenWhole,
	) {
		if (typeof type !== "string" && !(type instanceof String)) {
			console.debug("Getting checkout type from checkout: ", type);
			type = CheckoutTypeUtils.typeFromCheckout(type);
			console.debug("Got storage type from item: ", type);
		}

		switch (type) {
			case "AMOUNT":
				if (whenAmount !== null) {
					return whenAmount();
				}
				break;
			case "WHOLE":
				if (whenWhole !== null) {
					return whenWhole();
				}
				break;
			default:
				console.warn("Storage type was not valid; ", storedType);
				break;
		}
	}
}