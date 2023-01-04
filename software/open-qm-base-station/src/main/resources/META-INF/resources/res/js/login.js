var loginFormMessages = $("#loginFormMessages");
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
    window.location.replace("/api/v1/auth/user/logout");
}

async function getToken(usernameEmail, password, rememberUser){
    console.log("Getting token for user: " + usernameEmail);
    var loginRequestData = {
            usernameEmail: $("#emailUsernameInput").val(),
            password: $("#passwordInput").val(),
            extendedExpire: rememberUser
        };
    var result = false;


    await doRestCall({
        url: "/api/v1/auth/user",
        method: "POST",
        data: loginRequestData,
        async: true,
        done: function(data) {
            console.log("Response from login request: " + JSON.stringify(data));
            result = data.token;
        },
        failMessagesDiv: loginFormMessages
    });

    return result;
}

/**
 * Determines if the token is valid and provides actual authorization.
 * Returns bool
 */
async function checkToken(jwtToken){
    console.log("Checking validity of token: " + jwtToken);
    var valid = false;

    await doRestCall({
        spinnerContainer: null,
        url: "/api/v1/auth/tokenCheck",
        async: true,
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
async function isLoggedIn() {
    var jwtVal = Cookies.get(jwtCookieName);
    console.log("User token: " + jwtVal);
    if (!jwtVal) {
        return false;
    }
    return await checkToken(jwtVal);
}

function assertLoggedIn(){
    if(!isLoggedIn()){
        logout();
    }
}

async function assertNotLoggedIn() {
    if (await isLoggedIn()) {
        window.location.replace("/overview");
    } else {
        Cookies.remove(jwtCookieName);
    }

}

$("#logoutButton").click(function(){
    logout();
});