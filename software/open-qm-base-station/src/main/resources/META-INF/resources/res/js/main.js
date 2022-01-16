
var navSearchInput = $('#navSearchInput');
var navSearchForm = $('#navSearchForm');
var navSearchSelectButton = $('#navSearchSelectButton');

function setNavSearch(action, name, caller){
    navSearchForm.attr("action", action);
    navSearchInput.attr("name", name);
    navSearchSelectButton.text(caller.textContent);
}

