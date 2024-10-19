const StoredTypeUtils = {
    types: ["AMOUNT", "UNIQUE"],
    runForType(
        storedType,
        whenAmount,
        whenUnique,
    ) {
        if (storedType === "AMOUNT") {
            if (whenAmount !== null) {
                return whenAmount();
            }
        } else if (storedType === "UNIQUE") {
            if (whenUnique !== null) {
                return whenUnique();
            }
        }
    },
    typeToDisplay(storedType) {
        if (storedType === "AMOUNT") {
            return "Amount";
        } else if (storedType === "UNIQUE") {
            return "Unique";
        }
        return "FAIL";
    }
};

const StorageTypeUtils = {
    types: ["BULK", "AMOUNT_LIST", "UNIQUE_MULTI", "UNIQUE_SINGLE"],
    runForType(
        storedType,
        whenBulk,
        whenAmountList,
        whenUniqueMulti,
        whenUniqueSingle
    ) {
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
        }
    },
    runForStoredType(
        storedType,
        whenAmount,
        whenUnique,
    ) {
        if (storedType === "BULK" || storedType === "AMOUNT_LIST") {
            if (whenAmount !== null) {
                return whenAmount();
            }
        } else if (storedType === "UNIQUE_MULTI" || storedType === "UNIQUE_SINGLE") {
            if (whenUnique !== null) {
                return whenUnique();
            }
        }
    },
    typeToDisplay(storedType) {
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
};