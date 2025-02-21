const StoredView = {
	getBlockViewCell(name, value, classes = "") {
		let output = $('<div class="col-sm-4 col-4 col-xs-6 '+classes+'"><h5></h5><p></p></div>');

		output.find("h5").text(name);
		output.find("p").text(value);
		return output;
	},
	getStorageBlockTrackedIdentifierView(stored) {
		if (stored.type.includes("UNIQUE")) {
			return StoredView.getBlockViewCell("Identifier", stored.identifier);
		}
		return "";
	},
	getStorageBlockAmountHeldView(stored, showCurrently = false) {
		if (stored.type.includes("AMOUNT")) {
			return StoredView.getBlockViewCell((showCurrently ? "Currently " : "") + "Stored:", stored.amount.value + stored.amount.unit.symbol, "storedViewAmount");
		}
		return "";
	},
	getStorageBlockBarcodeView(stored, index = false) {
		//TODO:: rework
		if (stored.barcode) {
			let url = "/api/media/code/item/" + stored.item + "/barcode/stored/" + stored.storageBlock;

			if (index !== false) {
				url += "/" + index;
			}

			return '<div class="col"><h5>Barcode:</h5><img src="' + url + '" title="Stored item barcode" alt="Stored item barcode" class="barcodeViewImg"></div>';
		}
		return "";
	},
	getStorageBlockIdentifyingDetailsView(stored) {
		if (stored.type.includes("TRACKED") && stored.identifyingDetails) {
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
		output.html(Links.getStorageViewButton(storageBlockId, 'View Block'));

		if (small) {
			output.addClass("col-sm-6 col-xs-6 col-md-4 col-lg-2");
		} else {
			output.addClass("col-sm-6 col-xs-6 col-md-4 col-lg-2");
		}

		return output;
	},

	getCheckoutBlockLink(stored, small = false) {
		let output = $('<div class=""></div>');

		console.log("Creating checkout link. Item: " + stored.item + " Block: " + stored.storageBlock + " Stored: ", stored)

		let checkoutButton = $('<button type=button class="btn btn-warning" data-bs-toggle="modal" data-bs-target="#itemCheckoutModal"></button>');
		let setupCheckoutFunc = function () {
			//TODO:: move to transaction
			// ItemCheckout.setupCheckoutItemModal(stored, itemId, storageId);

		}
		checkoutButton = checkoutButton.on("click", setupCheckoutFunc);
		checkoutButton.append(Icons.itemCheckout + "Checkout");
		output.append(checkoutButton);

		if (small) {
			output.addClass("col-sm-6 col-xs-6 col-md-4 col-lg-2");
		} else {
			output.addClass("col-sm-6 col-xs-6 col-md-4 col-lg-2");
		}

		return output;
	},
	//TODO: finish figuring this out
	getTransactBlockLink(stored, small = false,
						 {
							 showAllTransactions = false,
							 showAddTransaction = false,
							 showSubtractTransaction = false,
							 showTransferTransaction = false,
							 showCheckoutTransaction = false,
							 showSetTransaction = false
						 }
	) {
		if (!(
			showAllTransactions || showAddTransaction || showSubtractTransaction ||
			showTransferTransaction || showCheckoutTransaction || showSetTransaction
		)) {
			return "";
		}
		let output = $('<div class=""></div>');

		console.log("Creating checkout link. Item: " + stored.item + " Block: " + stored.storageBlock + " Stored: ", stored)


		output.append(ItemStoredTransaction.ModalUtils.getTransactionSelectDropdown((stored ? stored.item : null),
			stored,
			{
				showAllTransactions: showAllTransactions,
				showAddTransaction: showAddTransaction,
				showSubtractTransaction: showSubtractTransaction,
				showCheckoutTransaction: showCheckoutTransaction,
				showTransferTransaction: showTransferTransaction,
				showSetTransaction: showSetTransaction
			}));

		if (small) {
			output.addClass("col-sm-6 col-xs-6 col-md-4 col-lg-2");
		} else {
			output.addClass("col-sm-6 col-xs-6 col-md-4 col-lg-2");
		}

		return output;
	},
	getStoredViewContent(
		stored,
		{
			index = false,
			includeBlockLink = false,
			includeIdentifier = false,
			showCurrentlyStored = false,
			showAllTransactions = false,
			showAddTransaction = false,
			showSubtractTransaction = false,
			showTransferTransaction = false,
			showCheckoutTransaction = false,
			showSetTransaction = false,
		}) {
		console.log("Getting stored view html for: ", stored)
		let newContent = $('<div class="row storedViewRow"></div>');

		if (includeBlockLink) {
			newContent.append(
				StoredView.getStoredBlockLink(stored.storageBlock, true)
			);
		}

		if (includeIdentifier) {
			newContent.append(
				StoredView.getStorageBlockTrackedIdentifierView(stored)
			);
		}

		newContent.append(
			StoredView.getStorageBlockAmountHeldView(stored, showCurrentlyStored),
			StoredView.getStorageBlockBarcodeView(stored, index),
			StoredView.getStorageBlockIdentifyingDetailsView(stored),
			StoredView.getStorageBlockConditionView(stored),
			StoredView.getStorageBlockConditionNotesView(stored),
			StoredView.getStorageBlockExpiresView(stored),
			StoredView.getTransactBlockLink(stored, true, {
				showAllTransactions: showAllTransactions,
				showAddTransaction: showAddTransaction,
				showSubtractTransaction: showSubtractTransaction,
				showCheckoutTransaction: showCheckoutTransaction,
				showTransferTransaction: showTransferTransaction,
				showSetTransaction: showSetTransaction
			})
		);
		//TODO:: images, keywords, atts


		return newContent;
	},
};