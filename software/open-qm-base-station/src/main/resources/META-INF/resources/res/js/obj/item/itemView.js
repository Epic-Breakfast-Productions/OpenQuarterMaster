const ItemView = {
	itemViewModal: $("#itemViewModal"),
	viewBsModal: new bootstrap.Modal($("#itemViewModal"), {}),
	itemViewMessages: $("#itemViewMessages"),
	itemViewModalLabel: $("#itemViewModalLabel"),
	itemViewStored: $("#itemViewStored"),
	itemViewStoredNonePresentContainer: $("#itemViewStoredNonePresentContainer"),
	itemViewStoredNum: $("#itemViewStoredNum"),
	itemViewStoredAccordion: $("#itemViewStoredAccordion"),
	itemViewTotal: $("#itemViewTotal"),
	itemViewTotalVal: $("#itemViewTotalVal"),
	itemViewValPerUnitDefault: $("#itemViewValPerUnitDefault"),
	itemViewValPerUnit: $("#itemViewValPerUnit"),
	itemViewCategoriesContainer: $("#itemViewCategoriesContainer"),
	itemViewCategories: $("#itemViewCategories"),
	itemViewCarousel: $("#itemViewCarousel"),
	itemViewStorageType: $("#itemViewStorageType"),
	itemViewDescriptionContainer: $("#itemViewDescriptionContainer"),
	itemViewDescription: $("#itemViewDescription"),
	itemViewBarcodeContainer: $('#itemViewBarcodeContainer'),
	itemViewBarcode: $("#itemViewBarcode"),
	itemViewTotalLowStockThresholdContainer: $("#itemViewTotalLowStockThresholdContainer"),
	itemViewTotalLowStockThreshold: $("#itemViewTotalLowStockThreshold"),
	itemViewIdentifyingAttContainer: $("#itemViewIdentifyingAttContainer"),
	itemViewIdentifyingAtt: $("#itemViewIdentifyingAtt"),
	viewKeywordsSection: $("#viewKeywordsSection"),
	viewAttsSection: $("#viewAttsSection"),
	itemViewId: $("#itemViewId"),
	itemViewEditButton: $('#itemViewEditButton'),
	itemHistoryAccordionCollapse: $("#itemHistoryAccordionCollapse"),
	itemViewCheckedOutResultsContainer: $("#itemViewCheckedOutResultsContainer"),

	resetView: function () {
		ItemView.itemViewModalLabel.text("");
		ItemView.itemViewStoredNum.text("");
		ItemView.itemViewStored.hide();
		ItemView.itemViewStoredNonePresentContainer.hide();
		ItemView.itemViewStoredAccordion.text("");
		ItemView.itemViewValPerUnitDefault.hide();
		ItemView.itemViewValPerUnit.text("");
		ItemView.itemViewIdentifyingAttContainer.hide();
		ItemView.itemViewTotalVal.text("");

		ItemView.itemViewCategoriesContainer.hide();
		ItemView.itemViewCategories.text("");

		ItemView.itemViewIdentifyingAtt.text("");

		ItemView.itemViewStorageType.text("");
		ItemView.itemViewDescriptionContainer.hide();
		ItemView.itemViewDescription.text("");
		ItemView.itemViewBarcodeContainer.hide();
		ItemView.itemViewBarcode.attr("src", "");
		ItemView.itemViewTotal.text("");
		ItemView.itemViewTotalLowStockThreshold.text("");
		ItemView.itemViewTotalLowStockThresholdContainer.hide();

		ItemView.itemViewCheckedOutResultsContainer.html("");

		resetHistorySearch(ItemView.itemHistoryAccordionCollapse);

		Carousel.clearCarousel(ItemView.itemViewCarousel);
		clearHideKeywordDisplay(ItemView.viewKeywordsSection);
		clearHideAttDisplay(ItemView.viewKeywordsSection);

		if (ItemView.itemViewEditButton) {
			ItemView.itemViewEditButton.off('click');
		}
	},
	addViewAccordionItem: function (id, content, headerContent, trackedType) {
		let accordId = "itemViewAccordBlock" + id;

		//if not string, expected as stored obj
		if (typeof headerContent !== 'string' && !(headerContent instanceof String)) {
			let stored = headerContent;
			headerContent = "";

			let funcForAmount = function () {
				headerContent += stored.amount.value + stored.amount.unit.symbol;
			};
			StoredTypeUtils.foreachStoredType(
				trackedType,
				funcForAmount,
				funcForAmount,
				function () {

				}
			);

			if (stored.condition) {
				headerContent += " Condition: " + stored.condition + "%";
			}
			if (stored.expires) {
				headerContent += " Expires: " + stored.expires;
			}
		}

		let newAccordItem = $('<div class="accordion-item">' +
			'<h2 class="accordion-header" id="' + accordId + 'Header">' +
			'<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#' + accordId + 'Collapse" aria-expanded="false" aria-controls="' + accordId + 'Collapse">' +
			headerContent +
			'</button>' +
			'</h2>' +
			'<div id="' + accordId + 'Collapse" class="accordion-collapse collapse" aria-labelledby="' + accordId + 'Header" data-bs-parent="#' + accordId + 'Header">' +
			'<div class="accordion-body">' +
			'</div>' +
			'</div>' +
			'</div>');

		newAccordItem.find('.accordion-button').text(headerContent);
		newAccordItem.find('.accordion-body').append(content);

		return newAccordItem;
	},
	addViewStorageBlocksAccordionItem: function (blockId, content, stored) {
		return ItemView.itemViewStoredAccordion.append($(ItemView.addViewAccordionItem(blockId, content, stored)));
	},
	getBlockViewCell(name, value) {
		let output = $('<div class="col"><h5></h5><p></p></div>');

		output.find("h5").text(name);
		output.find("p").text(value);
		return output;
	},
	getStorageBlockTrackedIdentifierView(stored) {
		if (stored.storedType.includes("TRACKED")) {
			return ItemView.getBlockViewCell("Identifier", stored.identifier);
		}
		return "";
	},
	getStorageBlockAmountHeldView(stored) {
		if (stored.storedType.includes("AMOUNT")) {
			return ItemView.getBlockViewCell("Stored", stored.amount.value + stored.amount.unit.symbol);
		}
		return "";
	},
	getStorageBlockBarcodeView(stored, itemId, storageBlockId, index = false) {
		if (stored.barcode) {
			let url = "/api/v1/media/code/item/" + itemId + "/barcode/stored/" + storageBlockId;

			if (index !== false) {
				url += "/" + index;
			}

			return '<div class="col"><h5>Barcode:</h5><img src="' + url + '" title="Stored item barcode" alt="Stored item barcode" class="barcodeViewImg"></div>';
		}
		return "";
	},
	getStorageBlockIdentifyingDetailsView(stored) {
		if (stored.storedType.includes("TRACKED") && stored.identifyingDetails) {
			return ItemView.getBlockViewCell("Identifying Details", stored.identifyingDetails);
		}
		return "";
	},
	getStorageBlockConditionView(stored) {
		if (stored.condition) {
			return ItemView.getBlockViewCell("Condition", stored.condition + "%");
		}
		return "";
	},
	getStorageBlockConditionNotesView(stored) {
		if (stored.conditionNotes) {
			return ItemView.getBlockViewCell("Condition Notes", stored.conditionNotes);
		}
		return "";
	},
	getStorageBlockExpiresView(stored) {
		if (stored.expires) {
			return ItemView.getBlockViewCell("Expires", stored.expires);
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
		includeIdentifier = false
	) {
		console.log("Getting stored view html for "+JSON.stringify(stored))
		let newContent = $('<div class="row storedViewRow"></div>');

		if (includeStoredLink) {
			newContent.append(
				ItemView.getStoredBlockLink(storageBlockId, true)
			);
		}

		if (includeCheckoutLink) {
			newContent.append(
				ItemView.getCheckoutBlockLink(stored, itemId, storageBlockId, true)
			);
		}

		if (includeIdentifier) {
			newContent.append(
				ItemView.getStorageBlockTrackedIdentifierView(stored)
			);
		}

		newContent.append(
			ItemView.getStorageBlockAmountHeldView(stored),
			ItemView.getStorageBlockBarcodeView(stored, itemId, storageBlockId, index),
			ItemView.getStorageBlockIdentifyingDetailsView(stored),
			ItemView.getStorageBlockConditionView(stored),
			ItemView.getStorageBlockConditionNotesView(stored),
			ItemView.getStorageBlockExpiresView(stored),
		);
		//TODO:: images, keywords, atts

		return newContent;
	},
	getAmountStoredContent(stored, itemId, storageBlockId) {
		console.log("Getting view content for simple amount stored.");
		return ItemView.getStoredViewContent(stored, itemId, storageBlockId, false, true, true);
	},
	getAmountListStoredContent(itemId, blockId, storedList) {
		console.log("Getting view content for list amount stored.");

		let accordContent = $('<div class="col accordion"></div>');

		if (storedList.length > 0) {
			let accordId = "itemViewStored" + blockId + "Accordion";
			accordContent.prop("id", accordId);
			let i = 0;
			storedList.forEach(function (curStored) {
				accordContent.append(
					ItemView.addViewAccordionItem(
						accordId + i,
						ItemView.getStoredViewContent(curStored, itemId, blockId, i, false, true),
						curStored,
						"AMOUNT_LIST"
					)
				);
				i++;
			});

		} else {
			accordContent.append($('<h4>Nothing currently stored.</h4>'));
		}

		return $('<div></div>')
			.append($('<div class="row mb-1"></div>').append(ItemView.getStoredBlockLink(blockId)))
			.append($('<div class="row"></div>').append(accordContent));
	},
	getTrackedStoredWrapperContent(itemId, blockId, trackedMap) {
		console.log("Getting view content for tracked stored wrapper.");

		let accordContent = $('<div class="col accordion"></div>');
		let storageIds = Object.keys(trackedMap);

		if (storageIds.length > 0) {
			let accordId = "itemViewStored" + blockId + "Accordion";
			accordContent.prop("id", accordId);
			storageIds.forEach(key => {

				accordContent.append(ItemView.addViewAccordionItem(
					accordId + key,
					ItemView.getStoredViewContent(trackedMap[key], itemId, blockId, key, false, true, true),
					key
				));
			});

		} else {
			accordContent.html('<h4>Nothing currently stored.</h4>');
		}


		return $('<div></div>')
			.append($('<div class="row mb-1"></div>').append(ItemView.getStoredBlockLink(blockId)))
			.append($('<div class="row"></div>').append(accordContent));
	},
	getStoredAccordView(storageType, itemId, storageId, storedWrapperData){
		return StoredTypeUtils.foreachStoredType(
			storageType,
			function () {
				return ItemView.getAmountStoredContent(storedWrapperData, itemId, storageId);
			},
			function () {
				return ItemView.getAmountListStoredContent(itemId, storageId, storedWrapperData);
			},
			function () {
				return ItemView.getTrackedStoredWrapperContent(itemId, storageId, storedWrapperData)
			}
		);
	},
	setupView(itemId) {
		console.log("Setting up view for item " + itemId);
		ItemView.resetView();

		if (ItemView.itemViewEditButton) {
			ItemView.itemViewEditButton.on("click", function () {
				setupAddEditForEdit(itemId);
			});
		}

		ItemView.itemViewId.text(itemId);
		UriUtils.addOrReplaceParams("view", itemId);
		ItemView.itemViewModalLabel.text(itemId);

		doRestCall({
			spinnerContainer: ItemView.itemViewModal,
			url: "/api/v1/inventory/item/" + itemId,
			failMessagesDiv: ItemView.itemViewMessages,
			done: async function (itemData) {
				let promises = [];

				if (itemData.categories.length) {
					ItemView.itemViewCategoriesContainer.show();
					promises.push(ItemCategoryView.setupItemCategoryView(ItemView.itemViewCategories, itemData.categories));
				}

				processKeywordDisplay(ItemView.viewKeywordsSection, itemData.keywords);
				processAttDisplay(ItemView.viewKeywordsSection, itemData.attributes);
				ItemView.itemViewModalLabel.text(itemData.name);
				ItemView.itemViewStorageType.text(itemData.storageType);
				ItemView.itemViewTotal.text(itemData.total.value + "" + itemData.total.unit.symbol);
				ItemView.itemViewTotalVal.text(itemData.valueOfStored);

				if (itemData.description) {
					ItemView.itemViewDescription.text(itemData.description);
					ItemView.itemViewDescriptionContainer.show();
				}

				if (itemData.barcode) {
					ItemView.itemViewBarcode.attr("src", "/api/v1/media/code/item/" + itemData.id + "/barcode")
					ItemView.itemViewBarcodeContainer.show();
				}

				if (itemData.lowStockThreshold) {
					ItemView.itemViewTotalLowStockThreshold.text(itemData.lowStockThreshold.value + "" + itemData.lowStockThreshold.unit.symbol);
					ItemView.itemViewTotalLowStockThresholdContainer.show();
				}

				if (itemData.imageIds.length) {
					console.log("Item had images to show.");
					ItemView.itemViewCarousel.show();
					promises.push(Carousel.setCarouselImagesFromIds(itemData.imageIds, ItemView.itemViewCarousel));
				} else {
					console.log("Storage block had no images to show.");
					ItemView.itemViewCarousel.hide();
				}

				console.log("Setting up view of stored.");

				let numStorageBlocks = Object.keys(itemData.storageMap).length;

				if (numStorageBlocks === 0) {
					console.log("None stored.");
					ItemView.itemViewStoredNonePresentContainer.show();
				} else {
					console.log(numStorageBlocks + " stored.");
					ItemView.itemViewStoredNum.text(numStorageBlocks);
					ItemView.itemViewStored.show();
				}

				let showAmountStoredPricePerUnit = function () {
					ItemView.itemViewValPerUnit.text(itemData.valuePerUnit);
				}
				StoredTypeUtils.foreachStoredType(
					itemData.storageType,
					showAmountStoredPricePerUnit,
					showAmountStoredPricePerUnit,
					function () {
						ItemView.itemViewValPerUnit.text(itemData.defaultValue);
						ItemView.itemViewValPerUnitDefault.show();

						ItemView.itemViewIdentifyingAtt.text(itemData.trackedItemIdentifierName);
						ItemView.itemViewIdentifyingAttContainer.show();
					}
				);

				Object.keys(itemData.storageMap).forEach(storageId => {
					promises.push(new Promise(async function () {
						console.log("Processing stored wrapper under storage block " + storageId);
						let curBlockName = storageId;
						await doRestCall({
							spinnerContainer: null,
							async: false,
							url: "/api/v1/inventory/storage-block/" + storageId,
							failMessagesDiv: ItemView.itemViewMessages,
							done: function (data) {
								curBlockName = data.label;
							}
						});

						ItemView.addViewStorageBlocksAccordionItem(
							storageId,
							ItemView.getStoredAccordView(itemData.storageType, itemId, storageId, itemData.storageMap[storageId].stored),
							curBlockName
						);
					}));
				});
				await Promise.all(promises);
			}
		});

		//TODO:: adjust html to match history
		//ItemCheckoutSearch.setupSearchForItem(ItemView.itemViewCheckedOutResultsContainer, itemId);
		setupHistorySearch(ItemView.itemHistoryAccordionCollapse, itemId);
	}
};

ItemView.itemViewModal[0].addEventListener("hidden.bs.modal", function () {
	UriUtils.removeParam("view");
});

if (UriUtils.getParams.has("view")
) {
	ItemView.setupView(UriUtils.getParams.get("view"));
	ItemView.viewBsModal.show();
}