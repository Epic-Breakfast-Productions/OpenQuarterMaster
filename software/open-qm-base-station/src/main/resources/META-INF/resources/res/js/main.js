
var navSearchSelect = $('#navSearchSelect');
var navSearchInput = $('#navSearchInput');
var navSearchForm = $('#navSearchForm');

function setNavSearch(){
    var split = navSearchSelect.val().split(";");
    navSearchForm.attr("action", split[0]);
    navSearchInput.attr("name", split[1]);
}

