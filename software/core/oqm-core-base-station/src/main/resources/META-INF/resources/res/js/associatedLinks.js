AssociatedLinks = {
	Form: {
		Input: {
			getInput(innerElem){
				let output = innerElem;
				if (!output.jquery) {
					output = $(output);
				}
				if (!output.hasClass("assocLinkInputLink")) {
					output = output.closest(".assocLinkInputLink");
				}
				return output;
			},
			newInput(data = null){
				let newInput = $(PageComponents.Inputs.AssociatedLinks.linkInput);

				if (data != null) {

					AssociatedLinks.Form.Input.getLabelInput(newInput).val(data.label);
					AssociatedLinks.Form.Input.getLinkInput(newInput).val(data.link);

					if(data.description) {
						AssociatedLinks.Form.Input.getDescToggle(newInput).prop("checked", true);
						AssociatedLinks.Form.Input.getDescInput(newInput).val(data.description);
					}
				}

				AssociatedLinks.Form.Input.updateDescDisplay(newInput);

				return newInput;
			},
			getLabelInput(linkInputJq){
				return linkInputJq.find(".assocLinkInputLinkLabel");
			},
			getLinkInput(linkInputJq){
				return linkInputJq.find(".assocLinkInputLinkLink");
			},
			getDescToggle(linkInputJq){
				return linkInputJq.find(".assocLinkInputLinkDescToggle");
			},
			getDescInput(linkInputJq){
				return linkInputJq.find(".assocLinkInputLinkDesc");
			},
			descEnabled(linkInputJq){
				let toggleButt =AssociatedLinks.Form.Input.getDescToggle(linkInputJq);
				return toggleButt.is(":checked");
			},
			updateDescDisplay(innerElemJq){
				console.log("Toggling display of link description: ", innerElemJq);
				let input = AssociatedLinks.Form.Input.getInput(innerElemJq);
				let descInput = AssociatedLinks.Form.Input.getDescInput(input);

				if(AssociatedLinks.Form.Input.descEnabled(input)){
					descInput.show();
				} else {
					descInput.hide();
				}
			},
			getData(linkInputJq){

				return {
					label: AssociatedLinks.Form.Input.getLabelInput(linkInputJq).val(),
					link: AssociatedLinks.Form.Input.getLinkInput(linkInputJq).val(),
					description: AssociatedLinks.Form.Input ?
						AssociatedLinks.Form.Input.getDescInput(linkInputJq).val() : null
				};
			}
		},
		getInput: function (innerElem) {
			let output = innerElem;
			if (!output.jquery) {
				output = $(output);
			}
			if (!output.hasClass("associatedLinkInput")) {
				output = output.closest(".associatedLinkInput");
			}
			return output;
		},
		getLinkInputs: function (linkInputJq) {
			let linksContainer = AssociatedLinks.Form.getLinksContainer(linkInputJq);

			return linksContainer.find(".assocLinkInputLink");
		},
		getLinksContainer: function (linkInputJq) {
			return linkInputJq.find(".linksContainer");
		},


		addLink: function (linkInputJq, linkData = null) {
			console.info("Adding new link input to ", linkInputJq);
			let linksContainer = AssociatedLinks.Form.getLinksContainer(linkInputJq);

			let newInput =  AssociatedLinks.Form.Input.newInput(linkData);
			linksContainer.append(newInput);

			return newInput;
		},
		removeLink(remButtJq) {
			if (confirm("Are you sure?") === false) return;
			SelectedObjectDivUtils.removeSelected(
				Links.Form.Input.getInput(remButtJq)
			);
		},

		getLinkData: function (linkInputJq) {
			let output = [];

			let links = AssociatedLinks.Form.getLinkInputs(linkInputJq);

			links.each(function (i, linkInputJs) {
				output.push(
					AssociatedLinks.Form.Input.getData($(linkInputJs))
				);
			});

			return output;
		},
		populateInput: function (linkInputJq, linkList = null) {
			console.log("Populating associated link input: ",  linkInputJq, linkList);

			if(linkList) {
				linkList.forEach(function (curLinkListData) {
					AssociatedLinks.Form.addLink(linkInputJq, curLinkListData);
				});
			}
		},
		reset: function (linkInputJq) {
			AssociatedLinks.Form.getLinksContainer(linkInputJq).html("");
		},
	},

	View: {
		newPriceContainer: function (priceData, extraClasses="") {
			let output = $(`
			<div class="priceDisplayContainer p-1 `+extraClasses+`">
				<div class="priceDisplay card">
					<div class="card-header">
						<span class="card-body priceFromDefaultInd px-0 d-none" title="Default from item">*</span>
						<span class="priceLabel h4"></span> -
						<span class="pricePrice h4"></span>
						<span class="priceAsOfDateContainer"></span>
						<button class="btn btn-sm btn-link float-end showPriceDropdownButton d-none" type="button" title="Show Price Breakdown" onclick="Pricing.View.toggleBreakdownView($(this))">`+Icons.dropdown+`</button>
					</div>
					<div class="card-body priceBreakdownContainer d-none">
						<div class="mb-1 priceBreakdownFixedPriceContainer">
							<span class="h5">Fixed Price:</span>
							<span class="priceBreakdownFixedPrice"></span>
						</div>
						<div class="mb-1 priceBreakdownPerUnitPriceContainer d-none">
							+
							<span class="h5">Per-Unit Price:</span>
							<span class="priceBreakdownPerUnitPrice"></span>
						</div>
					</div>
				</div>
			</div>
			`);

			output.find(".priceLabel").text(priceData.label);

			if(priceData.asOfDate){
				let dateDisplay = output.find(".priceAsOfDateContainer");
				dateDisplay.text(priceData.asOfDate);
				dateDisplay.show();
			}

			return output;
		},
		toggleBreakdownView(breakdownButtonJq){
			breakdownButtonJq.parent().parent().find(".priceBreakdownContainer").toggleClass("d-none");
		},

		display(conainerJq, extraClasses= ""){

		}
	}
};
