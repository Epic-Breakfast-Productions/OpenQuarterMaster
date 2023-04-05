const ItemListView = {
	itemListReviewMessages: $("#itemListReviewMessages"),
	applyButton: $("#applyButton"),
	applyLockButton: $("#applyLockButton"),
	itemListReviewModalTitle: $("#itemListReviewModalTitle"),
	itemListReviewModalBody: $("#itemListReviewModalBody"),
	itemListReviewCreatorRef: $("#itemListReviewCreatorRef"),
	itemListReviewCreatedTime: $("#itemListReviewCreatedTime"),
	itemListReviewEditButtonContainer: $("#itemListReviewEditButtonContainer"),
	itemListReviewEditButton: $("#itemListReviewEditButton"),
	itemListReviewId: $("#itemListReviewId"),
	itemListReviewDescContainer: $("#itemListReviewDescContainer"),
	itemListReviewDesc: $("#itemListReviewDesc"),

	setupReview(itemListId, showEdit= true) {
		console.log("Setting up item list review for list " + itemListId);

		ItemListView.itemListReviewId.text(itemListId);
		if(showEdit){
			ItemListView.itemListReviewEditButtonContainer.show();
			ItemListView.itemListReviewEditButton.prop("href", "/itemList/" + itemListId);
		} else {
			ItemListView.itemListReviewEditButtonContainer.hide();
		}
		ItemListView.itemListReviewModalTitle.text("");
		ItemListView.itemListReviewCreatorRef.text("");
		ItemListView.itemListReviewCreatedTime.text("");
		ItemListView.itemListReviewDescContainer.hide();
		ItemListView.itemListReviewDesc.text("");

		doRestCall({
			url: "/api/v1/inventory/item-list/" + itemListId,
			failMessagesDiv: ItemListView.itemListReviewMessages,
			spinnerContainer: ItemListView.itemListReviewModalBody,
			done: async function (data) {
				ItemListView.itemListReviewModalTitle.text(data.name);
				if(data.description) {
					ItemListView.itemListReviewDescContainer.show();
					ItemListView.itemListReviewDesc.text(data.description);
				}
				//TODO:: list out actions
			}
		});
		//TODO:: creator, created datetime
	},
	toggleApplyLock(){
		console.log("Toggling apply lock. Current state: " + ItemListView.applyLockButton.data("locked"));
		if(!ItemListView.applyLockButton.data("locked") || ItemListView.applyLockButton.data("locked") === "true"){
			console.log("Unlocking apply button.");
			ItemListView.applyLockButton.data("locked", "false");
			ItemListView.applyLockButton.html(Icons.unlocked);
			ItemListView.applyButton.removeClass("disabled");
		} else {
			console.log("Locking apply button.");
			ItemListView.applyLockButton.data("locked", "true");
			ItemListView.applyLockButton.html(Icons.locked);
			ItemListView.applyButton.addClass("disabled");
		}
	},
	applyList(){
		if(!confirm("Are you sure?\nThis cannot be undone.")){
			console.log("User chose to not apply.")
			return false;
		}
		console.log("User chose to apply.");
		//TODO:: call endpoint to apply
	}
};