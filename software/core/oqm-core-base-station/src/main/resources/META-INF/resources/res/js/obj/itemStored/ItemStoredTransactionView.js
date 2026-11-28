import {Rest} from "../../Rest.js";
import {PageUtility} from "../../utilClasses/PageUtility.js";

export class ItemStoredTransactionView extends PageUtility {

	static Search = class {
		static getSearchResultContainer = function(searchContainerJq){
			return searchContainerJq.find(".itemStoredTransactionSearchResults");
		}

		static async runSearch(transactionSearchFormJs, event, spinner = true) {
			event.preventDefault();
			event.stopPropagation();

			let transactionSearchForm = $(transactionSearchFormJs);
			let searchContainer = transactionSearchForm
				.parent()
				.parent()
				.parent()
				.parent()
				.parent();

			let historySearchResults = ItemStoredTransactionView.Search.getSearchResultContainer(searchContainer);

			const formData = new FormData(transactionSearchFormJs);
			formData.delete("objectId");

			let url = transactionSearchForm.attr("action") + "?" + new URLSearchParams(formData).toString();

			return Rest.call({
				spinnerContainer: searchContainer.get(0),
				url: url,
				method: 'GET',
				failNoResponse: null,
				failNoResponseCheckStatus: true,
				returnType: "html",
				extraHeaders: {
					"accept": "text/html",
					"searchFormId": transactionSearchForm.attr("id"),
					// "inputIdPrepend": itemSearchSelectModal.attr("data-bs-inputIdPrepend"),
				},
				// async: false,
				done: function (data) {
					historySearchResults.html(data);
				}
			});
		}
	}

	static {
		window.ItemStoredTransactionView = this;
	}
}
