import {StorageSearchSelect} from "../storageBlock/StorageSearchSelect.js";
import {UnitUtils} from "../UnitUtils.js";
import {ItemCategoryInput} from "../itemCategory/ItemCategoryInput.js";
import {KeywordAttEdit} from "../ObjEditUtils.js";
import {ImageSearchSelect} from "../media/ImageSearchSelect.js";
import {Rest} from "../../Rest.js";
import {PageMessageUtils} from "../../PageMessageUtils.js";
import {Pricing} from "../../Pricing.js";
import {MarkdownUtils} from "../../MarkdownUtils.js";
import {Identifiers} from "../../Identifiers.js";
import {ExtItemSearch} from "../../item/ExtItemSearch.js";
import {IdGeneratorSearchSelect} from "../idGenerator/IdGeneratorSearchSelect.js";
import {DselectUtils} from "../../DselectUtils.js";
import {ModalUtils} from "../../ModalUtils.js";
import {Icons} from "../../Icons.js";
import {Getters} from "../Getters.js";
import {FileAttachmentSearchSelect} from "../media/fileAttachment/FileAttachmentSearchSelect.js";
import {AssociatedLinks} from "../../AssociatedLinks.js";
import {TimeUtils} from "../../TimeUtils.js";
import {StorageTypeUtils} from "../../StoredTypeUtils.js";
import {PageUtility} from "../../utilClasses/PageUtility.js";

export class ItemAddEdit extends PageUtility {
	static addEditItemForm = $('#addEditItemForm');
	static addEditItemFormSubmitButton = $('#addEditItemFormSubmitButton');
	static addEditItemModal = $("#addEditItemModal");
	static addEditItemModalBs = new bootstrap.Modal("#addEditItemModal");
	static addEditItemFormMessages = $("#addEditItemFormMessages");
	static addEditItemModalLabel = $('#addEditItemModalLabel');
	static addEditItemModalLabelIcon = $('#addEditItemModalLabelIcon');
	static addEditItemFormMode = $('#addEditItemFormMode');

	static addEditItemIdInput = $("#addEditItemIdInput");
	static addEditItemNameInput = $('#addEditItemNameInput');
	static addEditItemDescriptionInput = MarkdownUtils.Editor.initInput("#addEditItemDescriptionInput")[0];
	static addEditItemExpiryWarningThresholdInput = $('#addEditItemExpiryWarningThresholdInput');
	static addEditItemExpiryWarningThresholdUnitInput = $('#addEditItemExpiryWarningThresholdUnitInput');
	static addEditItemCategoriesInput = $("#addEditItemCategoriesInput");
	static addEditItemTotalLowStockThresholdInput = $("#addEditItemTotalLowStockThresholdInput");
	static addEditItemTotalLowStockThresholdUnitInput = $("#addEditItemTotalLowStockThresholdUnitInput");
	static addEditItemPricingInput = $("#addEditItemPricingInput");
	static defaultStoredLabelInput = $("#addEditItemDefaultStoredLabelInput");
	static addEditItemStorageTypeInput = $('#addEditItemStorageTypeInput');
	static addEditItemUnitInput = $('#addEditItemUnitInput');
	static addEditItemIdentifyingAttInput = $('#addEditItemIdentifyingAttInput');

	static identifierInputContainer = Identifiers.getInputContainer($("#addEditItemIdentifiersInput"));
	static associatedGeneratorInput = $("#addEditItem-item-associatedIdGeneratorInput");

	// itemNotStoredCheck = $("#addEditItemNotStoredCheck");
	// itemNotStoredInputContainer = $("#addEditItemNotStoredInputContainer");

	static linkInput = $('#addEditItemLinksInput');
	static fileInput = $('#addEditItemForm').find(".fileAttachmentSelectInputTable");
	static addEditKeywordDiv = $('#addEditItemForm').find(".keywordInputDiv");
	static addEditAttDiv = $('#addEditItemForm').find(".attInputDiv");
	static addEditItemImagesSelected = $('#addEditItemForm').find(".imagesSelected");
	static associatedStorageInputContainer = $("#addEditItemAssociatedStorageInputContainer");
	static addEditItemTrackedItemIdentifierNameRow = $('#addEditItemTrackedItemIdentifierNameRow');
	static addEditItemUnitNameRow = $('#addEditItemUnitNameRow');
	static compatibleUnitOptions = "";


	static numAmountStoredClicked = 0;
	static numTrackedStoredClicked = 0;

	static itemAdded(newItemName, newItemId) {
		PageMessageUtils.reloadPageWithMessage("Added \"" + newItemName + "\" item successfully!", "success", "Success!");
	}

	static async foreachStorageTypeFromInput(
		whenBulk,
		whenAmountList,
		whenUniqueMulti,
		whenUniqueSingle
	) {
		await StorageTypeUtils.runForType(
			ItemAddEdit.addEditItemStorageTypeInput[0].value,
			whenBulk,
			whenAmountList,
			whenUniqueMulti,
			whenUniqueSingle
		);
	}
	static async foreachStoredTypeFromStorageInput(
		whenAmount,
		whenUnique
	) {
		await StorageTypeUtils.runForStoredType(
			ItemAddEdit.addEditItemStorageTypeInput[0].value,
			whenAmount,
			whenUnique
		);
	}
	static async resetAddEditForm() {
		let promises = [];
		ExtItemSearch.hideAddEditProductSearchPane();
		ItemAddEdit.addEditItemIdInput.val("");
		ItemAddEdit.addEditItemFormMode.val("");
		ItemAddEdit.addEditItemNameInput.val("");
		ItemAddEdit.addEditItemDescriptionInput.setValue("");
		// Identifiers.reset(ItemAddEdit.identifierInputContainer);
		IdGeneratorSearchSelect.AssociatedInput.resetAssociatedIdGenListData(ItemAddEdit.associatedGeneratorInput);
		ItemAddEdit.addEditItemModalLabel.text("Item");
		ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(0);
		ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 3);
		ItemAddEdit.addEditItemTotalLowStockThresholdInput.val("");
		ItemAddEdit.addEditItemIdentifyingAttInput.val("");
		ItemAddEdit.addEditItemStorageTypeInput.prop("disabled", false);
		ItemAddEdit.addEditItemStorageTypeInput.val($("#addEditItemStorageTypeInput option:first").val());
		DselectUtils.resetDselect(ItemAddEdit.addEditItemUnitInput);
		ItemAddEdit.addEditItemUnitInput.data("previous", ItemAddEdit.addEditItemUnitInput.val());
		DselectUtils.resetDselect(ItemAddEdit.addEditItemCategoriesInput);
		FileAttachmentSearchSelect.resetInput(this.fileInput);
		Pricing.resetInput(ItemAddEdit.addEditItemPricingInput);
		ItemAddEdit.defaultStoredLabelInput.val("");

		promises.push(ItemAddEdit.unitChanged());
		ItemAddEdit.associatedStorageInputContainer.html("");

		// this.itemNotStoredCheck.attr("checked", false);
		// this.updateItemNotStored();
		// this.itemNotStoredInputContainer.text("");

		AssociatedLinks.Form.reset(ItemAddEdit.linkInput);
		ItemAddEdit.addEditItemImagesSelected.text("");
		ItemAddEdit.addEditKeywordDiv.text("");
		ItemAddEdit.addEditAttDiv.text("");
		await Promise.all(promises);
		console.log("Reset item add/edit form.");
	}
	static async setupAddEditForAdd() {
		console.log("Setting up add/edit form for add.");
		await ItemAddEdit.resetAddEditForm();
		ItemAddEdit.addEditItemModalLabelIcon.html(Icons.iconWithSub(Icons.item, Icons.add));
		ItemAddEdit.addEditItemModalLabel.text("Item Add");
		ItemAddEdit.addEditItemFormMode.val("add");
		ItemAddEdit.addEditItemFormSubmitButton.html(Icons.iconWithSub(Icons.item, Icons.add) + " Add Item");

		await ItemAddEdit.unitChanged();
	}

	static async setupAddEditForEdit(itemId, otherModal = null) {
		console.log("Setting up add/edit form for editing item " + itemId);
		ModalUtils.setReturnModal(ItemAddEdit.addEditItemModal, otherModal);
		await ItemAddEdit.resetAddEditForm();
		ItemAddEdit.addEditItemModalLabel.text("Item Edit");
		ItemAddEdit.addEditItemFormMode.val("edit");
		ItemAddEdit.addEditItemModalLabelIcon.html(Icons.iconWithSub(Icons.item, Icons.edit));
		ItemAddEdit.addEditItemFormSubmitButton.html(Icons.iconWithSub(Icons.item, Icons.edit) + " Edit Item");

		ItemAddEdit.addEditItemStorageTypeInput.prop("disabled", true);

		Rest.call({
			spinnerContainer: ItemAddEdit.addEditItemModal,
			url: Rest.passRoot + "/inventory/item/" + itemId,
			failMessagesDiv: ItemAddEdit.addEditItemFormMessages,
			done: async function (data) {
				ImageSearchSelect.addSelectedImages(ItemAddEdit.addEditItemImagesSelected, data.imageIds);
				KeywordAttEdit.addKeywordInputs(ItemAddEdit.addEditKeywordDiv, data.keywords);
				KeywordAttEdit.addAttInputs(ItemAddEdit.addEditAttDiv, data.attributes);
				FileAttachmentSearchSelect.populateFileInputFromObject(
					ItemAddEdit.fileInput,
					data.attachedFiles,
					ItemAddEdit.addEditItemModal,
					ItemAddEdit.addEditItemFormMessages
				);

				ItemAddEdit.addEditItemIdInput.val(data.id);
				ItemAddEdit.addEditItemNameInput.val(data.name);
				ItemAddEdit.addEditItemDescriptionInput.setValue(data.description);
				ItemAddEdit.addEditItemStorageTypeInput.val(data.storageType);
				DselectUtils.setValues(ItemAddEdit.addEditItemUnitInput, data.unit.string);
				DselectUtils.setValues(ItemAddEdit.addEditItemCategoriesInput, data.categories);
				ItemAddEdit.addEditStoredTypeInputChanged(true)
					.then(function () {
						if (data.lowStockThreshold) {
							console.log("Item had low stock threshold: ", data.lowStockThreshold);
							ItemAddEdit.addEditItemTotalLowStockThresholdInput.val(data.lowStockThreshold.value)
							ItemAddEdit.addEditItemTotalLowStockThresholdUnitInput.val(data.lowStockThreshold.unit.string)
						}
					});

				Identifiers.populateEdit(ItemAddEdit.identifierInputContainer, data.identifiers);
				IdGeneratorSearchSelect.AssociatedInput.populateAssociatedIdGenListData(ItemAddEdit.associatedGeneratorInput, data.idGenerators);

				let durationTimespan = TimeUtils.durationNumSecsToTimespan(data.expiryWarningThreshold);

				ItemAddEdit.addEditItemExpiryWarningThresholdInput.val(TimeUtils.durationNumSecsTo(data.expiryWarningThreshold, durationTimespan));
				switch (durationTimespan) {
					case "weeks":
						ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 4);
						break;
					case "days":
						ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 3);
						break;
					case "hours":
						ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 2);
						break;
					case "minutes":
						ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 1);
						break;
					case "seconds":
						ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.prop('selectedIndex', 0);
						break;
				}

				data.storageBlocks.forEach(curStorageBlockId => {
					Getters.StorageBlock.getStorageBlockLabel(curStorageBlockId, function (label) {
						//TODO:: determine if we are allowed to remove (if has stored items in it or not)
						ItemAddEdit.storageInput.addStorage(label, curStorageBlockId);
					});
				});


				ItemAddEdit.defaultStoredLabelInput.val(data.defaultLabelFormat);
				AssociatedLinks.Form.populateInput(ItemAddEdit.linkInput, data.associatedLinks);
				await Pricing.populateInput(
					ItemAddEdit.addEditItemPricingInput,
					ItemAddEdit.getUnit(),
					data.defaultPrices
				);

				await ItemAddEdit.unitChanged();
			}
		});
	}
	static async addEditStoredTypeInputChanged(force = false) {
		await ItemAddEdit.foreachStoredTypeFromStorageInput(
			function () {
				ItemAddEdit.addEditItemUnitNameRow.show();
				ItemAddEdit.addEditItemUnitInput.prop('required', true);
			},
			function () {
				ItemAddEdit.addEditItemUnitNameRow.hide();
				ItemAddEdit.addEditItemUnitInput.prop('required', false);
			}
		);
		return ItemAddEdit.unitChanged(force);
	}

	static storageInput = class {
		static addStorage(blockName, blockId) {
			Main.processStart();
			let found = false;
			ItemAddEdit.associatedStorageInputContainer.find('input[name="storageBlocks[]"]').each(function () {
				if ($(this).val() === blockId) {
					found = true;
				}
			});
			if (found) {
				console.log("Tried to add a block that was already present.");
				return;
			}

			let newBlock = $('<div class="col-3 blockSelection" data-block-id="">' +
				'  <input type="hidden" name="storageBlocks[]" />' +
				'  <div class="card">' +
				'    <div class="card-body">' +
				'      <p class="card-text blockInputName"></p>' +
				'    </div>' +
				'    <div class="card-footer text-body-secondary">' +
				'      <button class="btn btn-sm btn-outline-danger" type="button" onclick="ItemAddEdit.storageInput.removeStorage(this);">' + Icons.remove + '</button>' +
				'    </div>' +
				'  </div>' +
				'</div>');
			newBlock.attr("data-block-id", blockId);
			newBlock.find('input[name="storageBlocks[]"]').val(blockId);
			newBlock.find(".blockInputName").text(blockName);

			ItemAddEdit.associatedStorageInputContainer.append(newBlock);
			Main.processStop();
		}
		static removeStorage(removeButtonClicked) {//or input card?
			if (confirm("Are you sure you want to\nremove this associated storage?")) {
				console.log("Removing associated storage.");
				removeButtonClicked.parentElement.parentElement.parentElement.remove();
			} else {
				console.log("User canceled removing the associated storage.");
			}
		}
		static selectedStorageList() {
			return ItemAddEdit.associatedStorageInputContainer.find("input[name='storageBlocks[]']")
				.map(function () {
					return $(this).val();
				}).get();
		}
	}
	static getUnit(force = false){
		return (force || ItemAddEdit.addEditItemUnitNameRow.is(":visible")) ?
			ItemAddEdit.addEditItemUnitInput.val() :
			"units";
	}
	static async unitChanged(force = false){
		let itemUnit = ItemAddEdit.getUnit();

		console.log("Item Unit Changed to ", itemUnit);

		let lowStockUnitPromise = ItemAddEdit.updateLowStockUnits(itemUnit, force);
		let pricingUnitPromise = Pricing.setUnit(
			ItemAddEdit.addEditItemPricingInput,
			itemUnit
		);

		await Promise.all([lowStockUnitPromise, pricingUnitPromise]);
	}
	static updateLowStockUnits(itemUnit, force = false) {
		return UnitUtils.getCompatibleUnitOptions(itemUnit)
			.then(function (options) {
				ItemAddEdit.addEditItemTotalLowStockThresholdUnitInput.html(options);
			});
	}
	static {
		window.ItemAddEdit = this;

		ItemAddEdit.addEditItemUnitInput.on("change", function () {
			console.log("Changed unit!");
			ItemAddEdit.unitChanged();
		});

// //prevent enter from submitting form on barcode; barcode scanners can add enter key automatically
// ItemAddEdit.addEditItemBarcodeInput.on('keypress', function (e) {
// 	// Ignore enter keypress
// 	if (e.which === 13) {
// 		return false;
// 	}
// });

		ItemAddEdit.addEditItemForm.submit(async function (event) {
			event.preventDefault();
			console.log("Submitting add/edit form.");

			let addEditData = {
				name: ItemAddEdit.addEditItemNameInput.val(),
				description: ItemAddEdit.addEditItemDescriptionInput.getValue(),
				identifiers: Identifiers.getIdentifierData(ItemAddEdit.identifierInputContainer),
				idGenerators: IdGeneratorSearchSelect.AssociatedInput.getAssociatedIdGenListData(ItemAddEdit.associatedGeneratorInput),
				storageType: ItemAddEdit.addEditItemStorageTypeInput.val(),
				expiryWarningThreshold: ItemAddEdit.addEditItemExpiryWarningThresholdInput.val() * ItemAddEdit.addEditItemExpiryWarningThresholdUnitInput.val(),
				lowStockThreshold: (ItemAddEdit.addEditItemTotalLowStockThresholdInput.val() ? UnitUtils.getQuantityObj(
					ItemAddEdit.addEditItemTotalLowStockThresholdInput.val(),
					ItemAddEdit.addEditItemTotalLowStockThresholdUnitInput.val()
				) : null),
				associatedLinks: AssociatedLinks.Form.getLinkData(ItemAddEdit.linkInput),
				categories: ItemCategoryInput.getValueFromInput(ItemAddEdit.addEditItemCategoriesInput),
				storageBlocks: ItemAddEdit.storageInput.selectedStorageList(),
				attachedFiles: FileAttachmentSearchSelect.getFileListFromInput(ItemAddEdit.fileInput),
				defaultPrices: Pricing.getPricingData(ItemAddEdit.addEditItemPricingInput),
				defaultLabelFormat: ItemAddEdit.defaultStoredLabelInput.val() ? ItemAddEdit.defaultStoredLabelInput.val() : null
			};

			let setAmountStoredVars = function () {
				addEditData["unit"] = UnitUtils.getUnitObj(ItemAddEdit.addEditItemUnitInput.val());
			};

			ItemAddEdit.foreachStorageTypeFromInput(
				setAmountStoredVars,
				setAmountStoredVars,
				function () {

				}
			);

			KeywordAttEdit.addKeywordAttData(addEditData, ItemAddEdit.addEditKeywordDiv, ItemAddEdit.addEditAttDiv);
			ImageSearchSelect.addImagesToData(addEditData, ItemAddEdit.addEditItemImagesSelected);

			console.log("Data being submitted: " + JSON.stringify(addEditData));
			let verb = "";
			let result = false;
			if (ItemAddEdit.addEditItemFormMode.val() === "add") {
				verb = "Created";
				console.log("Adding new item.");
				await Rest.call({
					url: Rest.passRoot + "/inventory/item",
					method: "POST",
					data: addEditData,
					async: false,
					done: function (data) {
						console.log("Response from create request: " + JSON.stringify(data));
						result = true;
					},
					failMessagesDiv: ItemAddEdit.addEditItemFormMessages
				});
			} else if (ItemAddEdit.addEditItemFormMode.val() === "edit") {
				verb = "Edited";
				let id = ItemAddEdit.addEditItemIdInput.val();
				console.log("Editing storage block " + id);

				await Rest.call({
					url: Rest.passRoot + "/inventory/item/" + id,
					method: "PUT",
					data: addEditData,
					async: false,
					done: function (data) {
						console.log("Response from create request: " + JSON.stringify(data));
						result = true;
					},
					failMessagesDiv: ItemAddEdit.addEditItemFormMessages
				});
			}

			if (!result) {
				PageMessageUtils.addMessageToDiv(ItemAddEdit.addEditItemFormMessages, "danger", "Failed to do " + verb + " item.", "Failed", null);
			} else {
				PageMessageUtils.reloadPageWithMessage(verb + " item successfully!", "success", "Success!");
			}
		});
	}
}
