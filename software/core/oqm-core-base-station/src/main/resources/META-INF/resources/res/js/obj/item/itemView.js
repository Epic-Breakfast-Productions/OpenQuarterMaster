const ItemView = {
	itemViewModal: $("#itemViewModal"),
	viewBsModal: new bootstrap.Modal($("#itemViewModal"), {}),
	itemViewMessages: $("#itemViewMessages"),
	itemViewModalLabel: $("#itemViewModalLabel"),

	storedMultiContainer: $("#itemViewStoredMultiContainer"),

	storedSingleContainer: $("#itemViewStoredSingleContainer"),
	storedNonePresentContainer: $("#itemViewStoredNonePresentContainer"),
	storedNonePresentHasStorageContainer: $("#itemViewStoredNonePresentHasStorageContainer"),
	storedNonePresentNoStorageContainer: $("#itemViewStoredNonePresentNoStorageContainer"),

	storedMultiByBlockAccordion: $("#itemViewStoredMultiByBlockAccordion"),
	storedMultiAddStoredButton: $("#itemViewStoredMultiAddStoredButton"),
	storedMultiNoneStoredInBlock: $("#itemViewStoredMultiNonePresentBlocksList"),
	storedMultiNumStoredDisplay: $("#itemViewStoredMultiNumStoredDisplay"),
	storedMultiNumBlocksDisplay: $("#itemViewStoredMultiBlockNum"),
	storedViewTabAllStoredPane: $("#itemViewStoredViewTabAllStoredContainer"),

	storedBulkContainer: $("#itemViewStoredBulkContainer"),
	storedBulkAccordion: $("#itemViewStoredBulkAccordion"),
	storedBulkNonePresentBlocksList: $("#itemViewStoredBulkNonePresentBlocksList"),
	storedNonePresentBlocksList: $("#itemViewStoredNonePresentBlocksList"),
	storedBulkNumStoredDisplay: $("#itemViewStoredBulkNumStoredDisplay"),
	storedBulkBlockNum: $("#itemViewStoredBulkBlockNum"),


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

	idsAccord: $("#itemViewIdsContainer"),
	generalIdsAccord: $("#itemViewGeneralIdsAccord"),
	generalIdNumIds: $("#itemViewGeneralIdsNumIdsLabel"),
	generalIdContent: $("#itemViewGeneralIdsAccordContent"),
	uniqueIdsAccord: $("#itemViewUniqueIdsAccord"),
	uniqueIdNumIds: $("#itemViewUniqueIdsNumIdsLabel"),
	uniqueIdContent: $("#itemViewUniqueIdsAccordContent"),

	assocIdGensAccord: $("#itemViewIdGeneratorsAccord"),
	assocIdGensNumIds: $("#itemViewIdGeneratorsNumIdsLabel"),
	assocIdGensContent: $("#itemViewIdGeneratorsAccordContent"),

	pricesContainer: $("#itemViewPricesContainer"),
	pricesTotalsContainer: $("#itemViewPricesTotalsContainer"),

	itemViewTotalLowStockThresholdContainer: $("#itemViewTotalLowStockThresholdContainer"),
	itemViewTotalLowStockThreshold: $("#itemViewTotalLowStockThreshold"),
	itemViewExpiryWarnThresholdContainer: $("#itemViewExpiryWarnThresholdContainer"),
	itemViewExpiryWarnThreshold: $("#itemViewExpiryWarnThreshold"),
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
		ItemView.storedSingleContainer.text("");
		ItemView.storedBulkContainer.hide();
		ItemView.storedBulkNumStoredDisplay.text("");
		ItemView.storedBulkBlockNum.text("");
		ItemView.storedNonePresentContainer.hide();
		ItemView.storedNonePresentHasStorageContainer.hide();
		ItemView.storedNonePresentNoStorageContainer.hide();

		ItemView.storedMultiByBlockAccordion.text("");
		ItemView.storedMultiNoneStoredInBlock.text("");
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

		ItemView.idsAccord.hide();
		ItemView.generalIdsAccord.hide();
		ItemView.generalIdNumIds.text("");
		ItemView.generalIdContent.text("");
		ItemView.uniqueIdsAccord.hide();
		ItemView.uniqueIdNumIds.text("");
		ItemView.uniqueIdContent.text("");
		ItemView.assocIdGensAccord.hide();
		ItemView.assocIdGensNumIds.text("");
		ItemView.assocIdGensContent.text("");

		ItemView.itemViewTotal.text("");
		ItemView.itemViewTotalLowStockThreshold.text("");
		ItemView.itemViewTotalLowStockThresholdContainer.hide();
		ItemView.itemViewExpiryWarnThreshold.text("");
		ItemView.itemViewExpiryWarnThresholdContainer.hide();

		ItemView.pricesContainer.hide();
		ItemView.pricesTotalsContainer.hide();
		ItemView.pricesTotalsContainer.text("");

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

	addStoredBlockViewAccordionItem: async function (item, blockId, accordionId, body) {
		let headerId = blockId + "-stored-view-accord-header";
		let collapseId = headerId + "-collapse";

		let newAccordItem = $('<div class="accordion-item" data-block-id="">' +
			'  <h2 class="accordion-header">' +
			'    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="" aria-expanded="false" aria-controls="">' +
			'    </button>' +
			'  </h2>' +
			'  <div id="" class="accordion-collapse collapse" aria-labelledby="" data-bs-parent="">' +
			'    <div class="accordion-body">' +
			'    </div>' +
			'  </div>' +
			'</div>');
		newAccordItem.attr("data-block-id", blockId);

		let header = newAccordItem.find(".accordion-header");
		header.prop("id", headerId);
		let collapse = newAccordItem.find(".accordion-collapse");
		collapse.prop("id", collapseId);
		collapse.attr("aria-labelledby", header.prop("id"));
		collapse.attr("data-bs-parent", "#" + accordionId);
		let collapseButton = newAccordItem.find(".accordion-button");
		collapseButton.attr("aria-controls", collapseId);
		collapseButton.attr("data-bs-target", "#" + collapseId);
		newAccordItem.find(".accordion-body").append(body);

		collapseButton.text(blockId);
		getStorageBlockLabel(blockId, function (blockLabel) {
			let labelText = blockLabel;
			//TODO:: image

			collapseButton.text(labelText);
		});

		return newAccordItem;
	},
	getStoredInBlockSearch: function (itemId, blockId) {
		let accordId = "itemViewStoredIn" + blockId + "SearchAccordion";
		let accordHeaderId = accordId + "Header";
		let accordCollapseId = accordId + "Collapse";
		let searchResultsId = accordId + "SearchResults";
		let searchFormId = accordId + "SearchForm";

		let output = ItemView.storedViewTabAllStoredPane.clone();
		output.find("#itemViewAllStoredSearchResults").empty();
		output = $(
			output.html()
				.replaceAll("itemViewAllStoredSearchResults", searchResultsId)
				.replaceAll("itemViewAllStoredSearchAccordion", accordId)
				.replaceAll("itemViewAllStoredSearchAccordionFieldsHeader", accordHeaderId)
				.replaceAll("itemViewAllStoredSearchAccordionFieldsCollapse", accordCollapseId)
				.replaceAll("itemViewAllStoredSearchResults", searchResultsId)
				.replaceAll("itemViewAllStoredSearchForm", searchFormId)
		);
		output.find(".spinner").remove();//race condition; sometimes catch the spinner
		output.find("#" + searchFormId + "-storageBlockInputId").val(blockId);
		output.find("#" + searchFormId + "-storageBlockInputName").val(blockId);
		output.find("#" + searchFormId + "-storageBlockInputName").prop("disabled", true);
		output.find("#" + searchFormId + "-storageBlockInputName").prop("readonly", true);
		output.find("#" + searchFormId + "-storageBlockInputSearchButton").prop("disabled", true);
		output.find("#" + searchFormId + "-storageBlockInputSearchButton").prop("readonly", true);
		output.find("#" + searchFormId + "-storageBlockInputClearButton").prop("disabled", true);
		output.find("#" + searchFormId + "-storageBlockInputClearButton").prop("readonly", true);

		getStorageBlockLabel(blockId, function (blockLabel) {
			output.find("#" + searchFormId + "-storageBlockInputName").val(blockLabel);
		});

		return output;
	},
	getMultiStoredInBlockView: function (itemData, blockId) {
		let output = $('<div></div>');

		let dataRow = $('<div class="d-flex mb-5"></div>');
		output.append(dataRow);
		dataRow.append($('<div class="card"></div>').append($('<div class="card-body"></div>').append($('<h5 class="card-title d-inline">Num Stored:</h5>')).append($('<p class="card-text d-inline"></p>').text(itemData.stats.storageBlockStats[blockId].numStored))));
		dataRow.append(
			$('<div class="card"></div>')
				.append(
					$('<div class="card-body"></div>')
						.append($('<h5 class="card-title d-inline">Total:</h5>'))
						.append(
							$('<p class="card-text d-inline"></p>')
								.text(
									UnitUtils.quantityToDisplayStr(itemData.stats.storageBlockStats[blockId].total)
								)
						)
				)
		);

		output.append(ItemView.getStoredInBlockSearch(itemData.id, blockId));
		return output;
	},
	setupView(itemId) {
		Main.processStart();
		console.log("Setting up view for item " + itemId);
		ItemView.resetView();

		if (ItemView.itemViewEditButton) {
			ItemView.itemViewEditButton.on("click", function () {
				ItemAddEdit.setupAddEditForEdit(itemId, ItemView.itemViewModal);
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
					ItemView.storedBulkNumStoredDisplay.text(itemData.stats.numStored);
					ItemView.storedBulkBlockNum.text(itemData.storageBlocks.length);

					let multiDisplay = function () {
						console.log(itemData.stats.numStored + " stored.");
						ItemView.storedMultiNumStoredDisplay.text(itemData.stats.numStored);
						ItemView.storedMultiNumBlocksDisplay.text(itemData.storageBlocks.length);

						ItemView.storedMultiAddStoredButton.on("click", function () {
							ItemStoredTransaction.Add.setupForm(itemData.id, null, this)
						});

						ItemView.storedMultiContainer.show();

						ItemView.allStoredSearchFormItemInputName.val(itemData.name);
						ItemView.allStoredSearchFormItemInputId.val(itemId);
						setTimeout(
							function () {
								ItemView.allStoredSearchForm.submit();
							},
							1000
						);


						itemData.storageBlocks.forEach(function (blockId) {
							console.debug("Displaying block: ", blockId);

							if (itemData.stats.storageBlockStats[blockId].numStored) {
								promises.push(
									ItemView.addStoredBlockViewAccordionItem(
										itemData,
										blockId,
										"itemViewStoredBulkAccordion",
										ItemView.getMultiStoredInBlockView(itemData, blockId)
									).then(function (newAccordItem) {
										ItemView.storedMultiByBlockAccordion.append(newAccordItem);
										setTimeout(
											function () {
												newAccordItem.find(".pagingSearchForm").submit();
											},
											1000
										);
									})
								);
							} else {
								if (ItemView.storedMultiNoneStoredInBlock.empty()) {
									ItemView.storedMultiNoneStoredInBlock.append("Blocks with nothing stored:");
								}
								getStorageBlockLabel(blockId, function (labelText) {
									let newLink = Links.getStorageViewLink(blockId, labelText);
									ItemView.storedMultiNoneStoredInBlock.append(newLink);
									ItemView.storedMultiNoneStoredInBlock.append(" ");
								});
							}
						});
					}
					StorageTypeUtils.runForType(
						itemData,
						function () {
							itemData.storageBlocks.forEach(function (blockId) {
								console.debug("Displaying block: ", blockId);

								if (itemData.stats.storageBlockStats[blockId].hasStored) {
									promises.push(
										Getters.StoredItem.getSingleStoredForItemInBlock(itemId, blockId, async function (stored) {
											ItemView.addStoredBlockViewAccordionItem(
												itemData,
												blockId,
												"itemViewStoredBulkAccordion",
												StoredView.getStoredViewContent(
													stored,
													{
														includeBlockLink: true
													})
											).then(function (newAccordItem) {
												ItemView.storedBulkAccordion.append(newAccordItem);
											});
										})
									);
								} else {
									if (ItemView.storedBulkNonePresentBlocksList.empty()) {
										ItemView.storedBulkNonePresentBlocksList.append("Blocks with no stored items: ");
									}
									getStorageBlockLabel(blockId, function (labelText) {
										let newLink = Links.getStorageViewLink(blockId, labelText);
										ItemView.storedBulkNonePresentBlocksList.append(newLink);
										ItemView.storedBulkNonePresentBlocksList.append(" ");
									});
								}
							});

							ItemView.storedBulkContainer.show();
						},
						multiDisplay,
						multiDisplay,
						function () {
							Getters.StoredItem.getSingleStoredForItem(itemId, async function (stored) {
								let promises = [];
								let storageLabel = $(`
									<h3>
										Stored in: <span class="uniqueItemStoredInLabel"></span>
									</h3>
									<p class="uniqueItemStoredAlsoInContainer">
										Also found in: <span class="uniqueItemStoredAlsoInLabel"></span>
									</p>`);

								itemData.storageBlocks.forEach(function (curBlock) {
									promises.push(getStorageBlockLabel(curBlock, function (labelText) {
										let newLink = Links.getStorageViewLink(curBlock, labelText);

										if (curBlock === stored.storageBlock) {
											storageLabel.find(".uniqueItemStoredInLabel").append(newLink);
										} else {
											storageLabel.find(".uniqueItemStoredAlsoInLabel").append(newLink);
										}
									}));
								});

								ItemView.storedSingleContainer.append(storageLabel);

								ItemView.storedSingleContainer.append(
									StoredView.getStoredViewContent(
										stored,
										{
											includeBlockLink: false
										})
								);
								await Promise.all(promises);
								if (storageLabel.find(".uniqueItemStoredAlsoInLabel").children().length === 0) {
									// storageLabel.find(".uniqueItemStoredAlsoInContainer").remove();//for some reason this doesn't work ("find" doesn't work)
									storageLabel.get(1).remove()
								}
								ItemView.storedSingleContainer.show();
							})
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
						ItemView.storedNonePresentHasStorageContainer.show();
					} else {
						ItemView.storedNonePresentNoStorageContainer.show();
					}
				}

				if(itemData.stats.prices.length){
					Pricing.View.TotalPricing.showInDiv(ItemView.pricesTotalsContainer);

					ItemView.pricesContainer.show();
					ItemView.pricesTotalsContainer.show();
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

				if(itemData.generalIds.length || itemData.uniqueIds.length || itemData.idGenerators.length) {
					console.debug("Had ids to show");
					if(itemData.generalIds.length){
						ItemView.generalIdsAccord.show();
						ItemView.generalIdNumIds.text(itemData.generalIds.length);
						GeneralIdentifiers.View.showInDiv(ItemView.generalIdContent, itemData.generalIds);
					}
					if(itemData.uniqueIds.length){
						ItemView.uniqueIdsAccord.show();
						ItemView.uniqueIdNumIds.text(itemData.uniqueIds.length);
						UniqueIdentifiers.View.showInDiv(ItemView.uniqueIdContent, itemData.uniqueIds);
					}
					if(itemData.idGenerators.length){
						ItemView.assocIdGensAccord.show();
						ItemView.assocIdGensNumIds.text(itemData.idGenerators.length);

						itemData.idGenerators.forEach(function (idGenerator, i) {
							Getters.Identifiers.generator(idGenerator).then(function (generator) {
								let newEntry = $('<li></li>');
								newEntry.text(generator.name + " / " + generator.idFormat);

								ItemView.assocIdGensContent.append(newEntry);
							});
						});
					}
					ItemView.idsAccord.show();
				}

				if (itemData.lowStockThreshold) {
					ItemView.itemViewTotalLowStockThreshold.text(itemData.lowStockThreshold.value + "" + itemData.lowStockThreshold.unit.symbol);
					ItemView.itemViewTotalLowStockThresholdContainer.show();
				}

				if(itemData.expiryWarningThreshold){
					ItemView.itemViewExpiryWarnThreshold.text(TimeHelpers.durationNumSecsToHuman(itemData.expiryWarningThreshold));
					ItemView.itemViewExpiryWarnThresholdContainer.show();
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
				Main.processStop();
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
