function createSidebar() {
  // 1. Create host and attach Shadow DOM

  const host = document.createElement("div");
  host.id = "ext-sidebar-host";

  document.documentElement.appendChild(host);

  const shadow = host.attachShadow({
    mode: "open"
  });

  // 2. Create sidebar HTML

  shadow.innerHTML = `
    <div class="sidebar" id="sidebar">
      <button
        class="toggle-btn"
        id="toggle-btn"
        title="Collapse"
      >
        &rsaquo;
      </button>

      <div class="content">
        <p>OQM Shopping Cart Integrator</p>

        <button
          class="action"
          id="authenticate-btn"
          title="Log in to your OQM account"
        >
          Login
        </button>

        <button
          class="action"
          id="logout-btn"
          title="Log out of your OQM account"
        >
          Logout
        </button>

        <button
          class="action"
          id="choose-db-btn"
          title="Pick a database to connect to"
        >
          Choose Database
        </button>

        <button
          class="action"
          id="scan-btn"
          title="Automatically populates the data fields with info from your shopping cart"
        >
          Scan Page
        </button>

        <button
          class="action"
          id="feedback-btn"
          title="Provide feedback about the extension"
        >
          Feedback
        </button>

        <button
          class="action"
          id="integrate-btn"
          disabled
          title="Try scanning the page first"
        >
          Request integration with this site
        </button>
      </div>
    </div>
  `;

  // 3. Load CSS into the Shadow DOM

  const style = document.createElement("link");

  style.rel = "stylesheet";
  style.href = chrome.runtime.getURL(
    "frontend/sidebar.css"
  );

  shadow.prepend(style);

  // 4. Get UI elements

  const sidebar = shadow.getElementById("sidebar");
  const toggleBtn = shadow.getElementById("toggle-btn");

  const authBtn =
    shadow.getElementById("authenticate-btn");

  const logoutBtn =
    shadow.getElementById("logout-btn");

  const scanBtn =
    shadow.getElementById("scan-btn");

  const integrateBtn =
    shadow.getElementById("integrate-btn");

  const chooseDbBtn =
    shadow.getElementById("choose-db-btn");

  const feedbackBtn =
    shadow.getElementById("feedback-btn");

  // 5. Update authentication UI

  updateAuthUI(authBtn, logoutBtn);

  // 6. Collapse / expand

  toggleBtn.addEventListener("click", () => {
    const collapsed =
      sidebar.classList.toggle("collapsed");

    toggleBtn.innerHTML = collapsed
      ? "&lsaquo;"
      : "&rsaquo;";

    toggleBtn.title = collapsed
      ? "Expand"
      : "Collapse";
  });

  // 7. Login

  authBtn.addEventListener("click", async () => {
    await login();

    updateAuthUI(authBtn, logoutBtn);
  });

  // 8. Logout

  logoutBtn.addEventListener("click", async () => {
    await logout();

    updateAuthUI(authBtn, logoutBtn);
  });

  // 9. Scan

  scanBtn.addEventListener("click", async () => {
    const cartData = await scanPage();

    if (cartData && cartData.success) {
      integrateBtn.disabled = false;

      // Save the scan so the integration button
      // knows what data to use.
      integrateBtn.cartData = cartData;
    }
  });

  // 10. Database

  chooseDbBtn.addEventListener("click", () => {
    console.log("Choose Database clicked");
  });

  // 11. Integration

  integrateBtn.addEventListener("click", async () => {
    await requestIntegration(
      integrateBtn.cartData
    );
  });

  // 12. Feedback

  feedbackBtn.addEventListener("click", () => {
    console.log("Feedback clicked");
  });
}


// Updates which authentication buttons are visible
function updateAuthUI(authBtn, logoutBtn) {
  if (isLoggedIn()) {
    authBtn.classList.add("hidden");
    logoutBtn.classList.remove("hidden");
  } else {
    authBtn.classList.remove("hidden");
    logoutBtn.classList.add("hidden");
  }
}