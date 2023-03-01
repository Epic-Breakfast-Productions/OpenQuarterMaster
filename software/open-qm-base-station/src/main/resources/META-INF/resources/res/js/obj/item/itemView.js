//TODO:: put inside 'namespace'

var itemViewModal = $("#itemViewModal");
var itemViewMessages = $("#itemViewMessages");
var itemViewModalLabel = $("#itemViewModalLabel");
var itemViewStoredContainer = $("#itemViewStoredContainer");
var itemViewStored = $("#itemViewStored");
var itemViewStoredNonePresentContainer = $("#itemViewStoredNonePresentContainer");
var itemViewStoredNum = $("#itemViewStoredNum");
var itemViewStoredAccordion = $("#itemViewStoredAccordion");
var itemViewTotalVal = $("#itemViewTotalVal");
var itemViewValPerUnitDefault = $("#itemViewValPerUnitDefault");
var itemViewValPerUnit = $("#itemViewValPerUnit");

var itemViewCarousel = $("#itemViewCarousel");
var itemViewStorageType = $("#itemViewStorageType");
var itemViewDescriptionContainer = $("#itemViewDescriptionContainer");
var itemViewDescription = $("#itemViewDescription");
var itemViewBarcodeContainer = $('#itemViewBarcodeContainer');
var itemViewBarcode = $("#itemViewBarcode");
var itemViewTotal = $("#itemViewTotal");
var itemViewTotalLowStockThresholdContainer = $("#itemViewTotalLowStockThresholdContainer");
var itemViewTotalLowStockThreshold = $("#itemViewTotalLowStockThreshold");
var itemViewIdentifyingAttContainer = $("#itemViewIdentifyingAttContainer");
var itemViewIdentifyingAtt = $("#itemViewIdentifyingAtt");

var viewKeywordsSection = $("#viewKeywordsSection");
var viewAttsSection = $("#viewAttsSection");
var itemViewId = $("#itemViewId");

var itemHistoryAccordionCollapse = $("#itemHistoryAccordionCollapse");

function resetView(){
	itemViewStoredContainer.text("");
	itemViewModalLabel.text("");
	itemViewStoredNum.text("");
	itemViewStored.hide();
	itemViewStoredNonePresentContainer.hide();
	itemViewStoredAccordion.text("");
	itemViewValPerUnitDefault.hide();
	itemViewValPerUnit.text("");
	itemViewIdentifyingAttContainer.hide();
	itemViewTotalVal.text("");

	itemViewIdentifyingAtt.text("");

	itemViewStorageType.text("");
	itemViewDescriptionContainer.hide();
	itemViewDescription.text("");
	itemViewBarcodeContainer.hide();
	itemViewBarcode.attr("src", "");
	itemViewTotal.text("");
	itemViewTotalLowStockThreshold.text("");
	itemViewTotalLowStockThresholdContainer.hide();

	resetHistorySearch(itemHistoryAccordionCollapse);

	clearCarousel(itemViewCarousel);
	clearHideKeywordDisplay(viewKeywordsSection);
	clearHideAttDisplay(viewAttsSection);
}

function addViewAccordionItem(id, content, headerContent, trackedType){
	let accordId = "itemViewAccordBlock" + id;

	//if not string, expected as stored obj
	if (typeof headerContent !== 'string' && !(headerContent instanceof String)){
		let stored = headerContent;
		headerContent = "";

		let funcForAmount = function (){
			headerContent += stored.amount.value + stored.amount.unit.symbol;
		};
		StoredTypeUtils.foreachStoredType(
			trackedType,
			funcForAmount,
			funcForAmount,
			function (){

			}
		);

		if(stored.condition){
			headerContent += " Condition: " + stored.condition + "%";
		}
		if(stored.expires){
			headerContent += " Expires: " + stored.expires;
		}
	}

	return '<div class="accordion-item">'+
		'<h2 class="accordion-header" id="'+accordId+'Header">'+
		'<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#'+accordId+'Collapse" aria-expanded="false" aria-controls="'+accordId+'Collapse">'+
		headerContent+
		'</button>'+
		'</h2>'+
		'<div id="'+accordId+'Collapse" class="accordion-collapse collapse" aria-labelledby="'+accordId+'Header" data-bs-parent="#'+accordId+'Header">'+
		'<div class="accordion-body">'+
		content +
		'</div>'+
		'</div>'+
		'</div>';
}

function addViewStorageBlocksAccordionItem(blockId, content, stored){
	return itemViewStoredAccordion.append($(addViewAccordionItem(blockId, content, stored)));
}

function getBlockViewCell(name, value){
	return '<div class="col"><h5>'+name+':</h5><p>'+ value +'</p></div>';
}

function getStorageBlockAmountHeldView(stored, storedType){
	if(storedType.includes("AMOUNT")) {
		return getBlockViewCell("Stored", stored.amount.value + stored.amount.unit.symbol);
	}
	return "";
}
function getStorageBlockBarcodeView(stored, itemId, storageBlockId, index = false){
	if(stored.barcode) {
		let url = "/api/v1/media/code/item/"+itemId+"/barcode/stored/"+storageBlockId;

		if(index !== false){
			url += "/"+index;
		}

		return '<div class="col"><h5>Barcode:</h5><img src="'+url+'" title="Stored item barcode" alt="Stored item barcode" class="barcodeViewImg"></div>';
	}
	return "";
}

function getStorageBlockIdentifyingDetailsView(stored, storedType){
	if(storedType.includes("TRACKED") && stored.identifyingDetails) {
		return getBlockViewCell("Identifying Details", stored.identifyingDetails);
	}
	return "";
}

function getStorageBlockConditionView(stored){
	if(stored.condition) {
		return getBlockViewCell("Condition", stored.condition + "%");
	}
	return "";
}
function getStorageBlockConditionNotesView(stored){
	if(stored.conditionNotes) {
		return getBlockViewCell("Condition Notes", stored.conditionNotes);
	}
	return "";
}
function getStorageBlockExpiresView(stored){
	if(stored.expires) {
		return getBlockViewCell("Expires", stored.expires);
	}
	return "";
}

function getStoredViewContent(stored, storedType, itemId, storageBlockId, index = false){
	return '<div class="row">' +
		getStorageBlockAmountHeldView(stored, storedType)+
		getStorageBlockBarcodeView(stored, itemId, storageBlockId, index) +
		getStorageBlockIdentifyingDetailsView(stored, storedType)+
		getStorageBlockConditionView(stored)+
		getStorageBlockConditionNotesView(stored)+
		getStorageBlockExpiresView(stored)+
		'</div>';
	//TODO:: images, keywords, atts
}

function getAmountStoredContent(stored, itemId, storageBlockId){
	console.log("Getting view content for simple amount stored.");
	return getStoredViewContent(stored, "AMOUNT_SIMPLE", itemId, storageBlockId);
}

function getAmountListStoredContent(itemId, blockId, storedList){
	console.log("Getting view content for list amount stored.");


	let accordContent = "";

	if(storedList.length > 0) {
		let accordId = "itemViewStored"+blockId+"Accordion";
		let i = 0;
		storedList.forEach(function (curStored) {
			accordContent += addViewAccordionItem(
				accordId + i,
				getStoredViewContent(curStored, "AMOUNT_LIST", itemId, blockId, i),
				curStored,
				"AMOUNT_LIST"
			);
			i++;
		});

		accordContent = '<div class="col accordion" id="'+accordId+'">'+
			accordContent +
			'</div>';
	} else {
		accordContent = '<div class="col"><h4>Nothing currently stored.</h4></div>'
	}

	return '<div class="row"> ' +
		accordContent +
		'</div>';
}

function getTrackedStoredContent(itemId, blockId, trackedMap){
	console.log("Getting view content for tracked stored.");

	let accordContent = "";
	let storageIds = Object.keys(trackedMap);

	if(storageIds.length > 0) {
		let accordId = "itemViewStored"+blockId+"Accordion";
		storageIds.forEach(key => {
			accordContent += addViewAccordionItem(
				accordId + key,
				getStoredViewContent(trackedMap[key], "TRACKED",itemId, blockId, key),
				key
			);
		});

		accordContent = '<div class="col accordion" id="'+accordId+'">'+
			accordContent +
			'</div>';
	} else {
		accordContent = '<div class="col"><h4>Nothing currently stored.</h4></div>'
	}

	return '<div class="row"> ' +
		accordContent +
		'</div>';
}

function setupView(itemId){
	console.log("Setting up view for item " + itemId);
	resetView();

	itemViewId.text(itemId);
	addOrReplaceParams("view", itemId);
	itemViewModalLabel.text(itemId);

	doRestCall({
		spinnerContainer: itemViewModal,
		url: "/api/v1/inventory/item/" + itemId,
		done: async function (data) {
			let promises = [];
			processKeywordDisplay(viewKeywordsSection, data.keywords);
			processAttDisplay(viewAttsSection, data.attributes);
			itemViewModalLabel.text(data.name);
			itemViewStorageType.text(data.storageType);
			itemViewTotal.text(data.total.value + "" + data.total.unit.symbol);
			itemViewTotalVal.text(data.valueOfStored);

			if(data.description){
				itemViewDescription.text(data.description);
				itemViewDescriptionContainer.show();
			}

			if(data.barcode){
				itemViewBarcode.attr("src", "/api/v1/media/code/item/"+data.id+"/barcode")
				itemViewBarcodeContainer.show();
			}

			if(data.lowStockThreshold){
				itemViewTotalLowStockThreshold.text(data.lowStockThreshold.value + "" + data.lowStockThreshold.unit.symbol);
				itemViewTotalLowStockThresholdContainer.show();
			}

			if (data.imageIds.length) {
				console.log("Item had images to show.");
				itemViewCarousel.show();
				promises.push(setCarouselImagesFromIds(data.imageIds, itemViewCarousel));
			} else {
				console.log("Storage block had no images to show.");
				itemViewCarousel.hide();
			}

			console.log("Setting up view of stored.");

			let numStorageBlocks = Object.keys(data.storageMap).length;

			if (numStorageBlocks === 0) {
				console.log("None stored.");
				itemViewStoredNonePresentContainer.show();
			} else {
				console.log(numStorageBlocks + " stored.");
				itemViewStoredNum.text(numStorageBlocks);
				itemViewStored.show();
			}

			let showAmountStoredPricePerUnit = function () {
				itemViewValPerUnit.text(data.valuePerUnit);
			}
			StoredTypeUtils.foreachStoredType(
				data.storageType,
				showAmountStoredPricePerUnit,
				showAmountStoredPricePerUnit,
				function () {
					itemViewValPerUnit.text(data.defaultValue);
					itemViewValPerUnitDefault.show();

					itemViewIdentifyingAtt.text(data.trackedItemIdentifierName);
					itemViewIdentifyingAttContainer.show();
				}
			);

			Object.keys(data.storageMap).forEach(key => {
				promises.push(new Promise( async function(){
					console.log("Processing stored under storage block " + key);
					let curBlockName = key;
					await doRestCall({
						spinnerContainer: null,
						async: false,
						url: "/api/v1/inventory/storage-block/" + key,
						failMessagesDiv: itemViewMessages,
						done: function (data) {
							curBlockName = data.label;
						}
					});

					StoredTypeUtils.foreachStoredType(
						data.storageType,
						function () {
							addViewStorageBlocksAccordionItem(
								key,
								getAmountStoredContent(data.storageMap[key].stored, data.id, key),
								curBlockName
							);
						},
						function () {
							addViewStorageBlocksAccordionItem(
								key,
								getAmountListStoredContent(data.id, key, data.storageMap[key].stored),
								curBlockName
							);
						},
						function () {
							addViewStorageBlocksAccordionItem(
								key,
								getTrackedStoredContent(data.id, key, data.storageMap[key].stored),
								curBlockName
							);
						}
					);
				}));
			});
			await Promise.all(promises);
		},
		failMessagesDiv: itemViewMessages
	});

	setupHistorySearch(itemHistoryAccordionCollapse, itemId);
}

var viewModal = new bootstrap.Modal(itemViewModal, { });

itemViewModal[0].addEventListener("hidden.bs.modal", function (){
	removeParam("view");
});

if(getParams.has("view")){
	setupView(getParams.get("view"));
	viewModal.show();
}