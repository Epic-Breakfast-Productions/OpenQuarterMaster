const navSearchInput = $('#navSearchInput');
const navSearchForm = $('#navSearchForm');
const navSearchTypeSelect = $('#navSearchTypeSelect');

navSearchTypeSelect.on("change", function(event){
    console.log(
        "Changing nav form to " +
        "action: " + navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.action +
        " with fieldName: " + navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.field
    );
    navSearchForm.attr("action", navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.action);
    navSearchInput.attr("name", navSearchTypeSelect[0].options[event.target.selectedIndex].dataset.field);
});

TimeHelpers.setupDateTimeInputs();