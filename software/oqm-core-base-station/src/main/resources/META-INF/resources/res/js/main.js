const navSearchInput = $('#navSearchInput');
const navSearchForm = $('#navSearchForm');
const navSearchTypeSelect = $('#navSearchTypeSelect');

function updateNavSearchDestination(action, icon, fieldName){
    navSearchForm.attr("action", action);
    navSearchTypeSelect.html(icon);
    navSearchInput.attr("name", fieldName);
}

TimeHelpers.setupDateTimeInputs();