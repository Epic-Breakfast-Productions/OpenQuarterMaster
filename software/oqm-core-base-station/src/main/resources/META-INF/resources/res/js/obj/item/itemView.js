const ItemView = {
	itemViewModal: $("#itemViewModal"),
	viewBsModal: new bootstrap.Modal($("#itemViewModal"), {}),
	itemViewMessages: $("#itemViewMessages"),
	itemViewModalLabel: $("#itemViewModalLabel"),

	storedMultiContainer: $("#itemViewStoredMultiContainer"),

	storedSingleContainer: $("#itemViewStoredSingleContainer"),
	storedNonePresentContainer: $("#itemViewStoredNonePresentContainer"),
	storedMultiByBlockAccordion: $("#itemViewStoredMultiByBlockAccordion"),
	storedMultiNumStoredDisplay: $("#itemViewStoredMultiNumStoredDisplay"),
	storedMultiNumBlocksDisplay: $("#itemViewStoredMultiBlockNum"),
	storedSingleAccordion: $("#itemViewStoredSingleAccordion"),
	storedBulkAccordion: $("#itemViewStoredBulkAccordion"),
	storedNonePresentBlocksList: $("#itemViewStoredNonePresentBlocksList"),

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

	allStoredSearchForm: $("#itemViewAllStoredSearchForm"),
	allStoredSearchFormItemInputDeleteButton: $("#itemViewAllStoredSearchForm-itemInputClearButton"),
	allStoredSearchFormItemInputSearchButton: $("#itemViewAllStoredSearchForm-itemInputSearchButton"),
	allStoredSearchFormItemInputId: $("#itemViewAllStoredSearchForm-itemInputId"),
	allStoredSearchFormItemInputName: $("#itemViewAllStoredSearchForm-itemInputName"),

	resetView: function () {
		ItemView.itemViewModalLabel.text("");
		ItemView.storedMultiContainer.hide();
		ItemView.storedSingleContainer.hide();
		ItemView.storedNonePresentContainer.hide();
		ItemView.storedMultiByBlockAccordion.text("");
		ItemView.storedSingleAccordion.text("");
		ItemView.storedBulkAccordion.text("");
		ItemView.itemViewValPerUnitDefault.hide();
		ItemView.itemViewValPerUnit.text("");
		ItemView.itemViewIdentifyingAttContainer.hide();
		ItemView.itemViewTotalVal.text("");
		ItemView.storedNonePresentBlocksList.text("");

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

		ItemView.allStoredSearchForm.trigger("reset");

		resetHistorySearch(ItemView.itemHistoryAccordionCollapse);

		Carousel.clearCarousel(ItemView.itemViewCarousel);
		KeywordAttUtils.clearHideKeywordDisplay(ItemView.viewKeywordsSection);
		KeywordAttUtils.clearHideAttDisplay(ItemView.viewAttsSection);

		// this.allStoredSearchFormItemInputDeleteButton.disable();//TODO
		// this.allStoredSearchFormItemInputSearchButton.disable();//TODO
		this.allStoredSearchFormItemInputName.val("");
		this.allStoredSearchFormItemInputId.val("");


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
			StoredTypeUtils.runForType(
				trackedType,
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

	addStoredBlockViewAccordionItem: async function (item, blockId, accordionId, bodyPromise) {
		let headerId = blockId + "-stored-view-accord-header";
		let collapseId = headerId + "-collapse";

		let newAccordItem = $('<div class="accordion-item">' +
			'  <h2 class="accordion-header">' +
			'    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="" aria-expanded="false" aria-controls="">' +
			'    </button>' +
			'  </h2>' +
			'  <div id="" class="accordion-collapse collapse" aria-labelledby="" data-bs-parent="">' +
			'    <div class="accordion-body">' +
			'    </div>' +
			'  </div>' +
			'</div>');

		let header = newAccordItem.find(".accordion-header");
		header.prop("id", headerId);
		let collapse = newAccordItem.find(".accordion-collapse");
		collapse.prop("id", collapseId);
		collapse.prop("aria-labelledby", header.prop("id"));
		collapse.prop("data-bs-parent", "#" + accordionId);
		let collapseButton = newAccordItem.find(".accordion-button");
		collapseButton.prop("aria-controls", collapseId);
		collapseButton.prop("data-bs-target", "#" + collapseId);

		await bodyPromise;
		newAccordItem.find(".accordion-body").append(bodyPromise);

		return newAccordItem;
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

				if (itemData.stats.numStored) {
					let multiDisplay = function(){
						//TODO
					}
					StorageTypeUtils.runForType(
						itemData,
						function () {
							//TODO
						},
						multiDisplay,
						multiDisplay,
						function () {
							//TODO
						}
					);
				} else {
					console.log("None stored.");
					ItemView.storedNonePresentContainer.show();

					if (itemData.storageBlocks.length) {
						itemData.storageBlocks.forEach(function (curBlock) {
							getStorageBlockLabel(curBlock, function (labelText) {
								let newLink = Links.getStorageViewLink(curBlock, labelText);
								ItemView.storedNonePresentBlocksList.append(newLink);
								ItemView.storedNonePresentBlocksList.append(" ");
							});
						});
					} else {
						//TODO:: button to edit item
						ItemView.storedNonePresentBlocksList.text("None! Add storage blocks by editing the item!");
					}
				}

				if (itemData.categories.length) {
					ItemView.itemViewCategoriesContainer.show();
					promises.push(ItemCategoryView.setupItemCategoryView(ItemView.itemViewCategories, itemData.categories));
				}

				KeywordAttUtils.processKeywordDisplay(ItemView.viewKeywordsSection, itemData.keywords);
				KeywordAttUtils.processAttDisplay(ItemView.viewAttsSection, itemData.attributes);
				ItemView.itemViewModalLabel.text(itemData.name);
				ItemView.itemViewStorageType.text(StorageTypeUtils.typeToDisplay(itemData.storageType));
				ItemView.itemViewTotal.text(itemData.stats.total.value + "" + itemData.stats.total.unit.symbol);
				ItemView.itemViewTotalVal.text(itemData.valueOfStored);//TODO
				FileAttachmentView.setupObjectView(ItemView.itemViewFiles, itemData.attachedFiles, ItemView.itemViewMessages);

				if (itemData.description) {
					ItemView.itemViewDescription.text(itemData.description);
					ItemView.itemViewDescriptionContainer.show();
				}

				if (itemData.barcode) {
					ItemView.itemViewBarcode.attr("src", Rest.passRoot + "/media/code/item/" + itemData.id + "/barcode");
					ItemView.itemViewBarcodeContainer.show();
				}

				if (itemData.lowStockThreshold) {
					ItemView.itemViewTotalLowStockThreshold.text(itemData.lowStockThreshold.value + "" + itemData.lowStockThreshold.unit.symbol);
					ItemView.itemViewTotalLowStockThresholdContainer.show();
				}

				Carousel.processImagedObjectImages(itemData, ItemView.itemViewCarousel);

				console.log("Setting up view of stored.");

				// let numStorageBlocks = itemData.stats.numStored;// Object.keys(itemData.storageBlocks).length;
				//TODO:: show storage blocks associated
				// if (numStorageBlocks === 0) {
				// 	console.log("None stored.");
				// 	ItemView.storedNonePresentContainer.show();
				// 	//TODO:: if storage blocks present, prompt to edit item to add block(s)
				// 	//TODO:: prompt to add stored item
				// } else {
				// 	//TODO:: display right; bulk vs multi vs single display
				// 	console.log(numStorageBlocks + " stored.");
				// 	ItemView.itemViewStoredNum.text(numStorageBlocks);
				// 	ItemView.itemViewStored.show();
				//
				// 	ItemView.allStoredSearchFormItemInputName.val(itemData.name);
				// 	ItemView.allStoredSearchFormItemInputId.val(itemId);
				// 	ItemView.allStoredSearchForm.submit();
				// }

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

ItemView.allStoredSearchFormItemInputDeleteButton.prop("disabled", true);
ItemView.allStoredSearchFormItemInputSearchButton.prop("disabled", true);
ItemView.allStoredSearchFormItemInputName.prop("disabled", true);

ItemView.checkoutSearchFormItemSearchButt.prop("disabled", true);
ItemView.checkoutSearchFormItemClearButt.prop("disabled", true);
ItemView.checkoutSearchFormItemNameInput.prop("disabled", true);

ItemView.checkoutSearchForm.on("submit", function (e) {
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
