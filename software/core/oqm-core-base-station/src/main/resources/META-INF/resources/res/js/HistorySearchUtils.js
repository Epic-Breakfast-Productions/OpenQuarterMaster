import {Rest} from "./Rest.js";

export const HistorySearchUtils = {
	getHistorySearchForm(historyViewContainer) {
		return historyViewContainer.find(".historySearchForm");
	},

	getHistorySearchResults(historyViewContainer) {
		return historyViewContainer.find(".historyViewResults");
	},

	/**
	 *
	 * @param historyViewContainer
	 */
	resetHistorySearch(historyViewContainer) {
		HistorySearchUtils.getHistorySearchResults(historyViewContainer).text('');
		HistorySearchUtils.getHistorySearchForm(historyViewContainer)[0].reset();

	},

	setupHistorySearch(historyViewContainer, id) {
		HistorySearchUtils.resetHistorySearch(historyViewContainer);

		let historySearchForm = HistorySearchUtils.getHistorySearchForm(historyViewContainer);

		historySearchForm.find("input[name='objectId']").val(id);

		historySearchForm.submit();
	},

	runHistorySearch: async function (historySearchFormJs, event) {
		event.preventDefault();

		let historySearchForm = $(historySearchFormJs);
		let historySearchContainer = historySearchForm
			.parent()
			.parent()
			.parent()
			.parent()
			.parent();

		let historySearchResults = HistorySearchUtils.getHistorySearchResults(historySearchContainer);

		const formData = new FormData(historySearchFormJs);
		formData.delete("objectId");

		let historyUrl = historySearchForm.attr("action") +
			historySearchForm.find("input[name='objectId']").val() +
			"/history?" +
			new URLSearchParams(formData).toString();

		return Rest.call({
			spinnerContainer: historySearchContainer.get(0),
			url: historyUrl,
			method: 'GET',
			failNoResponse: null,
			failNoResponseCheckStatus: true,
			returnType: "html",
			extraHeaders: {
				"accept": "text/html",
				"searchFormId": historySearchForm.attr("id"),
				// "inputIdPrepend": itemSearchSelectModal.attr("data-bs-inputIdPrepend"),
			},
			// async: false,
			done: function (data) {
				historySearchResults.html(data);
			}
		});
	}

}
