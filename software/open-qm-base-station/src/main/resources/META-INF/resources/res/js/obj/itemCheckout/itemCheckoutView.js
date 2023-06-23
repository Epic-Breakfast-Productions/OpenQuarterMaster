const ItemCheckoutView = {
	itemCheckoutViewModal: $("#itemCheckoutViewModal"),
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
	checkinDetailsReturnedContainer: $("#itemCheckoutViewCheckinDetailsReturnedContainer"),
	checkinDetailsLossContainer: $("#itemCheckoutViewCheckinDetailsLossContainer"),
	checkinDetailsTime: $("#itemCheckoutViewCheckinDetailsTime"),
	checkinDetailsNotesContainer: $("#itemCheckoutViewCheckinDetailsNotesContainer"),
	checkinDetailsNotes: $("#itemCheckoutViewCheckinDetailsNotes"),
	checkinDetailsCarousel: $("#itemCheckoutViewCheckinDetailsCarousel"),

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
		ItemCheckoutView.checkinDetailsReturnedContainer.hide();
		ItemCheckoutView.checkinDetailsLossContainer.hide();
		ItemCheckoutView.checkinDetailsTime.text("");
		ItemCheckoutView.checkinDetailsNotesContainer.hide();
		ItemCheckoutView.checkinDetailsNotes.text("");

		resetHistorySearch(ItemCheckoutView.history);
		clearHideKeywordDisplay(ItemCheckoutView.viewKeywordsSection);
		clearHideAttDisplay(ItemCheckoutView.viewAttsSection);
		clearHideKeywordDisplay(ItemCheckoutView.checkinDetailsKeywordsSection);
		clearHideAttDisplay(ItemCheckoutView.checkinDetailsAttsSection);
	},
	async setupView(itemCheckoutId){
		console.log("Setting up view for item checkout " + itemCheckoutId);
		this.resetCheckoutView();

		ItemCheckoutView.viewId.text(itemCheckoutId);

		await doRestCall({
			spinnerContainer: ItemCheckin.modal,
			url: "/api/v1/inventory/item-checkout/" + itemCheckoutId,
			method: "GET",
			async: false,
			failMessagesDiv: ItemCheckin.messages,
			done: function (checkoutData) {
				let promises = [];

				processKeywordDisplay(ItemCheckoutView.viewKeywordsSection, checkoutData.keywords);
				processAttDisplay(ItemCheckoutView.viewAttsSection, checkoutData.attributes);
				ItemCheckoutView.checkedOutOn.text(checkoutData.checkoutDate);

				if(checkoutData.dueBack){
					ItemCheckoutView.dueBackOnContainer.show();
					ItemCheckoutView.dueBackOn.text(checkoutData.dueBack);

					if(luxon.DateTime.fromISO(checkoutData.dueBack) < luxon.DateTime.local()){
						ItemCheckoutView.dueBackOnContainer.addClass("bg-danger");
					}
				}

				if(checkoutData.stillCheckedOut){
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

				promises.push(doRestCall({
					url: "/api/v1/inventory/item/" + checkoutData.item,
					method: "GET",
					async: false,
					failMessagesDiv: ItemCheckin.messages,
					done: function (itemData) {
						ItemCheckoutView.itemName.append(
							Links.getItemViewLink(checkoutData.item, itemData.name)
						);
					}
				}));

				getStorageBlockLabel(checkoutData.checkedOutFrom, function (label){
					ItemCheckoutView.checkedOutFrom.append(
						Links.getStorageViewLink(checkoutData.checkedOutFrom, label)
					);
				});
				ItemCheckoutView.checkedOut.append(
					StoredView.getStoredViewContent(
						checkoutData.checkedOut,
						checkoutData.item,
						checkoutData.checkedOutFrom,
						false,
						false,
						false,
						true
					)
				);

				if(!checkoutData.stillCheckedOut){
					//TODO:: checked in by, checkin type, associated type fields
					ItemCheckoutView.checkinDetailsTime.text(checkoutData.checkInDetails.checkinDateTime);
					processKeywordDisplay(ItemCheckoutView.checkinDetailsKeywordsSection, checkoutData.checkInDetails.keywords);
					processAttDisplay(ItemCheckoutView.checkinDetailsAttsSection, checkoutData.checkInDetails.attributes);

					if(checkoutData.checkInDetails.notes) {
						ItemCheckoutView.checkinDetailsNotes.text(checkoutData.checkInDetails.notes);
						ItemCheckoutView.checkinDetailsNotesContainer.show();
					}

					Carousel.processImagedObjectImages(checkoutData.checkInDetails, ItemCheckoutView.checkinDetailsCarousel);

					ItemCheckoutView.checkinDetailsContainer.show();
				}

				//TODO:: Who for/by
			}
		});

		setupHistorySearch(ItemCheckoutView.history, itemCheckoutId);
	}
};