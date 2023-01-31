
function getHistorySearchForm(historyViewContainer){
	return historyViewContainer.find(".historySearchForm");
}

function getHistorySearchResults(historyViewContainer){
	return historyViewContainer.find(".historyViewResults");
}

/**
 *
 * @param historyViewContainer
 */
function resetHistorySearch(historyViewContainer){
	getHistorySearchResults(historyViewContainer).text('');
	getHistorySearchForm(historyViewContainer)[0].reset();

}

function setupHistorySearch(historyViewContainer, id){
	resetHistorySearch(historyViewContainer);

	let historySearchForm = getHistorySearchForm(historyViewContainer);

	historySearchForm.find("input[name='objectId']").val(id);

	historySearchForm.submit();
}


async function runHistorySearch(historySearchFormJs, event){
	event.preventDefault();

	let historySearchForm = $(historySearchFormJs);
	let historySearchContainer = historySearchForm
		.parent()
		.parent()
		.parent()
		.parent()
		.parent();

	let historySearchResults = getHistorySearchResults(historySearchContainer);

	const formData = new FormData(historySearchFormJs);
	formData.delete("objectId");

	let historyUrl = historySearchForm.attr("action") +
		historySearchForm.find("input[name='objectId']").val() +
		"/history?" +
		new URLSearchParams(formData).toString();

	return doRestCall({
		spinnerContainer: historySearchContainer.get(0),
		url: historyUrl,
		method: 'GET',
		failNoResponse: null,
		failNoResponseCheckStatus: true,
		extraHeaders: {
			"accept": "text/html",
			"searchFormId": historySearchForm.id,
			// "inputIdPrepend": itemSearchSelectModal.attr("data-bs-inputIdPrepend"),
		},
		// async: false,
		done: function(data){
			historySearchResults.html(data);
		}
	});
}
