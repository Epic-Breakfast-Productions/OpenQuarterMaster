

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

function displayObjHistory(container, historyList){
    console.log("Displaying image history.");
    historyList.forEach(function(curEvent){
        var curhistRow = $("<tr></tr>");

        curhistRow.append($("<td>"+curEvent.timestamp+"</td>"));
        var userTd = $('<td data-user-id="'+curEvent.userId+'">'+curEvent.userId+"</td>");
        curhistRow.append(userTd);
        curhistRow.append($("<td>"+curEvent.type+"</td>"));
        curhistRow.append($("<td>"+curEvent.description+"</td>"));

        container.append(curhistRow);

        doRestCall({
            spinnerContainer: container[0],
            url: "/api/user/" + curEvent.userId,
            method: "GET",
            async: true,
            done: function(data) {
                console.log("Response from create request: " + JSON.stringify(data));
                userTd.text(data.username);
            },
            fail: function(data) {
                console.warn("Bad response from user data get request: " + JSON.stringify(data));
                addMessageToDiv(imageViewMessages, "danger", "Failed to get info on user: " + data.responseText, "Failed", null);
            }
        });
    });
}