

function displayKeywordsIn(container, keywords){
    keywords.forEach(function(keyword){
        console.log("Keyword: " + keyword);
        container.append(
            $('<span class="badge bg-light text-dark m-2 user-select-all">'+keyword+'</span> ')
        );
    });
}

function processKeywordDisplay(display, container, keywords){
    if(keywords.length > 0){
        console.log("had keywords");
        viewKeywordsTextSection.show();
        displayKeywordsIn(viewKeywordsText, keywords);
    }
}

function displayAttsIn(container, attributes){
    Object.entries(attributes).forEach(entry => {
        const [key, val] = entry;
        console.log("Att: " + key + "/" + val);
        viewAttsSectionText.append($('<span class="badge bg-light text-dark m-2"><span class="user-select-all">'+key + '</span> <i class="fas fa-equals"></i> <code class="user-select-all">'+ val + '</code></span> '));
    });
}

function processAttDisplay(display, container, attributes){
    if(Object.keys(attributes).length > 0){
        console.log("had attributes");
        viewAttsSection.show();

        displayAttsIn(container, attributes)
    }
}