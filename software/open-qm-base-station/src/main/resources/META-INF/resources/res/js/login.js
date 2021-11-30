var jwtCookieName = "jwt";

function login(jwtVal, remember){
    Cookies.set(jwtCookieName, jwtVal);
    var getParams = new URLSearchParams(window.location.search);

    window.location.replace(
    (getParams.has("returnPath")?getParams.get("returnPath"):"/overview")
    );
}

function logout(){
    Cookies.remove(jwtCookieName);
    window.location.replace("/api/user/auth/logout");
}

function getToken(usernameEmail, password, rememberUser){
    console.log("Getting token for user: " + usernameEmail);
    var loginRequestData = {
            usernameEmail: $("#emailUsernameInput").val(),
            password: $("#passwordInput").val(),
            extendedExpire: rememberUser
        };
    var result = false;


    doRestCall({
        url: "/api/user/auth",
        method: "POST",
        data: loginRequestData,
        async: false,
        done: function(data) {
            console.log("Response from login request: " + JSON.stringify(data));
            result = data.token;
        }
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

    doRestCall({
        spinnerContainer: null,
        url: "/api/user/auth/tokenCheck",
        async: false,
        authorization: jwtToken,
        done: function(data){
            console.log("Got response from getting token check request: " + JSON.stringify(data));
            valid = data.hadToken && !data.expired;
        },
        fail: function(data){
            console.warn("Bad response from token check attempt: " + JSON.stringify(data));
        },
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