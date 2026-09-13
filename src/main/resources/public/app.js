// Client for the Networking Lab mini web application.
//
// Every action here is asynchronous: it prevents the browser's default
// full-page submission, fires a fetch() request to one of the server's
// hardcoded JSON services, and updates only the result/error area once the
// response arrives. The page itself never reloads and stays interactive
// while a request is pending -- but the *server* still handles only one
// connection at a time, which section 6.2 of the lab asks you to observe.

const loadingEl = document.getElementById("loading");
const resultEl = document.getElementById("result");
const errorEl = document.getElementById("error");

function showLoading() {
    loadingEl.hidden = false;
    resultEl.hidden = true;
    errorEl.hidden = true;
}

function showResult(data) {
    loadingEl.hidden = true;
    errorEl.hidden = true;
    resultEl.hidden = false;
    resultEl.textContent = JSON.stringify(data, null, 2);
}

function showError(message) {
    loadingEl.hidden = true;
    resultEl.hidden = true;
    errorEl.hidden = false;
    errorEl.textContent = message;
}

/**
 * Calls one of the server's hardcoded GET services and renders the result.
 * HTTP-level errors (4xx/5xx) are distinguished from network failures
 * (server unreachable, DNS failure, connection reset) so the user sees a
 * meaningful message either way, without any server implementation detail
 * leaking into the page.
 */
async function callService(path, params) {
    showLoading();
    const url = new URL(path, window.location.origin);
    Object.entries(params || {}).forEach(([key, value]) => url.searchParams.set(key, value));

    let response;
    try {
        response = await fetch(url, { method: "GET" });
    } catch (networkError) {
        showError("Network error: could not reach the server. Is it running?");
        return;
    }

    let body = null;
    try {
        body = await response.json();
    } catch (parseError) {
        // Fall through with body === null; handled below per status.
    }

    if (!response.ok) {
        const detail = body && body.error ? body.error : `HTTP ${response.status} ${response.statusText}`;
        showError(detail);
        return;
    }

    showResult(body);
}

document.getElementById("greeting-form").addEventListener("submit", (event) => {
    event.preventDefault();
    const name = document.getElementById("greeting-name").value;
    callService("/api/greeting", { name });
});

document.getElementById("square-form").addEventListener("submit", (event) => {
    event.preventDefault();
    const value = document.getElementById("square-value").value;
    callService("/api/square", { value });
});

document.getElementById("time-button").addEventListener("click", () => {
    callService("/api/time");
});

document.getElementById("slow-form").addEventListener("submit", (event) => {
    event.preventDefault();
    const seconds = document.getElementById("slow-seconds").value;
    callService("/api/slow", { seconds });
});

// A best-effort health check on load; failure here is not fatal to the page.
(async () => {
    const indicator = document.getElementById("health-indicator");
    try {
        const response = await fetch("/api/health");
        const body = await response.json();
        indicator.textContent = response.ok
            ? `Server status: ${body.status}`
            : "Server status: unavailable";
    } catch (e) {
        indicator.textContent = "Server status: unreachable";
    }
})();
