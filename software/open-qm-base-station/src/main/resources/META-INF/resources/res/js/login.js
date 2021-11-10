var jwtCookieName = "jwt";

function login(jwtVal, remember){
    Cookies.set(jwtCookieName, jwtVal);
    window.location.replace("/overview");
}

function logout(){
    Cookies.remove(jwtCookieName);
    window.location.replace("/");
}

function getToken(usernameEmail, password, rememberUser){
    console.log("Getting token for user: " + usernameEmail);
    var loginRequestData = {
            usernameEmail: $("#emailUsernameInput").val(),
            password: $("#passwordInput").val(),
            extendedExpire: rememberUser
        };
    var result = false;
    $.ajax({
            url: "/api/user/auth",
            data: JSON.stringify(loginRequestData),
            contentType: "application/json; charset=UTF-8",
            dataType: 'json',
            async: false,
            type: "POST"
        }).done(function(data) {
            console.log("Response from login request: " + JSON.stringify(data));
            result = data.token;
        });
    return result;
}

/**
 * Determines if the token is valid and provides actual authorization.
 * Returns bool
 */
function checkToken(jwtToken){
    console.log("Checking validity of token: " + jwtToken);
    var valid = false;
    $.ajax({
        url: "/api/user/auth/tokenCheck",
        headers: {"Authorization": "Bearer " + jwtToken},
        async: false,
        type: "GET",
        success: function(data) {
            var returned = jQuery.parseJSON(data);
            valid = returned.hadToken && !returned.expired;
        }
    });
    console.log("Result: " + valid);
    return valid;
}


/**
 * Checks if the user is logged in or not.
 * returns bool, if the user was logged in or not
 */
function isLoggedIn(){
    var jwtVal = Cookies.get(jwtCookieName);
    console.log("User token: " + jwtVal);
    if(!jwtVal){
        return false;
    }
    return checkToken(jwtVal);
}

function assertLoggedIn(){
    if(!isLoggedIn()){
        logout();
    }
}

function assertNotLoggedIn(){
    if(isLoggedIn()){
        window.location.replace("/overview");
    } else {
        Cookies.remove(jwtCookieName);
    }

}

$("#logoutButton").click(function(){
    logout();
});