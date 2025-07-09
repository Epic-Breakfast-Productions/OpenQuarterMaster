const ItemCheckoutView = {
	itemCheckoutViewModal: $("#itemCheckoutViewModal"),
	viewBsModal: new bootstrap.Modal($("#itemCheckoutViewModal"), {}),
	messages: $("#itemCheckoutViewMessages"),
	viewId: $("#itemCheckoutViewId"),
	statusLabel: $("#itemCheckoutViewStatusLabel"),
	statusLabelContainer: $("#itemCheckoutViewStatusLabelContainer"),
	itemName: $("#itemCheckoutViewItemName"),
	checkedOutFrom: $("#itemCheckoutViewCheckedOutFromLabel"),
	checkedOut: $("#itemCheckoutViewCheckedOut"),
	checkedOutByLabel: $("#itemCheckoutViewCheckedOutByLabel"),
	checkedOutForLabel: $("#itemCheckoutViewCheckedOutForLabel"),
	checkedOutOn: $("#itemCheckoutViewCheckedOutOn"),
	dueBackOnContainer: $("#itemCheckoutViewDueBackOnContainer"),
	dueBackOn: $("#itemCheckoutViewDueBackOn"),
	reason: $("#itemCheckoutViewReason"),
	reasonContainer: $("#itemCheckoutViewReasonContainer"),
	notes: $("#itemCheckoutViewNotes"),
	notesContainer: $("#itemCheckoutViewNotesContainer"),
	checkinDetailsContainer: $("#itemCheckoutViewCheckinDetailsContainer"),
	checkinDetailsTime: $("#itemCheckoutViewCheckinDetailsTime"),
	checkinDetailsNotesContainer: $("#itemCheckoutViewCheckinDetailsNotesContainer"),
	checkinDetailsNotes: $("#itemCheckoutViewCheckinDetailsNotes"),
	checkinDetailsCarousel: $("#itemCheckoutViewCheckinDetailsCarousel"),
	checkinDetailsTypeContainer: $("#itemCheckoutViewCheckinDetailsTypeContainer"),
	checkinDetailsType: $("#itemCheckoutViewCheckinDetailsType"),
	checkinDetailsLossReasonContainer: $("#itemCheckoutViewCheckinDetailsLossReasonContainer"),
	checkinDetailsLossReason: $("#itemCheckoutViewCheckinDetailsLossReason"),
	checkinDetailsCheckedIntoContainer: $("#itemCheckoutViewCheckinDetailsCheckedIntoContainer"),
	checkinDetailsCheckedInto: $("#itemCheckoutViewCheckinDetailsCheckedInto"),
	checkinDetailsCheckedInBy: $("#itemCheckoutViewCheckinDetailsCheckedInBy"),
	checkinButton: $("#itemCheckoutViewCheckinButton"),

	history: $("#itemCheckoutViewHistoryAccordionCollapse"),
	viewKeywordsSection: $("#itemCheckoutViewKeywordsSection"),
	viewAttsSection: $("#itemCheckoutViewAttsSection"),
	checkinDetailsKeywordsSection: $("#itemCheckoutViewCheckinDetailsKeywordsSection"),
	checkinDetailsAttsSection: $("#itemCheckoutViewCheckinDetailsAttsSection"),


	resetCheckoutView(){
		ItemCheckoutView.messages.text("");
		ItemCheckoutView.viewId.text("");
		ItemCheckoutView.itemName.text("");
		ItemCheckoutView.checkedOutFrom.text("");
		ItemCheckoutView.checkedOut.text("");
		ItemCheckoutView.statusLabel.text("");
		ItemCheckoutView.statusLabelContainer.removeClass("bg-success");
		ItemCheckoutView.checkedOutByLabel.text("");
		ItemCheckoutView.checkedOutForLabel.text("");
		ItemCheckoutView.checkedOutOn.text("");
		ItemCheckoutView.dueBackOnContainer.hide();
		ItemCheckoutView.dueBackOnContainer.removeClass("bg-danger")
		ItemCheckoutView.dueBackOn.text("");
		ItemCheckoutView.reasonContainer.hide();
		ItemCheckoutView.reason.text("");
		ItemCheckoutView.notesContainer.hide();
		ItemCheckoutView.notes.text("");
		ItemCheckoutView.checkinDetailsContainer.hide();
		ItemCheckoutView.checkinDetailsTypeContainer.removeClass("bg-success", "bg-danger");
		ItemCheckoutView.checkinDetailsType.text("");
		ItemCheckoutView.checkinDetailsTime.text("");
		ItemCheckoutView.checkinDetailsNotesContainer.hide();
		ItemCheckoutView.checkinDetailsNotes.text("");
		ItemCheckoutView.checkinDetailsLossReasonContainer.hide();
		ItemCheckoutView.checkinDetailsLossReason.text("");
		ItemCheckoutView.checkinDetailsCheckedIntoContainer.hide();
		ItemCheckoutView.checkinDetailsCheckedInto.text("");
		ItemCheckoutView.checkinButton.hide();

		resetHistorySearch(ItemCheckoutView.history);
		KeywordAttUtils.clearHideKeywordDisplay(ItemCheckoutView.viewKeywordsSection);
		KeywordAttUtils.clearHideAttDisplay(ItemCheckoutView.viewAttsSection);
		KeywordAttUtils.clearHideKeywordDisplay(ItemCheckoutView.checkinDetailsKeywordsSection);
		KeywordAttUtils.clearHideAttDisplay(ItemCheckoutView.checkinDetailsAttsSection);
	},
	async setupView(itemCheckoutId){
		console.log("Setting up view for item checkout " + itemCheckoutId);
		this.resetCheckoutView();

		ItemCheckoutView.viewId.text(itemCheckoutId);

		await Rest.call({
			spinnerContainer: ItemCheckoutView.itemCheckoutViewModal.get(0),
			url: Rest.passRoot + "/inventory/item-checkout/" + itemCheckoutId,
			method: "GET",
			async: false,
			failMessagesDiv: ItemCheckoutView.messages,
			done: function (checkoutData) {
				console.log("Setting up view for checkout: ", checkoutData);
				let promises = [];

				//TODO:: get create history event for checkout to show who checked out, enabled by #332
				promises.push(Rest.call({
					url: Rest.passRoot + "/inventory/item-checkout/" + itemCheckoutId + "/history?eventType=CREATE",
					method: "GET",
					async: false,
					failMessagesDiv: ItemCheckoutView.messages,
					done: function (checkoutData) {
						console.log("Checkout history for creates: ", checkoutData);
						EntityRef.getEntityRef(checkoutData.results[0].entity, function(entityRefHtml){
							ItemCheckoutView.checkedOutByLabel.html(entityRefHtml);
						});
					}
				}))

				switch (checkoutData.checkoutDetails.checkedOutFor.type){
					case "OQM_ENTITY":
						EntityRef.getEntityRef(
							checkoutData.checkoutDetails.checkedOutFor.entity,
							function(refHtml){
								ItemCheckoutView.checkedOutForLabel.append(refHtml);
							}
						);
						break;
					case "EXT_SYS_USER":
						ItemCheckoutView.checkedOutForLabel.append(
							$("<h6>Id:</h6>"),
							$("<p></p>").text(checkoutData.checkoutDetails.checkedOutFor.externalId)
						);

						if(checkoutData.checkedOutFor.name){
							ItemCheckoutView.checkedOutForLabel.append(
								$("<h6>Name:</h6>"),
								$("<p></p>").text(checkoutData.checkoutDetails.checkedOutFor.name)
							);
						}
						break;
				}

				KeywordAttUtils.processKeywordDisplay(ItemCheckoutView.viewKeywordsSection, checkoutData.keywords);
				KeywordAttUtils.processAttDisplay(ItemCheckoutView.viewAttsSection, checkoutData.attributes);
				ItemCheckoutView.checkedOutOn.text(checkoutData.checkoutDate);

				if(checkoutData.dueBack){
					ItemCheckoutView.dueBackOnContainer.show();
					ItemCheckoutView.dueBackOn.text(checkoutData.dueBack);

					if(
						checkoutData.stillCheckedOut &&
						luxon.DateTime.fromISO(checkoutData.dueBack) < luxon.DateTime.local()
					){
						ItemCheckoutView.dueBackOnContainer.addClass("bg-danger");
					}
				}

				if(checkoutData.stillCheckedOut){
					ItemCheckoutView.checkinButton.data("checkoutId", itemCheckoutId);
					ItemCheckoutView.checkinButton.show();
					ItemCheckoutView.statusLabel.html(Icons.itemCheckout + " Out");
				} else {
					ItemCheckoutView.statusLabel.html(Icons.itemCheckin + " In");
					ItemCheckoutView.statusLabelContainer.addClass("bg-success");
				}

				if(checkoutData.reason){
					ItemCheckoutView.reason.text(checkoutData.reason);
					ItemCheckoutView.reasonContainer.show();
				}
				if(checkoutData.notes){
					ItemCheckoutView.notes.text(checkoutData.notes);
					ItemCheckoutView.notesContainer.show();
				}

				promises.push(Rest.call({
					url: Rest.passRoot + "/inventory/item/" + checkoutData.item,
					method: "GET",
					async: false,
					failMessagesDiv: ItemCheckoutView.messages,
					done: function (itemData) {
						ItemCheckoutView.itemName.append(
							Links.getItemViewLink(checkoutData.item, itemData.name)
						);
					}
				}));

				getStorageBlockLabel(checkoutData.fromBlock, function (label){
					ItemCheckoutView.checkedOutFrom.append(
						Links.getStorageViewLink(checkoutData.fromBlock, label)
					);
				});

				switch(checkoutData.type){
					case "AMOUNT":
						ItemCheckoutView.checkedOut.append(
							UnitUtils.quantityToDisplayStr(checkoutData.checkedOut)
						);
						break;
					case "WHOLE":
						ItemCheckoutView.checkedOut.append(
							StoredView.getStoredViewContent(
								checkoutData.checkedOut,
								checkoutData.item,
								checkoutData.fromBlock,
								false,
								false,
								false,
								true
							)
						);
						break;
				}


				if(!checkoutData.stillCheckedOut){
					Carousel.processImagedObjectImages(checkoutData.checkInDetails, ItemCheckoutView.checkinDetailsCarousel);
					ItemCheckoutView.checkinDetailsTime.text(checkoutData.checkInDetails.checkinDateTime);
					KeywordAttUtils.processKeywordDisplay(ItemCheckoutView.checkinDetailsKeywordsSection, checkoutData.checkInDetails.keywords);
					KeywordAttUtils.processAttDisplay(ItemCheckoutView.checkinDetailsAttsSection, checkoutData.checkInDetails.attributes);

					if(checkoutData.checkInDetails.notes) {
						ItemCheckoutView.checkinDetailsNotes.text(checkoutData.checkInDetails.notes);
						ItemCheckoutView.checkinDetailsNotesContainer.show();
					}

					switch (checkoutData.checkInDetails.checkinType){
						case "RETURN":
							ItemCheckoutView.checkinDetailsTypeContainer.addClass("bg-success");
							ItemCheckoutView.checkinDetailsType.text("Returned");

							getStorageBlockLabel(checkoutData.fromBlock, function (label){
								ItemCheckoutView.checkinDetailsCheckedInto.append(
									Links.getStorageViewLink(checkoutData.checkInDetails.storageBlockCheckedInto, label)
								);
								ItemCheckoutView.checkinDetailsCheckedIntoContainer.show();
							});

							break;
						case "LOSS":
							ItemCheckoutView.checkinDetailsTypeContainer.addClass("bg-danger");
							ItemCheckoutView.checkinDetailsType.text("Loss");

							if(checkoutData.checkInDetails.reason){
								ItemCheckoutView.checkinDetailsLossReason.text(checkoutData.checkInDetails.reason);
								ItemCheckoutView.checkinDetailsLossReasonContainer.show();
							}
							break;
					}

					promises.push(Rest.call({
						url: Rest.passRoot + "/inventory/item-checkout/" + itemCheckoutId + "/history?eventType=ITEM_CHECKIN",
						method: "GET",
						async: false,
						failMessagesDiv: ItemCheckoutView.messages,
						done: function (checkoutData) {
							console.log("Checkout history for checkins: ", checkoutData);
							EntityRef.getEntityRef(checkoutData.results[0].entity, function(entityRefHtml){
								ItemCheckoutView.checkinDetailsCheckedInBy.html(entityRefHtml);
							});
						}
					}))

					ItemCheckoutView.checkinDetailsContainer.show();
				}
			}
		});

		setupHistorySearch(ItemCheckoutView.history, itemCheckoutId);
	}
};


ItemCheckoutView.itemCheckoutViewModal[0].addEventListener("hidden.bs.modal", function () {
	UriUtils.removeParam("view");
});

if (UriUtils.getParams.has("view")
) {
	ItemCheckoutView.setupView(UriUtils.getParams.get("view"));
	ItemCheckoutView.viewBsModal.show();
}