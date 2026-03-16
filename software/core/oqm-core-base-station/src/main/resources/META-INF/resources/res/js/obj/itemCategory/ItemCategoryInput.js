import {Rest} from "../../Rest.js";
import {DselectUtils} from "../../DselectUtils.js";
import {PageMessageUtils} from "../../PageMessageUtils.js";

export const ItemCategoryInput = {
	getValueFromInput(itemCatInputJq){
		let inputIsMultiple = itemCatInputJq.prop("multiple");
		let val = itemCatInputJq.val();

		if(val.constructor === Array){
			val = val.filter(v => v !== "");
		} else {
			if(val === ""){
				val = null;
			}
		}

		if(inputIsMultiple){
			if(val === null){
				val = [];
			}
		}

		return val;
	},

	/**
	 * Handle newly created categories from dselect creatable mode
 	 */
	handleNewCategory(selectElementJs, newCategoryName) {
		console.log("Creating new category: ", newCategoryName);
		let selectElement = $(selectElementJs);

		Rest.call({
			url: Rest.passRoot + "/inventory/item-category",
			method: "POST",
			data: { name: newCategoryName },
			failMessagesDiv: selectElement.parent().find(".category-input-messages"),
			done: function(data) {
				console.log("Created new category with ID: " + data.id);
				// Update the option value from the text to the actual ID
				let option = selectElement.find('option').filter(function() {
					return $(this).val() === newCategoryName && $(this).text() === newCategoryName;
				});
				if (option.length) {
					option.val(data.id);
				}
			},
			fail: function(data) {
				console.error("Failed to create category: ", data);
				// Remove the invalid option
				selectElement.find('option').filter(function() {
					return $(this).val() === newCategoryName;
				}).remove();
				DselectUtils.setupDselect(selectElementJs);
			}
		});
	},

	initPage: function () {
		$(".category-input").on("change", function(e) {
			let selectElement = this;
			let values = $(selectElement).val();

			// Handle both single and multi-select
			if (!Array.isArray(values)) {
				values = values ? [values] : [];
			}

			// Check each selected value to see if it's a newly created category
			// (dselect creates options with value === text for new items)
			values.forEach(function(val) {
				if (val && val !== "") {
					let option = $(selectElement).find('option').filter(function() {
							return $(this).val() === val;
						});
					// If value equals text and doesn't look like a MongoDB ObjectId, it's new
					if (option.length && option.text() === val && !val.match(/^[0-9a-fA-F]{24}$/)) {
						ItemCategoryInput.handleNewCategory(selectElement, val);
					}
				}
			});
		});
	}
};
