let Search = {
	mainPageSearch: $('#mainPageSearch'),
	hasMainPageSearch: function (){
		return Search.mainPageSearch.length === 1;
	},
	paginationClick: function (formId, page) {
		console.log("Paginating. Form Id: \"" + formId, "\", page: " + page);
		let searchForm = $("#" + formId);

		searchForm.find('input[name="pageNum"]').val(page);

		searchForm.submit();
	},
	fillInQueryForm: function (queryForm) {
		let getParams = new URLSearchParams(window.location.search);

		console.log("Filling in query form from page query.");
		{
			let formInputs = queryForm.find('input');
			formInputs.each(function (ind, formInput) {
				if (getParams.has(formInput.name)) {
					switch (formInput.type) {
						case "checkbox":
							if (getParams.has(formInput.name)) {
								formInput.checked = true;
							}
							break;
						default:
							$(formInput).val(getParams.get(formInput.name));
					}
				}
			});
		}
		{
			let formSelects = queryForm.find('select');
			formSelects.each(function (ind, formSelect) {
				let multi = formSelect.multiple;
				let value = (multi ? getParams.getAll(formSelect.name) : getParams.get(formSelect.name));

				if (getParams.has(formSelect.name)) {
					if (formSelect.classList.contains("dselect-select")) {
						DselectUtils.setValues($(formSelect), value);
					} else {
						$(formSelect).val(value);
					}
				}
			});
		}


		let keywordAddButton = queryForm.find(".keywordAddButton");
		if (keywordAddButton) {
			getParams.getAll("keyword").forEach(function (curKeyword) {
				console.log("Keyword: " + curKeyword);
				keywordAddButton.trigger('click');
				queryForm.find(".keywordInputDiv").find(":input.keywordInput").last().val(curKeyword);
			});
		} else {
			console.log("no keywords in search");
		}
		let attributeAddButton = queryForm.find(".attributeAddButton");
		if (attributeAddButton) {
			let attKeys = getParams.getAll("attributeKey");
			let attVals = getParams.getAll("attributeValue");

			attKeys.forEach(function (curKeyword, i) {
				console.log("attribute: " + curKeyword);
				attributeAddButton.trigger('click');
				let attInputDiv = queryForm.find(".attInputDiv");
				attInputDiv.find(":input.attInputKey").last().val(curKeyword);
				attInputDiv.find(":input.attInputValue").last().val(attVals[i]);
			});
		} else {
			console.log("No attributes in search");
		}
		let capacityAddButton = queryForm.find(".capacityAddButton");
		if (attributeAddButton) {
			let capacities = getParams.getAll("capacity");
			let units = getParams.getAll("unit");

			capacities.forEach(function (curCapacity, i) {
				let curUnit = units[i];
				console.log("Capacity: " + curCapacity + curUnit);
				capacityAddButton.trigger('click');
				let capacityInputDiv = queryForm.find(".capacityInputDiv");
				capacityInputDiv.find(":input.capacityInput").last().val(curCapacity);
				capacityInputDiv.find(":input.unitSelect").last().val(curUnit);
			});
		} else {
			console.log("No attributes in search");
		}

		console.log("DONE filling in query form from page query.");
	},

	resetPageToOne: function (pageNumInputId) {
		console.log("page num input reset to 1");
		$("#" + pageNumInputId).val(1);
	}
}

// TODO:: attach handlers to all search forms


$(document).ready(function(){
	Main.processStart();
	if (Search.hasMainPageSearch()) {
		console.log("Filling in main search form from GET params")
		Search.fillInQueryForm(Search.mainPageSearch);
		// Search.mainPageSearch.submit();
	} else {
		console.log("Page has no main search to fill in.")
	}
	Main.processStop();
});

$(".pagingSearchForm").each(function (i, form) {
	let pageNumInputId = $(form).find('input[name="pageNum"]').get(0).id;
	$(form).find(":input").each(function (i2, input) {
		if (input.name != "pageNum") {
			input.addEventListener('change', function () {
				Search.resetPageToOne(pageNumInputId);
			});
		}
	});
});

