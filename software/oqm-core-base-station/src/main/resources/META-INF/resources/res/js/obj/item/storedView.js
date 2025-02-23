const StoredView = {
	getBlockViewCell(name, ...valueJqs) {
		let output = $('<div class="col-sm-4 col-4 col-xs-6">' +
			'<h5 class="storedDataTitle"></h5>' +
			'<div class="storedDataContainer"></div>' +
			'</div>');
		output.find(".storedDataTitle").text(name);
		let container = output.find(".storedDataContainer");
		valueJqs.forEach(value=>{
			container.append(value);
		});

		return output;
	},
	getSimpleBlockViewCell(name, value, classes = "") {
		let valueJq = $('<p class="'+classes+'"></p>');
		valueJq.text(value);
		return StoredView.getBlockViewCell(name, valueJq);
	},
	getStorageBlockTrackedIdentifierView(stored) {
		if (stored.type.includes("UNIQUE")) {
			return StoredView.getSimpleBlockViewCell("Identifier", stored.identifier);
		}
		return "";
	},
	getStorageBlockAmountHeldView(stored, showCurrently = false) {
		if (stored.type.includes("AMOUNT")) {
			return StoredView.getSimpleBlockViewCell((showCurrently ? "Currently " : "") + "Stored:", stored.amount.value + stored.amount.unit.symbol, "storedViewAmount");
		}
		return "";
	},
	getStorageBlockBarcodeView(stored, index = false) {
		//TODO:: rework
		if (stored.barcode) {
			let url = "/api/media/code/barcode/" + encodeURIComponent(stored.barcode);

			if (index !== false) {
				url += "/" + index;
			}

			return StoredView.getBlockViewCell(
				"Barcode",
				$('<img src="' + url + '" title="Stored item barcode" alt="Stored item barcode" class="barcodeViewImg">')
			);
		}
		return "";
	},
	getStorageBlockIdentifyingDetailsView(stored) {
		if (stored.type.includes("TRACKED") && stored.identifyingDetails) {
			return StoredView.getSimpleBlockViewCell("Identifying Details", stored.identifyingDetails);
		}
		return "";
	},
	getStorageBlockConditionView(stored) {
		if (stored.condition) {
			let conditionJqs = [];

			let newJq = $('<p class="storedCondition mb-0"></p>');
			newJq.text(stored.condition + "%");
			conditionJqs.push(newJq);

			if(stored.conditionNotes){
				let newJq = $('<p class="storedConditionNotes mt-0 small"></p>');
				newJq.text(stored.conditionNotes);
				conditionJqs.push(newJq);
			}

			return StoredView.getBlockViewCell(
				"Condition",
				...conditionJqs
			);
		}
		return "";
	},
	getStorageBlockExpiresView(stored) {
		if (stored.expires) {
			return StoredView.getSimpleBlockViewCell("Expires", stored.expires);
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
	getStoredKeywords(stored) {
		if(stored.keywords){
			let keywordContainer = $('<div class="keywordsViewContainer"></div>');
			KeywordAttUtils.displayKeywordsIn(keywordContainer, stored.keywords);

			return StoredView.getBlockViewCell(
				"Keywords",
				keywordContainer
			);
		}
		return "";
	},
	getStoredAtts(stored) {
		if(stored.attributes){
			let keywordContainer = $('<div class="attsViewContainer"></div>');
			KeywordAttUtils.displayAttsIn(keywordContainer, stored.attributes);

			return StoredView.getBlockViewCell(
				"Attributes",
				keywordContainer
			);
		}
		return "";
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
			StoredView.getStorageBlockExpiresView(stored),
			StoredView.getStoredKeywords(stored),
			StoredView.getStoredAtts(stored),
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