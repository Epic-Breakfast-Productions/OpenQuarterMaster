const StoredView = {
	getBlockViewCell(name, value) {
		let output = $('<div class="col"><h5></h5><p></p></div>');

		output.find("h5").text(name);
		output.find("p").text(value);
		return output;
	},
	getStorageBlockTrackedIdentifierView(stored) {
		if (stored.storedType.includes("TRACKED")) {
			return StoredView.getBlockViewCell("Identifier", stored.identifier);
		}
		return "";
	},
	getStorageBlockAmountHeldView(stored, showCurrently = false) {
		if (stored.storedType.includes("AMOUNT")) {
			return StoredView.getBlockViewCell((showCurrently?"Currently ":"") + "Stored:", stored.amount.value + stored.amount.unit.symbol);
		}
		return "";
	},
	getStorageBlockBarcodeView(stored, itemId, storageBlockId, index = false) {
		if (stored.barcode) {
			let url = "/api/media/code/item/" + itemId + "/barcode/stored/" + storageBlockId;

			if (index !== false) {
				url += "/" + index;
			}

			return '<div class="col"><h5>Barcode:</h5><img src="' + url + '" title="Stored item barcode" alt="Stored item barcode" class="barcodeViewImg"></div>';
		}
		return "";
	},
	getStorageBlockIdentifyingDetailsView(stored) {
		if (stored.storedType.includes("TRACKED") && stored.identifyingDetails) {
			return StoredView.getBlockViewCell("Identifying Details", stored.identifyingDetails);
		}
		return "";
	},
	getStorageBlockConditionView(stored) {
		if (stored.condition) {
			return StoredView.getBlockViewCell("Condition", stored.condition + "%");
		}
		return "";
	},
	getStorageBlockConditionNotesView(stored) {
		if (stored.conditionNotes) {
			return StoredView.getBlockViewCell("Condition Notes", stored.conditionNotes);
		}
		return "";
	},
	getStorageBlockExpiresView(stored) {
		if (stored.expires) {
			return StoredView.getBlockViewCell("Expires", stored.expires);
		}
		return "";
	},
	getStoredBlockLink(storageBlockId, small = false) {
		let output = $('<div class=""></div>');
		output.html(Links.getStorageViewButton(storageBlockId, 'View in Storage'));

		if (small) {
			output.addClass("col-1");
		} else {
			output.addClass("col");
		}

		return output;
	},

	getCheckoutBlockLink(stored, itemId, storageId, small = false) {
		let output = $('<div class=""></div>');

		console.log("Creating checkout link. Item: " + itemId + " Block: " + storageId + " Stored: " + JSON.stringify(stored))

		let checkoutButton = $('<button type=button class="btn btn-warning" data-bs-toggle="modal" data-bs-target="#itemCheckoutModal"></button>');
		let setupCheckoutFunc = function () {
			ItemCheckout.setupCheckoutItemModal(stored, itemId, storageId);
		}
		checkoutButton = checkoutButton.on("click", setupCheckoutFunc);
		checkoutButton.append(Icons.itemCheckout + "Checkout");
		output.append(checkoutButton);

		if (small) {
			output.addClass("col-1");
		} else {
			output.addClass("col");
		}

		return output;
	},
	getStoredViewContent(
		stored,
		itemId,
		storageBlockId,
		index = false,
		includeStoredLink = false,
		includeCheckoutLink = false,
		includeIdentifier = false,
		showCurrentlyStored = false
	) {
		console.log("Getting stored view html for "+JSON.stringify(stored))
		let newContent = $('<div class="row storedViewRow"></div>');

		if (includeStoredLink) {
			newContent.append(
				StoredView.getStoredBlockLink(storageBlockId, true)
			);
		}

		if (includeCheckoutLink) {
			newContent.append(
				StoredView.getCheckoutBlockLink(stored, itemId, storageBlockId, true)
			);
		}

		if (includeIdentifier) {
			newContent.append(
				StoredView.getStorageBlockTrackedIdentifierView(stored)
			);
		}

		newContent.append(
			StoredView.getStorageBlockAmountHeldView(stored, showCurrentlyStored),
			StoredView.getStorageBlockBarcodeView(stored, itemId, storageBlockId, index),
			StoredView.getStorageBlockIdentifyingDetailsView(stored),
			StoredView.getStorageBlockConditionView(stored),
			StoredView.getStorageBlockConditionNotesView(stored),
			StoredView.getStorageBlockExpiresView(stored),
		);
		//TODO:: images, keywords, atts

		return newContent;
	},
};