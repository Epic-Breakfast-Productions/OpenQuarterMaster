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
			done: function (data) {
				let promises = [];

				processKeywordDisplay(ItemCheckoutView.viewKeywordsSection, data.keywords);
				processAttDisplay(ItemCheckoutView.viewAttsSection, data.attributes);

				if(data.stillCheckedOut){
					ItemCheckoutView.statusLabel.html(Icons.itemCheckout + " Out");
				} else {
					ItemCheckoutView.statusLabel.html(Icons.itemCheckin + " In");
					ItemCheckoutView.statusLabelContainer.addClass("bg-success");
				}

				promises.push(doRestCall({
					url: "/api/v1/inventory/item/" + data.item,
					method: "GET",
					async: false,
					failMessagesDiv: ItemCheckin.messages,
					done: function (itemData) {
						ItemCheckoutView.itemName.append(
							Links.getItemViewLink(data.item, itemData.name)
						);
					}
				}));

				getStorageBlockLabel(data.checkedOutFrom, function (label){
					ItemCheckoutView.checkedOutFrom.append(
						Links.getStorageViewLink(data.checkedOutFrom, label)
					);
				});
				ItemCheckoutView.checkedOut.append(
					StoredView.getStoredViewContent(
						data.checkedOut,
						data.item,
						data.checkedOutFrom,
						false,
						false,
						false,
						true
					)
				);

				//TODO:: checkout date
				//TODO:: checkin due date
				//TODO:: Who for/by
				//TODO:: if checked in, show details
				//TODO:: reason, notes
			}
		});

		setupHistorySearch(ItemCheckoutView.history, itemCheckoutId);
	}
};