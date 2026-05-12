import {Getters} from "../Getters.js";
import {ModalUtils} from "../../ModalUtils.js";
import {Rest} from "../../Rest.js";
import {PageMessageUtils} from "../../PageMessageUtils.js";
import {StoredFormInput} from "./StoredFormInput.js";
import {PageUtility} from "../../utilClasses/PageUtility.js";

export class ItemStoredEdit extends PageUtility {
	static modal = $("#itemStoredEditModal");
	static form = $("#itemStoredEditForm");
	static formMessages = $("#itemStoredEditMessages");
	static infoStoredLabel = $("#itemStoredEditItemInfoStoredLabel");
	static infoItemName = $("#itemStoredEditItemInfoItemName");
	static infoBlockLabel = $("#itemStoredEditItemInfoBlockLabel");

	static resetForm() {
		ItemStoredEdit.formMessages.text("");
		ItemStoredEdit.form.attr("action", "");
		ItemStoredEdit.form.html("");
		ItemStoredEdit.infoStoredLabel.text("");
		ItemStoredEdit.infoItemName.text("");
		ItemStoredEdit.infoBlockLabel.text("");
	}
	static async setupEditForm(buttonPressed, item, stored){
		Main.processStart();
		if (typeof item === "string" || (item instanceof String)) {
			return Getters.InventoryItem.get(item, function (itemData){
				ItemStoredEdit.setupEditForm(buttonPressed, itemData, stored);
				Main.processStop();
			});
		}
		if (typeof stored === "string" || (stored instanceof String)) {
			return Getters.StoredItem.getStored(item.id, stored, function (storedData){
				ItemStoredEdit.setupEditForm(buttonPressed, item.id, storedData);
				Main.processStop();
			});
		}
		console.log("Setting up stored edit form for stored item: ", stored);
		ModalUtils.setReturnModal(ItemStoredEdit.modal, buttonPressed);
		ItemStoredEdit.resetForm();

		let promises = [];

		ItemStoredEdit.infoStoredLabel.text(stored.labelText);
		ItemStoredEdit.infoItemName.text(item.name);

		promises.push(Getters.StorageBlock.getStorageBlockLabel(stored.storageBlock, function (blockLabel){
			ItemStoredEdit.infoBlockLabel.text(blockLabel);
		}));

		ItemStoredEdit.form.attr("action", Rest.passRoot + "/inventory/item/"+item.id+"/stored/"+stored.id);

		let inputs = await StoredFormInput.getStoredInputs(stored.type, stored, item, true);
		ItemStoredEdit.form.append(inputs);

		await Promise.all(promises);

		Main.processStop();
	}
	static {
		window.ItemStoredEdit = this;

		ItemStoredEdit.form.on("submit", async function (e) {
			e.preventDefault();
			console.log("Stored item edit form submitted.");

			let updateData = {};
			StoredFormInput.dataFromInputs(updateData, ItemStoredEdit.form);
			delete updateData["type"];

			console.debug("Stored item update data: ", updateData);

			Rest.call({
				method: "PUT",
				url: ItemStoredEdit.form.attr("action"),
				data: updateData,
				failMessagesDiv: ItemStoredEdit.formMessages,
				done: async function(){
					console.log("Successfully updated stored item.");
					PageMessageUtils.reloadPageWithMessage("Updated stored item successfully!", "success", "Success!");
				}
			});
		});
	}
}
