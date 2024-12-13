const navSearchInput = $('#navSearchInput');
const navSearchForm = $('#navSearchForm');
const navSearchTypeSelect = $('#navSearchTypeSelect');

function updateNavSearchDestination(action, icon, fieldName){
    navSearchForm.attr("action", action);
    navSearchTypeSelect.html(icon);
    navSearchInput.attr("name", fieldName);
}

TimeHelpers.setupDateTimeInputs();

const Main = {
    processCount: 0,
    processStart(){
        this.processCount++;
    },
    processStop(){
        this.processCount--;
    },
    processesRunning(){
        return this.processCount !== 0;
    },
    noProcessesRunning(){
        return !this.processesRunning();
    }
}