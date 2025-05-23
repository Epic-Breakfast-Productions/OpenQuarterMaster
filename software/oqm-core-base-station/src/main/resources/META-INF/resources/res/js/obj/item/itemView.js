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
	checkoutSearchForm: $("#itemViewCheckoutSearchForm"),
	checkoutSearchResults: $("#itemViewCheckoutSearchResults"),
	checkoutSearchFormItemNameInput: $("#itemViewCheckoutSearchForm-itemInputName"),
	checkoutSearchFormItemIdInput: $("#itemViewCheckoutSearchForm-itemInputId"),
	checkoutSearchFormItemSearchButt: $("#itemViewCheckoutSearchForm-itemInputSearchButton"),
	checkoutSearchFormItemClearButt: $("#itemViewCheckoutSearchForm-itemInputClearButton"),
	itemViewFiles: $("#itemViewFilesContainer"),

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

		ItemView.checkoutSearchFormItemNameInput.val("");
		ItemView.checkoutSearchFormItemIdInput.val("");
		ItemView.checkoutSearchForm.trigger("reset");
		FileAttachmentView.resetObjectView(ItemView.itemViewFiles);

		resetHistorySearch(ItemView.itemHistoryAccordionCollapse);

		Carousel.clearCarousel(ItemView.itemViewCarousel);
		KeywordAttUtils.clearHideKeywordDisplay(ItemView.viewKeywordsSection);
		KeywordAttUtils.clearHideAttDisplay(ItemView.viewAttsSection);

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


	getAmountStoredContent(stored, itemId, storageBlockId) {
		console.log("Getting view content for simple amount stored.");
		return StoredView.getStoredViewContent(stored, itemId, storageBlockId, false, true, true);
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
						StoredView.getStoredViewContent(curStored, itemId, blockId, i, false, true),
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
			.append($('<div class="row mb-1"></div>').append(StoredView.getStoredBlockLink(blockId)))
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
					StoredView.getStoredViewContent(trackedMap[key], itemId, blockId, key, false, true, true),
					key
				));
			});

		} else {
			accordContent.html('<h4>Nothing currently stored.</h4>');
		}


		return $('<div></div>')
			.append($('<div class="row mb-1"></div>').append(StoredView.getStoredBlockLink(blockId)))
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
				ItemAddEdit.setupAddEditForEdit(itemId);
			});
		}

		ItemView.itemViewId.text(itemId);
		UriUtils.addOrReplaceParams("view", itemId);
		ItemView.itemViewModalLabel.text(itemId);

		Rest.call({
			spinnerContainer: ItemView.itemViewModal,
			url: Rest.passRoot + "/inventory/item/" + itemId,
			failMessagesDiv: ItemView.itemViewMessages,
			done: async function (itemData) {
				let promises = [];

				if (itemData.categories.length) {
					ItemView.itemViewCategoriesContainer.show();
					promises.push(ItemCategoryView.setupItemCategoryView(ItemView.itemViewCategories, itemData.categories));
				}

				KeywordAttUtils.processKeywordDisplay(ItemView.viewKeywordsSection, itemData.keywords);
				KeywordAttUtils.processAttDisplay(ItemView.viewAttsSection, itemData.attributes);
				ItemView.itemViewModalLabel.text(itemData.name);
				ItemView.itemViewStorageType.text(StoredTypeUtils.storedTypeToDisplay(itemData.storageType));
				ItemView.itemViewTotal.text(itemData.total.value + "" + itemData.total.unit.symbol);
				ItemView.itemViewTotalVal.text(itemData.valueOfStored);
				FileAttachmentView.setupObjectView(ItemView.itemViewFiles, itemData.attachedFiles, ItemView.itemViewMessages);

				if (itemData.description) {
					ItemView.itemViewDescription.text(itemData.description);
					ItemView.itemViewDescriptionContainer.show();
				}

				if (itemData.barcode) {
					ItemView.itemViewBarcode.attr("src", Rest.apiRoot + "/media/code/item/" + itemData.id + "/barcode")
					ItemView.itemViewBarcodeContainer.show();
				}

				if (itemData.lowStockThreshold) {
					ItemView.itemViewTotalLowStockThreshold.text(itemData.lowStockThreshold.value + "" + itemData.lowStockThreshold.unit.symbol);
					ItemView.itemViewTotalLowStockThresholdContainer.show();
				}

				Carousel.processImagedObjectImages(itemData, ItemView.itemViewCarousel);

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
						await Rest.call({
							spinnerContainer: null,
							async: false,
							url: Rest.passRoot + "/inventory/storage-block/" + storageId,
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
				ItemView.checkoutSearchFormItemNameInput.val(itemData.name);
				ItemView.checkoutSearchFormItemIdInput.val(itemId);
				ItemView.checkoutSearchForm.submit();
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


//TODO:: disable item input

ItemView.checkoutSearchFormItemSearchButt.prop("disabled", true);
ItemView.checkoutSearchFormItemClearButt.prop("disabled", true);
ItemView.checkoutSearchFormItemNameInput.prop("disabled", true);

ItemView.checkoutSearchForm.on("submit", function(e){
	e.preventDefault();
	let searchParams = new URLSearchParams(new FormData(e.target));
	console.log("URL search params: " + searchParams);

	Rest.call({
		spinnerContainer: ItemView.itemViewModal.get(0),
		url: Rest.passRoot + "/inventory/item-checkout?" + searchParams,
		method: 'GET',
		returnType: false,
		failNoResponse: null,
		failNoResponseCheckStatus: true,
		extraHeaders: {
			"accept": "text/html",
			"actionType": "viewCheckin",
			"searchFormId": "itemViewCheckoutSearchForm",
			"showItemCol": false
		},
		async: false,
		done: function (data) {
			console.log("Got data!");
			ItemView.checkoutSearchResults.html(data);
		}
	});
});
