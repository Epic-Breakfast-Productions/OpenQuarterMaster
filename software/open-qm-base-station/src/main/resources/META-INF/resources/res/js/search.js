


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
    console.log("DONE filling in query form from page query.");
}