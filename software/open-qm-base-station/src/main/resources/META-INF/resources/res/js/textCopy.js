
function copyText(buttonClicked, textContainerId){
    navigator.clipboard.writeText($("#"+textContainerId).text());

    buttonClicked = $(buttonClicked);
    buttonClicked.html(Icons.copyChecked);
    setTimeout(
        function (){
            // console.log("Setting copy symbol back.");
            buttonClicked.html(Icons.copy);
        },
        5_000
    );
}
