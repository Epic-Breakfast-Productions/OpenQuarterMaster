function isLoggedIn() {
  return Boolean(
    localStorage.getItem("oqm_user_token")
  );
}

async function login() {
  console.log("Login clicked");

  // Temporary login behavior
  localStorage.setItem(
    "oqm_user_token",
    "example-token-123"
  );
}

async function logout() {
  console.log("Logout clicked");

  localStorage.removeItem("oqm_user_token");
}