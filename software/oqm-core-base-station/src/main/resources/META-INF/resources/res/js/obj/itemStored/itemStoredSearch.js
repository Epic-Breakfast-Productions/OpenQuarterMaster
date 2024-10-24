ItemStoredSearch = {
    //TODO:: handle searching
    search: async function (searchFormJs, event, resultsContainerSelector) {
        event.preventDefault();
        console.log("Searching for items stored.");
        let searchFormJq = $(searchFormJs);
        let resultsContainer = $(resultsContainerSelector);
        let searchContainer = searchFormJq
            .parent()
            .parent()
            .parent()
            .parent()
            .parent();

        const formData = new FormData(searchFormJs);
        let itemId = formData.get("item");

        formData.delete("itemName");
        formData.delete("item");

        let searchUrl = Rest.passRoot + `/inventory/item/${itemId}/stored?` + new URLSearchParams(formData).toString();

        return Rest.call({
            spinnerContainer: searchContainer.get(0),
            url: searchUrl,
            method: 'GET',
            failNoResponse: null,
            failNoResponseCheckStatus: true,
            returnType: "html",
            extraHeaders: {
                "accept": "text/html",
                "searchFormId": searchFormJq.attr("id"),
                // "inputIdPrepend": itemSearchSelectModal.attr("data-bs-inputIdPrepend"),
            },
            // async: false,
            done: function (data) {
                resultsContainer.html(data);
            }
        });
    }
}