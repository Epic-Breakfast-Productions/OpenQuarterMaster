const ItemCheckoutView = {
	itemCheckoutViewModal: $("#itemCheckoutViewModal"),
	viewBsModal: $("#itemCheckoutViewModal").length ? new bootstrap.Modal($("#itemCheckoutViewModal"), {}) : null,
	messages: $("#itemCheckoutViewMessages"),
	content: $("#itemCheckoutViewContent"),
	checkinButton: $("#itemCheckoutViewCheckinButton"),
	history: $("#itemCheckoutViewHistoryAccordionCollapse"),

	getCheckoutForDisplay: async function (checkedOutFor, addingToJq) {
		switch (checkedOutFor.type) {
			case "OQM_ENTITY":
				await EntityRef.getEntityRef(
					checkedOutFor.entity,
					function (refHtml) {
						addingToJq.append(refHtml);
					}
				);
				break;
			case "EXT_SYS_USER":
				addingToJq.append(
					$("<h6>Id:</h6>"),
					$("<p></p>").text(checkedOutFor.externalId)
				);

				if (checkedOutFor.name) {
					addingToJq.append(
						$("<h6>Name:</h6>"),
						$("<p></p>").text(checkedOutFor.name)
					);
				}
				break;
		}
	},
	getCheckoutDisplay(itemCheckoutData) {
		let output = $(`
	<div class="itemCheckoutViewContainer">
		<div class="row">
			<div class="col">
				<div class="row">
					<div class="card col-sm-12 col-md-6 itemCheckoutViewStatusLabelContainer">
						<div class="card-body">
							<h5 class="card-title d-inline">` + Icons.itemCheckouts + ` Checkout status:</h5>
							<p class="d-inline itemCheckoutViewStatusLabel">
							</p>
						</div>
					</div>
					<div class="card col-sm-12 col-md-6">
						<div class="card-body">
							<h5 class="card-title d-inline">Id:</h5>
							<p class="card-text d-inline">
								<small>
									<span class="itemCheckoutViewId"></span>
	<!--								TODO-->
	<!--								{#copyTextButton textContainerId='itemCheckoutViewId'}{/copyTextButton}-->
									
								</small>
							</p>
						</div>
					</div>
					<div class="card col-sm-12 col-md-6">
						<div class="card-body">
							<h5 class="card-title">` + Icons.item + ` Item:</h5>
							<p class="itemCheckoutViewItemName">
							</p>
						</div>
					</div>
					<div class="card col-sm-12 col-md-6">
						<div class="card-body">
							<h5 class="card-title">` + Icons.storageBlock + ` Checked out from:</h5>
							<p class="itemCheckoutViewCheckedOutFromLabel">
							</p>
						</div>
					</div>
					<div class="card col-sm-12 col-md-6">
						<div class="card-body">
							<h5 class="card-title">Checked out by:</h5>
							<p class="itemCheckoutViewCheckedOutByLabel">
							</p>
						</div>
					</div>
					<div class="card col-sm-12 col-md-6">
						<div class="card-body">
							<h5 class="card-title">Checked out For:</h5>
							<div class="itemCheckoutViewCheckedOutForLabel">
							</div>
						</div>
					</div>
					<div class="card col-sm-12 col-md-6">
						<div class="card-body">
							<h5 class="card-title">Checked out on:</h5>
							<p class="itemCheckoutViewCheckedOutOn">
							</p>
						</div>
					</div>
					<div class="card col-sm-12 col-md-6 itemCheckoutViewDueBackOnContainer">
						<div class="card-body">
							<h5 class="card-title">Due back:</h5>
							<p class="itemCheckoutViewDueBackOn">
							</p>
						</div>
					</div>
					<div class="card col-sm-12 col-md-6">
						<div class="card-body">
							<h5 class="card-title">Checked out:</h5>
							<div class="itemCheckoutViewCheckedOut">
							</div>
						</div>
					</div>
					
					<div class="col itemCheckoutViewKeywords">
					
					
					</div>
					<div class="col itemCheckoutViewCheckinDetailsAttsSection">
					
					</div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="card col-12 itemCheckoutViewReasonContainer">
				<div class="card-body">
					<h5 class="card-title">Reason:</h5>
					<p class="itemCheckoutViewReason">
					</p>
				</div>
			</div>
			<div class="card col-12 itemCheckoutViewNotesContainer">
				<div class="card-body">
					<h5 class="card-title">Notes:</h5>
					<p class="itemCheckoutViewNotes">
					</p>
				</div>
			</div>
		</div>
		<div class="row itemCheckoutViewCheckinDetailsContainer">
			<div class="card col-12">
				<div class="card-body">
					<h3 class="card-title">
						` + Icons.checkinTransaction + ` Checkin Details:
					</h3>
					<div class="row">
	<!--					TODO:: -->
						<!-- {#carousel id='itemCheckoutViewCheckinDetailsCarousel' carouselCss='col'}{/carousel}-->
						<div class="col">
							<div class="row">
								<div class="card col-sm-12 col-md-6 itemCheckoutViewCheckinDetailsTypeContainer">
									<div class="card-body">
										<h5 class="card-title">Checkin Type:</h5>
										<p class="itemCheckoutViewCheckinDetailsType">
										</p>
									</div>
								</div>
								<div class="card col-sm-12 col-md-6">
									<div class="card-body">
										<h5 class="card-title">Checkin Time:</h5>
										<p class="itemCheckoutViewCheckinDetailsTime">
										</p>
									</div>
								</div>
								<div class="card col-sm-12 col-md-6 itemCheckoutViewCheckinDetailsCheckedIntoContainer">
									<div class="card-body">
										<h5 class="card-title">Checked into:</h5>
										<p class="itemCheckoutViewCheckinDetailsCheckedInto">
										</p>
									</div>
								</div>
								<div class="card col-sm-12 col-md-6">
									<div class="card-body">
										<h5 class="card-title">Checked in by:</h5>
										<p class="itemCheckoutViewCheckinDetailsCheckedInBy">
										</p>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row itemCheckoutViewCheckinDetailsLossReasonContainer">
						<div class="card col-12">
							<div class="card-body">
								<h5 class="card-title">Loss Reason:</h5>
								<p class="itemCheckoutViewCheckinDetailsLossReason">
								</p>
							</div>
						</div>
					</div>
					<div class="row itemCheckoutViewCheckinDetailsNotesContainer">
						<div class="card col-12">
							<div class="card-body">
								<h5 class="card-title">Notes:</h5>
								<p class="itemCheckoutViewCheckinDetailsNotes">
								</p>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col itemCheckoutViewCheckinDetailsKeywordsSection">
						
						</div>
						<div class="col itemCheckoutViewCheckinDetailsAttsSection">
						
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
		`);
		// if (itemCheckoutData.stillCheckedOut) {
		// 	output.find(".itemCheckoutViewStatusLabel").text("Out");
		// } else {
		// 	output.find(".itemCheckoutViewStatusLabel").text("In");
		// 	output.find("itemCheckoutViewStatusLabelContainer").addClass("bg-success");
		// }
		output.find(".itemCheckoutViewId").text(itemCheckoutData.id);

		let promises = [];

		promises.push(
			EntityRef.getEntityRef(itemCheckoutData.checkedOutByEntity, function (entityRefHtml) {
				output.find(".itemCheckoutViewCheckedOutByLabel").html(entityRefHtml);
			})
		);

		promises.push(
			ItemCheckoutView.getCheckoutForDisplay(itemCheckoutData.checkoutDetails.checkedOutFor, output.find(".itemCheckoutViewCheckedOutForLabel"))
		);

		promises.push(
			KeywordAttUtils.Keywords.getNewDisplay(itemCheckoutData.keywords)
				.then(function (keywordDisplay) {
					output.find(".itemCheckoutViewKeywords").append(keywordDisplay)
				})
		);
		promises.push(
			KeywordAttUtils.Attributes.getNewDisplay(itemCheckoutData.attributes)
				.then(function (attDisplay) {
					output.find(".itemCheckoutViewAttributes").append(attDisplay)
				})
		);

		output.find(".itemCheckoutViewCheckedOutOn").text(itemCheckoutData.checkoutDate);

		if (itemCheckoutData.dueBack) {
			output.find(".itemCheckoutViewDueBackOn").text(itemCheckoutData.dueBack);

			if (
				itemCheckoutData.stillCheckedOut &&
				luxon.DateTime.fromISO(itemCheckoutData.dueBack) < luxon.DateTime.local()
			) {
				output.find(".itemCheckoutViewDueBackOnContainer").addClass("bg-danger");
			}
		} else {
			output.find(".itemCheckoutViewDueBackOnContainer").hide();
		}

		if (itemCheckoutData.stillCheckedOut) {
			output.find(".itemCheckoutViewStatusLabel").text("Out");
		} else {
			output.find(".itemCheckoutViewStatusLabel").text("In");
			output.find("itemCheckoutViewStatusLabelContainer").addClass("bg-success");
		}

		if (itemCheckoutData.reason) {
			output.find(".itemCheckoutViewReason").text(itemCheckoutData.reason);
		} else {
			output.find(".itemCheckoutViewReasonContainer").hide();
		}
		if (itemCheckoutData.notes) {
			output.find(".itemCheckoutViewNotes").text(itemCheckoutData.notes);
		} else {
			output.find(".itemCheckoutViewNotesContainer").hide();
		}

		promises.push(Rest.call({
			url: Rest.passRoot + "/inventory/item/" + itemCheckoutData.item,
			method: "GET",
			async: false,
			failMessagesDiv: ItemCheckoutView.messages,
			done: function (itemData) {
				output.find(".itemCheckoutViewItemName").append(
					Links.getItemViewLink(itemCheckoutData.item, itemData.name)
				);
			}
		}));

		getStorageBlockLabel(itemCheckoutData.fromBlock, function (label) {
			output.find(".itemCheckoutViewCheckedOutFromLabel").append(
				Links.getStorageViewLink(itemCheckoutData.fromBlock, label)
			);
		});//TODO:: from stored

		switch (itemCheckoutData.type) {
			case "AMOUNT":
				output.find(".itemCheckoutViewCheckedOut").append(
					UnitUtils.quantityToDisplayStr(itemCheckoutData.checkedOut)
				);
				break;
			case "WHOLE":
				output.find(".itemCheckoutViewCheckedOut").append(
					StoredView.getStoredViewContent(
						itemCheckoutData.checkedOut,
						{
							includeEditButton: false
						}
					)
				);
				break;
		}


		if (itemCheckoutData.stillCheckedOut) {
			console.debug("Was not checked in, hiding.");
			console.debug("checkout details container: ", output.find(".itemCheckoutViewCheckinDetailsContainer"));
			output.find(".itemCheckoutViewCheckinDetailsContainer").hide();
		} else {
			console.debug("Was checked in, displaying checkin data.");
			//TODO:: this
			// Carousel.processImagedObjectImages(itemCheckoutData.checkInDetails, ItemCheckoutView.checkinDetailsCarousel);

			output.find(".itemCheckoutViewCheckinDetailsTime").text(itemCheckoutData.checkInDetails.checkinDateTime);

			promises.push(
				KeywordAttUtils.Keywords.getNewDisplay(itemCheckoutData.checkInDetails.keywords)
					.then(function (keywordDisplay) {
						output.find(".itemCheckoutViewCheckinDetailsKeywordsSection").append(keywordDisplay)
					})
			);
			promises.push(
				KeywordAttUtils.Attributes.getNewDisplay(itemCheckoutData.checkInDetails.attributes)
					.then(function (attDisplay) {
						output.find(".itemCheckoutViewCheckinDetailsAttsSection").append(attDisplay)
					})
			);

			if (itemCheckoutData.checkInDetails.notes) {
				output.find(".itemCheckoutViewCheckinDetailsNotes").text(itemCheckoutData.checkInDetails.notes);
			} else {
				output.find(".itemCheckoutViewCheckinDetailsNotesContainer").hide();
			}

			console.debug("Checkin type: ", itemCheckoutData.checkInDetails.type);
			switch (itemCheckoutData.checkInDetails.type) {
				case "RETURN_FULL":
					output.find(".itemCheckoutViewCheckinDetailsLossReasonContainer").hide();
					output.find(".itemCheckoutViewCheckinDetailsTypeContainer").addClass("bg-success");
					output.find(".itemCheckoutViewCheckinDetailsType").text("Returned");

					getStorageBlockLabel(itemCheckoutData.fromBlock, function (label) {
						output.find(".itemCheckoutViewCheckinDetailsCheckedInto").append(
							Links.getStorageViewLink(itemCheckoutData.checkInDetails.storageBlockCheckedInto, label)
						);
					});

					break;
					//TODO:: return part
				case "LOSS":
					output.find(".itemCheckoutViewCheckinDetailsCheckedInto").hide();

					output.find(".itemCheckoutViewCheckinDetailsTypeContainer").addClass("bg-danger");
					output.find(".itemCheckoutViewCheckinDetailsType").text("Loss");

					if (itemCheckoutData.checkInDetails.reason) {
						output.find(".itemCheckoutViewCheckinDetailsLossReason").text(itemCheckoutData.checkInDetails.reason);
					}
					break;
			}

			//TODO:: checked in by

		}

		Promise.all(promises);

		return output;
	},
	resetCheckoutView() {
		ItemCheckoutView.messages.text("");
		ItemCheckoutView.content.text("");
		ItemCheckoutView.checkinButton.hide();

		resetHistorySearch(ItemCheckoutView.history);
	},
	async setupView(itemCheckoutId) {
		console.log("Setting up view for item checkout " + itemCheckoutId);
		this.resetCheckoutView();

		await Rest.call({
			spinnerContainer: ItemCheckoutView.itemCheckoutViewModal.get(0),
			url: Rest.passRoot + "/inventory/item-checkout/" + itemCheckoutId,
			method: "GET",
			async: false,
			failMessagesDiv: ItemCheckoutView.messages,
			done: async function (checkoutData) {
				console.log("Setting up view for checkout: ", checkoutData);
				let promises = [];

				if (checkoutData.stillCheckedOut) {
					ItemCheckoutView.checkinButton.data("checkoutId", itemCheckoutId);
					ItemCheckoutView.checkinButton.show();
				}

				let display = await ItemCheckoutView.getCheckoutDisplay(checkoutData);
				ItemCheckoutView.content.html(display);
			}
		});

		setupHistorySearch(ItemCheckoutView.history, itemCheckoutId);
	}
};

if (ItemCheckoutView.itemCheckoutViewModal.length) {
	ItemCheckoutView.itemCheckoutViewModal[0].addEventListener("hidden.bs.modal", function () {
		UriUtils.removeParam("view");
	});
}

if (UriUtils.getParams.has("view")) {
	ItemCheckoutView.setupView(UriUtils.getParams.get("view"));
	ItemCheckoutView.viewBsModal.show();
}