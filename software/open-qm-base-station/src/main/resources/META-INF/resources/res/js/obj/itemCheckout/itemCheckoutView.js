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
	history: $("#itemCheckoutViewHistoryAccordionCollapse"),
	viewKeywordsSection: $("#itemCheckoutViewKeywordsSection"),
	viewAttsSection: $("#itemCheckoutViewAttsSection"),


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
		resetHistorySearch(ItemCheckoutView.history);
		clearHideKeywordDisplay(ItemCheckoutView.viewKeywordsSection);
		clearHideAttDisplay(ItemCheckoutView.viewAttsSection);
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

				//TODO:: Who for/by
				//TODO:: if checked in, show details
				//TODO:: reason, notes
			}
		});

		setupHistorySearch(ItemCheckoutView.history, itemCheckoutId);
	}
};