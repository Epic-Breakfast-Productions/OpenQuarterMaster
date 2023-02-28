const StoredTypeUtils = {
	foreachStoredType(
		storedType,
		whenAmountSimple,
		whenAmountList,
		whenTracked
	) {
		if (storedType === "AMOUNT_SIMPLE") {
			if (whenAmountSimple !== null) {
				whenAmountSimple();
			}
		} else if (storedType === "AMOUNT_LIST") {
			if (whenAmountList !== null) {
				whenAmountList();
			}
		} else if (storedType === "TRACKED") {
			if (whenTracked !== null) {
				whenTracked();
			}
		}
	}
};
