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

				//TODO:: get create history event for checkout to show who checked out


				switch (checkoutData.checkedOutFor.type){
					case "OQM_ENTITY":
						EntityRef.getEntityRef(
							checkoutData.checkedOutFor.entity,
							function(refHtml){
								ItemCheckoutView.checkedOutForLabel.append(refHtml);
							}
						);
						break;
					case "EXT_SYS_USER":
						ItemCheckoutView.checkedOutForLabel.append(
							$("<h6>Id:</h6>"),
							$("<p></p>").text(checkoutData.checkedOutFor.externalId)
						);

						if(checkoutData.checkedOutFor.name){
							ItemCheckoutView.checkedOutForLabel.append(
								$("<h6>Name:</h6>"),
								$("<p></p>").text(checkoutData.checkedOutFor.name)
							);
						}
						break;
				}

				processKeywordDisplay(ItemCheckoutView.viewKeywordsSection, checkoutData.keywords);
				processAttDisplay(ItemCheckoutView.viewAttsSection, checkoutData.attributes);
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
					//TODO:: checked in by
					Carousel.processImagedObjectImages(checkoutData.checkInDetails, ItemCheckoutView.checkinDetailsCarousel);
					ItemCheckoutView.checkinDetailsTime.text(checkoutData.checkInDetails.checkinDateTime);
					processKeywordDisplay(ItemCheckoutView.checkinDetailsKeywordsSection, checkoutData.checkInDetails.keywords);
					processAttDisplay(ItemCheckoutView.checkinDetailsAttsSection, checkoutData.checkInDetails.attributes);

					if(checkoutData.checkInDetails.notes) {
						ItemCheckoutView.checkinDetailsNotes.text(checkoutData.checkInDetails.notes);
						ItemCheckoutView.checkinDetailsNotesContainer.show();
					}

					switch (checkoutData.checkInDetails.checkinType){
						case "RETURN":
							ItemCheckoutView.checkinDetailsTypeContainer.addClass("bg-success");
							ItemCheckoutView.checkinDetailsType.text("Returned");

							getStorageBlockLabel(checkoutData.checkedOutFrom, function (label){
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

					ItemCheckoutView.checkinDetailsContainer.show();
				}

				//TODO:: Who for/by
			}
		});

		setupHistorySearch(ItemCheckoutView.history, itemCheckoutId);
	}
};