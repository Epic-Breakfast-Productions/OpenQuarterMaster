const StoredTypeUtils = {
	foreachStoredType(
		storedType,
		whenAmountSimple,
		whenAmountList,
		whenTracked
	) {
		if (storedType === "AMOUNT_SIMPLE") {
			if (whenAmountSimple !== null) {
				return whenAmountSimple();
			}
		} else if (storedType === "AMOUNT_LIST") {
			if (whenAmountList !== null) {
				return whenAmountList();
			}
		} else if (storedType === "TRACKED") {
			if (whenTracked !== null) {
				return whenTracked();
			}
		}
	},
	storedTypeToDisplay(storedType){
		if (storedType === "AMOUNT_SIMPLE") {
			return "Amount Simple";
		} else if (storedType === "AMOUNT_LIST") {
			return "Amount List";
		} else if (storedType === "TRACKED") {
			return "Tracked";
		}
		return "FAIL";
	}
};
