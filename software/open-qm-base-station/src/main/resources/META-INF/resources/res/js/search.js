
function paginationClick(formId, page){
    console.log("Paginating. Form Id: \"" + formId, "\", page: " + page);
    var searchForm = $("#" + formId);

    searchForm.find('input[name="pageNum"]').val(page);

    searchForm.submit();
}

function fillInQueryForm(queryForm){
    var getParams = new URLSearchParams(window.location.search);

    console.log("Filling in query form from page query.");
    {
        let formInputs = queryForm.find('input');
        formInputs.each(function (ind, formInput) {
            if (getParams.has(formInput.name)) {
                $(formInput).val(getParams.get(formInput.name));
            }
        });
    }
    {
        let formSelects = queryForm.find('select');
        formSelects.each(function (ind, formSelect) {
            let multi = formSelect.multiple;
            let value = (multi ? getParams.getAll(formSelect.name): getParams.get(formSelect.name));

            if (getParams.has(formSelect.name)) {
                if(formSelect.classList.contains("dselect-select")){
                    Dselect.setValues($(formSelect), value);
                } else {
                    $(formSelect).val(value);
                }
            }
        });
    }
    
    

    var keywordAddButton = queryForm.find(".keywordAddButton");
    if(keywordAddButton){
        getParams.getAll("keyword").forEach(function(curKeyword){
            console.log("Keyword: " + curKeyword);
            keywordAddButton.trigger('click');
            queryForm.find(".keywordInputDiv").find(":input.keywordInput").last().val(curKeyword);
        });
    } else {
        console.log("no keywords in search");
    }
    var attributeAddButton = queryForm.find(".attributeAddButton");
    if(attributeAddButton){
        var attKeys = getParams.getAll("attributeKey");
        var attVals = getParams.getAll("attributeValue");

        attKeys.forEach(function(curKeyword, i){
            console.log("attribute: " + curKeyword);
            attributeAddButton.trigger('click');
            var attInputDiv = queryForm.find(".attInputDiv");
            attInputDiv.find(":input.attInputKey").last().val(curKeyword);
            attInputDiv.find(":input.attInputValue").last().val(attVals[i]);
        });
    } else {
        console.log("No attributes in search");
    }
    var capacityAddButton = queryForm.find(".capacityAddButton");
    if(attributeAddButton){
        var capacities = getParams.getAll("capacity");
        var units = getParams.getAll("unit");

        capacities.forEach(function(curCapacity, i){
            var curUnit = units[i];
            console.log("Capacity: " + curCapacity + curUnit);
            capacityAddButton.trigger('click');
            var capacityInputDiv = queryForm.find(".capacityInputDiv");
            capacityInputDiv.find(":input.capacityInput").last().val(curCapacity);
            capacityInputDiv.find(":input.unitSelect").last().val(curUnit);
        });
    } else {
        console.log("No attributes in search");
    }

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

