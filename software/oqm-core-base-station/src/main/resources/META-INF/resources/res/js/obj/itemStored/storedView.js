const StoredView = {
	viewModal: $("#itemStoredViewModal"),
	viewModalLabel: $("#itemStoredViewModalLabel"),
	viewModalMessages: $("#itemStoredViewMessages"),
	viewModalContent: $("#itemStoredViewDisplayContainer"),
	viewModalKeywords: $("#itemStoredViewKeywordsSection"),
	viewModalAtts: $("#itemStoredViewAttsSection"),
	viewModalHistory: $("#itemStoredHistory"),

	getBlockViewCell(name, ...valueJqs) {
		let output = $('<div class="col-sm-4 col-4 col-xs-6">' +
			'<h5 class="storedDataTitle"></h5>' +
			'<div class="storedDataContainer"></div>' +
			'</div>');
		output.find(".storedDataTitle").text(name);
		let container = output.find(".storedDataContainer");
		valueJqs.forEach(value => {
			container.append(value);
		});

		return output;
	},
	getSimpleBlockViewCell(name, value, classes = "") {
		let valueJq = $('<p class="' + classes + '"></p>');
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

			if (stored.conditionNotes) {
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
		if (stored.keywords.length) {
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
		if (Object.keys(stored.attributes).length) {
			let keywordContainer = $('<div class="attsViewContainer"></div>');
			KeywordAttUtils.displayAttsIn(keywordContainer, stored.attributes);

			return StoredView.getBlockViewCell(
				"Attributes",
				keywordContainer
			);
		}
		return "";
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
			includeEditButton = true,
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
		let newContent = $('<div class="storedView"><div class="row storedInfo"></div><div class="storedButtons d-flex"</div></div>');
		let newContentInfo = newContent.find(".storedInfo");
		let newContentButtons = newContent.find(".storedButtons");

		if (includeBlockLink) {
			newContentButtons.append(
				StoredView.getStoredBlockLink(stored.storageBlock, true)
			);
		}

		if (includeIdentifier) {
			newContent.append(
				StoredView.getStorageBlockTrackedIdentifierView(stored)
			);
		}

		newContentInfo.append(
			StoredView.getStorageBlockAmountHeldView(stored, showCurrentlyStored),
			StoredView.getStorageBlockBarcodeView(stored, index),
			StoredView.getStorageBlockIdentifyingDetailsView(stored),
			StoredView.getStorageBlockConditionView(stored),
			StoredView.getStorageBlockExpiresView(stored),
			StoredView.getStoredKeywords(stored),
			StoredView.getStoredAtts(stored)
		);
		//TODO:: images, files

		newContentButtons.append(StoredView.getTransactBlockLink(stored, true, {
			showAllTransactions: showAllTransactions,
			showAddTransaction: showAddTransaction,
			showSubtractTransaction: showSubtractTransaction,
			showCheckoutTransaction: showCheckoutTransaction,
			showTransferTransaction: showTransferTransaction,
			showSetTransaction: showSetTransaction
		}));

		if(includeEditButton){
			newContentButtons.append($('<button class="btn btn-warning"  title="Edit This Stored Item" data-bs-toggle="modal" data-bs-target="#itemStoredEditModal" onclick="ItemStoredEdit.setupEditForm(this, \''+stored.item+'\', \''+stored.id+'\');">' +
				Icons.iconWithSub(Icons.stored, Icons.edit)+' Edit' +
				'</button>'));
		}

		//TODO:: history, applied transactions
		return newContent;
	},
	resetViewModal() {
		StoredView.viewModalMessages.text("");
		StoredView.viewModalLabel.text("");
		StoredView.viewModalAtts.text("");
		StoredView.viewModalKeywords.text("");
		StoredView.viewModalContent.text("");
		//TODO:: clear history
	},
	setupViewModal: async function (itemId, blockId, storedId, previousModal) {
		Main.processStart();
		console.log("Setting up stored view for ", storedId);
		ModalHelpers.setReturnModal(StoredView.viewModal, previousModal);
		StoredView.resetViewModal();

		let promises = [];

		promises.push(Getters.StoredItem.getStored(
			itemId, blockId, storedId, function (stored) {
				StoredView.viewModalContent.append(StoredView.getStoredViewContent(stored));
			}));
		//TODO:: setup history search

		Promise.all(promises);
		console.log("Finished setting up stored view for ", storedId);
		Main.processStop();
	},
	setupViewResultTableRow(rowJq, itemId, storedId){
		//TODO:: create new row under row of button pressed, display stored info
	},
	toggleViewResultTableRow: async function(buttonPressed, itemId, storedId){
		Main.processStart();
		let buttonPressedJq = $(buttonPressed);
		let storedResultRow = buttonPressedJq.closest("tr.itemStoredResultRow");
		let nextRow = storedResultRow.next();
		let viewRow;

		if(nextRow.length === 0 || nextRow.hasClass("itemStoredResultRow")){//no current view row exists
			console.debug("No view for selected stored exists. Generating.");
			viewRow = $('<tr class="itemStoredResultViewRow table-active" style="display: none"><td class="itemStoredViewDisplayContainer" colspan="100"></td></tr>');
			let viewContainer = viewRow.find("td.itemStoredViewDisplayContainer");

			await Getters.StoredItem.getStored(itemId, storedId, function(stored){
				viewContainer.append(StoredView.getStoredViewContent(stored, {}));
			});
			storedResultRow.after(viewRow);
		} else if(nextRow.hasClass("itemStoredResultViewRow")) {
			console.debug("View row for stored result already existent.");
			viewRow = nextRow;
		} else {
			console.error("FAILED to properly determine view row.");
			return null;
		}

		viewRow.toggle();

		if(viewRow.is(":visible")){
			buttonPressedJq.html(Icons.viewClose);
			buttonPressedJq.attr("title", "Close View");
		} else {
			buttonPressedJq.html(Icons.view);
			buttonPressedJq.attr("title", "View");
		}

		Main.processStop();
	}
};