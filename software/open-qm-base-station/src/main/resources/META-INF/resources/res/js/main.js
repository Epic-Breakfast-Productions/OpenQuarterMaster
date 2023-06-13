
var navSearchInput = $('#navSearchInput');
var navSearchForm = $('#navSearchForm');
var navSearchTypeSelect = $('#navSearchTypeSelect');

navSearchTypeSelect.on("change", function(event){
    console.log(
        "Changing nav form to " +
        "action: " + navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.action +
        " with fieldName: " + navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.field
    );
    navSearchForm.attr("action", navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.action);
    navSearchInput.attr("name", navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.field);
});


let nowDateTimeStamp = new Date().toISOString().slice(0, 16);
$(".datetimeInputFuture").each(function(i, element){
    element.min = nowDateTimeStamp;
});