const StoredView = {
	viewModal: $("#itemStoredViewModal"),
	viewModalLabel: $("#itemStoredViewModalLabel"),
	viewModalMessages: $("#itemStoredViewMessages"),
	viewModalContent: $("#itemStoredViewDisplayContainer"),
	viewModalKeywords: $("#itemStoredViewKeywordsSection"),
	viewModalAtts: $("#itemStoredViewAttsSection"),
	viewModalHistory: $("#itemStoredHistory"),

	getBlockViewCell(name, ...valueJqs) {
		let output = $('<div class="col-sm-3 col-3 col-xs-6">' +
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
	getStorageBlockIdentifyingDetailsView(stored) {
		if (stored.type.includes("TRACKED") && stored.identifyingDetails) {
			return StoredView.getSimpleBlockViewCell("Identifying Details", stored.identifyingDetails);
		}
		return "";
	},
	getStorageBlockConditionView(stored) {
		if (stored.condition || stored.conditionNotes) {
			let conditionJqs = [];

			if (stored.condition) {
				let newJq = $('<p class="storedCondition mb-0"></p>');
				newJq.text(stored.condition + "%");
				conditionJqs.push(newJq);
			}

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

	getStoredImages(stored){
		if(stored.imageIds.length) {

			let output = $('<div class="col-sm-6 col-md-6 col-lg-3"></div>');

			Carousel.newCarousel(stored.id + "-image-carousel", stored, output);

			return output;
		}
		return "";
	},

	getStoredAttachedFiles(stored){
		if(stored.attachedFiles.length) {
			let output = $('<div class="col-sm-12 col-md-12 col-lg-6">'+PageComponents.View.attachedFileList+'</div>');

			FileAttachmentView.setupObjectView(output, stored.attachedFiles, null);

			return output;
		}
		return "";
	},

	getStoredIdentifiers(stored){
		if(Object.keys(stored.identifiers).length) {
			let output = $('<div class="col-sm-12 col-md-12 col-lg-6"><div class="row identifiersContainer"></div></div>');

			Identifiers.View.showInDiv(output.find(".identifiersContainer"), stored.identifiers);

			return output;
		}
		return "";
	},

	getStoredPricing(stored){
		if(Object.keys(stored.calculatedPrices).length) {
			let output = $('<div class="col-sm-12 col-md-12 col-lg-6"><div class="row pricingContainer"></div></div>');

			Pricing.View.CalculatedPricing.showInDiv(output.find(".pricingContainer"), stored.calculatedPrices, "col-6");

			return output;
		}
		return "";
	},

	//TODO: finish figuring this out
	getTransactBlockLink(stored, small = false,
						 {
							 showAddTransaction = true,
							 showSubtractTransaction = true,
							 showTransferTransaction = true,
							 showCheckoutTransaction = true,
							 showSetTransaction = true
						 }
	) {
		if (!(
			showAddTransaction || showSubtractTransaction ||
			showTransferTransaction || showCheckoutTransaction || showSetTransaction
		)) {
			return "";
		}
		let output = $('<div class=""></div>');

		console.log("Creating checkout link. Item: " + stored.item + " Block: " + stored.storageBlock + " Stored: ", stored)

		ItemStoredTransaction.ModalButtons.getTransactionSelectDropdown(
			(stored ? stored.item : null),
			stored,
			{
				showAddTransaction: showAddTransaction,
				showSubtractTransaction: showSubtractTransaction,
				showCheckoutTransaction: showCheckoutTransaction,
				showTransferTransaction: showTransferTransaction,
				showSetTransaction: showSetTransaction
			}
		).then((dropdown) => {
			output.append(dropdown);
		});

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
			showAddTransaction = true,
			showSubtractTransaction = true,
			showTransferTransaction = true,
			showCheckoutTransaction = true,
			showSetTransaction = true,
		}) {
		console.log("Getting stored view html for: ", stored)
		let newContent = $('<div class="storedView"><div class="row storedInfo"></div><hr /><div class="storedButtons d-flex"</div></div>');
		let newContentInfo = newContent.find(".storedInfo");
		let newContentButtons = newContent.find(".storedButtons");

		if (includeBlockLink) {
			newContentButtons.append(
				StoredView.getStoredBlockLink(stored.storageBlock, true)
			);
		}

		if (includeIdentifier) {//TODO:: likely remove, after label rework #1003
			newContent.append(
				StoredView.getStorageBlockTrackedIdentifierView(stored)
			);
		}

		newContentInfo.append(
			StoredView.getStoredImages(stored),
			StoredView.getStorageBlockAmountHeldView(stored, showCurrentlyStored),
			StoredView.getStorageBlockIdentifyingDetailsView(stored),
			StoredView.getStorageBlockConditionView(stored),
			StoredView.getStorageBlockExpiresView(stored),
			StoredView.getStoredKeywords(stored),
			StoredView.getStoredAtts(stored),
			StoredView.getStoredIdentifiers(stored),
			StoredView.getStoredPricing(stored),
			StoredView.getStoredAttachedFiles(stored)
		);

		newContentButtons.append(StoredView.getTransactBlockLink(stored, true, {
			showAddTransaction: showAddTransaction,
			showSubtractTransaction: showSubtractTransaction,
			showCheckoutTransaction: showCheckoutTransaction,
			showTransferTransaction: showTransferTransaction,
			showSetTransaction: showSetTransaction
		}));

		if (includeEditButton) {
			newContentButtons.append($('<button class="btn btn-warning"  title="Edit This Stored Item" data-bs-toggle="modal" data-bs-target="#itemStoredEditModal" onclick="ItemStoredEdit.setupEditForm(this, \'' + stored.item + '\', \'' + stored.id + '\');">' +
				Icons.iconWithSub(Icons.stored, Icons.edit) + ' Edit' +
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
	setupViewResultTableRow(rowJq, itemId, storedId) {
		//TODO:: create new row under row of button pressed, display stored info
	},
	toggleViewResultTableRow: async function (buttonPressed, itemId, storedId) {
		Main.processStart();
		let buttonPressedJq = $(buttonPressed);
		let storedResultRow = buttonPressedJq.closest("tr.itemStoredResultRow");
		let nextRow = storedResultRow.next();
		let viewRow;

		if (nextRow.length === 0 || nextRow.hasClass("itemStoredResultRow")) {//no current view row exists
			console.debug("No view for selected stored exists. Generating.");
			viewRow = $('<tr class="itemStoredResultViewRow table-active" style="display: none"><td class="itemStoredViewDisplayContainer" colspan="100"></td></tr>');
			let viewContainer = viewRow.find("td.itemStoredViewDisplayContainer");

			await Getters.StoredItem.getStored(itemId, storedId, function (stored) {
				viewContainer.append(StoredView.getStoredViewContent(stored, {}));
			});
			storedResultRow.after(viewRow);
		} else if (nextRow.hasClass("itemStoredResultViewRow")) {
			console.debug("View row for stored result already existent.");
			viewRow = nextRow;
		} else {
			console.error("FAILED to properly determine view row.");
			return null;
		}

		viewRow.toggle();

		if (viewRow.is(":visible")) {
			buttonPressedJq.html(Icons.viewClose);
			buttonPressedJq.attr("title", "Close View");
		} else {
			buttonPressedJq.html(Icons.view);
			buttonPressedJq.attr("title", "View");
		}

		Main.processStop();
	}
};