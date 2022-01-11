
function paginationClick(formId, page){
    console.log("Paginating. Form Id: \"" + formId, "\", page: " + page);
    var searchForm = $("#" + formId);

    searchForm.find('input[name="pageNum"]').val(page);

    searchForm.submit();
}

function fillInQueryForm(queryForm){
    var formInputs = queryForm.find('input');
    var getParams = new URLSearchParams(window.location.search);

    console.log("Filling in query form from page query.");
    formInputs.each(function(ind, formInput){
        if(getParams.has(formInput.name)){
            $(formInput).val(getParams.get(formInput.name));
        }
    });
    var keywordAddButton = queryForm.find(".keywordAddButton");
    getParams.forEach(function(value, key){
        if(key === "keyword" && keywordAddButton){
            console.log("Keyword: " + value);
            keywordAddButton.trigger('click');
            queryForm.find(".keywordInputDiv").find(":input.keywordInput").last().val(value);
        }
    });


    console.log("DONE filling in query form from page query.");
}

function resetToOne(pageNumInputId){
    console.log("page num input reset to 1");
    $("#" + pageNumInputId).val(1);
}

$(".pagingSearchForm").each(function(i, form){

    var pageNumInputId = $(form).find('input[name="pageNum"]').get(0).id;
    $(form).find(":input").each(function(i2, input){
        if(input.name != "pageNum"){
            input.addEventListener('change', function() {
                resetToOne(pageNumInputId);
            });
        }
    });
});

