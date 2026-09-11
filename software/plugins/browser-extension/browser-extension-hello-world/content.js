(async function () {
  // 1. Close sidebar if it already exists
  const existingHost = document.getElementById("ext-sidebar-host");
  if (existingHost) {
    existingHost.remove();
    return;
  }

  // 2. Create host and attach Shadow DOM
  const host = document.createElement("div");
  host.id = "ext-sidebar-host";
  document.documentElement.appendChild(host);

  const shadow = host.attachShadow({ mode: "open" });

  // 3. Inject HTML and CSS FIRST so elements exist in the DOM
  shadow.innerHTML = `
    <style>
      :host { all: initial; }

      .hidden {
        display: none !important;
      }

      .sidebar {
        position: fixed;
        top: 0;
        right: 0;
        height: 100vh;
        width: 220px;
        background: #ffffff;
        border-left: 1px solid #d0d0d0;
        box-shadow: -2px 0 6px rgba(0, 0, 0, 0.08);
        z-index: 2147483647;
        font-family: system-ui, -apple-system, sans-serif;
        transition: width 0.15s ease;
        box-sizing: border-box;
      }

      .sidebar.collapsed {
        width: 16px;
        border-left: 1px solid #d0d0d0;
        box-shadow: none;
      }

      .toggle-btn {
        position: absolute;
        top: 12px;
        left: -14px;
        width: 28px;
        height: 28px;
        border-radius: 50%;
        border: 1px solid #d0d0d0;
        background: #ffffff;
        cursor: pointer;
        font-size: 13px;
        line-height: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0;
      }

      .toggle-btn:hover {
        background: #f0f0f0;
      }

      .content {
        padding: 40px 12px 12px 12px;
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .sidebar.collapsed .content {
        display: none;
      }

      button.action {
        padding: 8px 10px;
        font-size: 13px;
        border: 1px solid #d0d0d0;
        border-radius: 4px;
        background: #f7f7f7;
        cursor: pointer;
        text-align: left;
        color: #222;
        font-family: inherit;
      }

      button.action:hover:not(:disabled) {
        background: #ededed;
      }

      button.action:disabled {
        color: #a0a0a0;
        cursor: not-allowed;
        background: #f2f2f2;
      }
    </style>

    <div class="sidebar" id="sidebar">
      <button class="toggle-btn" id="toggle-btn" title="Collapse">&rsaquo;</button>
      <div class="content">
        <p>OQM Shopping Cart Integrator</p>
        <button class="action" id="authenticate-btn" title="Log in to your OQM account">Login</button>
        <button class="action" id="logout-btn" title="Log out of your OQM account">Logout</button>
        <button class="action" id="choose-db-btn" title="Pick a database to connect to">Choose Database</button>
        <button class="action" id="scan-btn" title="Automatically populates the data fields with info from your shopping cart">Scan Page</button>
        <button class="action" id="feedback-btn" title="Provide feedback about the extension">Feedback</button>
        <button class="action" id="integrate-btn" disabled title="Try scanning the page first">Request integration with this site</button>
      </div>
    </div>
  `;

  // 4. Query elements from Shadow DOM NOW that they exist
  const sidebar = shadow.getElementById("sidebar");
  const toggleBtn = shadow.getElementById("toggle-btn");
  const authBtn = shadow.getElementById("authenticate-btn");
  const logoutBtn = shadow.getElementById("logout-btn");

  // 5. Auth State Manager
  async function updateAuthState() {
    const isLoggedIn = Boolean(localStorage.getItem("oqm_user_token"));

    if (isLoggedIn) {
      authBtn.classList.add("hidden");
      logoutBtn.classList.remove("hidden");
    } else {
      authBtn.classList.remove("hidden");
      logoutBtn.classList.add("hidden");
    }
  }

  // Initialize auth UI state immediately
  await updateAuthState();

  // 6. Bind Event Listeners
  toggleBtn.addEventListener("click", () => {
    const collapsed = sidebar.classList.toggle("collapsed");
    toggleBtn.innerHTML = collapsed ? "&lsaquo;" : "&rsaquo;";
    toggleBtn.title = collapsed ? "Expand" : "Collapse";
  });

  authBtn.addEventListener("click", async () => {
    console.log("Login clicked");
    localStorage.setItem("oqm_user_token", "example-token-123");
    await updateAuthState();
  });

  logoutBtn.addEventListener("click", async () => {
    console.log("Logout clicked");
    localStorage.removeItem("oqm_user_token");
    await updateAuthState();
  });

  shadow.getElementById("scan-btn").addEventListener("click", () => {
    console.log("Scan Page clicked");
    shadow.getElementById("integrate-btn").disabled = false;
  });

  shadow.getElementById("choose-db-btn").addEventListener("click", () => {
    console.log("Choose Database clicked");
  });

  shadow.getElementById("integrate-btn").addEventListener("click", () => {
    console.log("Request integration clicked");
  });

  shadow.getElementById("feedback-btn").addEventListener("click", () => {
    console.log("Feedback clicked");
  });
})();