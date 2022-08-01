

function displayKeywordsIn(container, keywords){
    keywords.forEach(function(keyword){
        console.log("Keyword: " + keyword);
        container.append(
            $('<span class="badge bg-secondary m-2 user-select-all">'+keyword+'</span> ')
        );
    });
}

function clearHideKeywordDisplay(container){
    container.hide();
    container.find('.keywordsViewContainer').html("");
}

function processKeywordDisplay(container, keywords){
    clearHideKeywordDisplay(container);
    if(keywords.length > 0){
        console.log("had keywords");
        container.show();
        displayKeywordsIn(container.find('.keywordsViewContainer'), keywords);
    } else {
        console.log("did not have keywords.");
    }
}

function displayAttsIn(container, attributes){
    Object.entries(attributes).forEach(entry => {
        const [key, val] = entry;
        console.log("Att: " + key + "/" + val);
        container.append($('<span class="badge bg-secondary m-2"><span class="user-select-all">'+key+'</span> <i class="fas fa-equals"></i> <code class="user-select-all">'+ val + '</code></span> '));
    });
}

function clearHideAttDisplay(container){
    container.hide();
    container.find('.attsViewContainer').html("");
}

function processAttDisplay(container, attributes){
    clearHideAttDisplay(container);
    if(Object.keys(attributes).length > 0){
        console.log("had attributes");
        viewAttsSection.show();
        displayAttsIn(container.find('.attsViewContainer'), attributes)
    } else {
        console.log("did not have attributes.");
    }
}

function displayObjHistory(container, historyObjectEndpoint){

    doRestCall({
        url: "/api" + historyObjectEndpoint + "/history",
        method: "GET",
        async: false,
        extraHeaders: {
            "accept": "text/html"
        },
        done: function (historyObject) {
            console.log("Displaying object history.");
            container.html(historyObject);
        }
    });


}