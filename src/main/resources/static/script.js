
const API = "/api";

// LOGIN
const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        try {
            const response = await fetch(`${API}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                alert("Login failed. Check your email and password.");
                return;
            }

            window.location.href = "/dashboard.html";
        } catch (error) {
            alert("Could not connect to the backend. Make sure Spring Boot is running.");
            console.error(error);
        }
    });
}

// DASHBOARD: load current user and files
if (window.location.pathname.endsWith("/dashboard.html")) {
    loadCurrentUser();
    loadFiles();
}

async function loadCurrentUser() {
    try {
        const response = await fetch(`${API}/auth/me`, {
            credentials: "include"
        });

        if (!response.ok) {
            window.location.href = "/login.html";
            return;
        }

        const email = (await response.text()).replaceAll('"', '');
        const name = document.querySelector(".profile b");
        const details = document.querySelector(".profile small");

        if (name) name.textContent = email.split("@")[0];
        if (details) details.textContent = email;
    } catch (error) {
        console.error("User loading failed:", error);
    }
}

async function loadFiles() {
    const container = document.getElementById("uploadedFiles");
    if (!container) return;

    try {
        const response = await fetch(`${API}/files`, {
            credentials: "include"
        });

        if (!response.ok) {
            container.textContent = "Could not load files.";
            return;
        }

        const files = await response.json();
        container.replaceChildren();

        if (files.length === 0) {
            container.textContent = "No files uploaded yet.";
            return;
        }

        files.forEach(file => {
            const row = document.createElement("div");
            row.className = "file-row";

            const name = document.createElement("span");
            name.textContent = file.fileName;

            const download = document.createElement("button");
            download.textContent = "Download";
            download.addEventListener("click", () =>
                downloadFile(file.id, file.fileName)
            );

            const remove = document.createElement("button");
            remove.textContent = "Delete";
            remove.addEventListener("click", () =>
                deleteFile(file.id)
            );

            row.append(name, download, remove);
            container.appendChild(row);
        });
    } catch (error) {
        console.error("File loading failed:", error);
        container.textContent = "Could not connect to the backend.";
    }
}

// UPLOAD
const fileUpload = document.getElementById("fileUpload");

if (fileUpload) {
    fileUpload.addEventListener("change", async () => {
        const file = fileUpload.files[0];
        if (!file) return;

        const data = new FormData();
        data.append("file", file);

        try {
            const response = await fetch(`${API}/files/upload`, {
                method: "POST",
                credentials: "include",
                body: data
            });

            if (!response.ok) {
                alert("Upload failed.");
                return;
            }

            alert("File uploaded successfully!");
            await loadFiles();
        } catch (error) {
            console.error(error);
            alert("Upload failed. Check the backend.");
        } finally {
            fileUpload.value = "";
        }
    });
}

// DOWNLOAD
async function downloadFile(id, fileName) {
    try {
        const response = await fetch(`${API}/files/${id}/download`, {
            credentials: "include"
        });

        if (!response.ok) {
            alert("Download failed.");
            return;
        }

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");

        link.href = url;
        link.download = fileName;
        link.click();

        URL.revokeObjectURL(url);
    } catch (error) {
        console.error(error);
        alert("Download failed.");
    }
}

// SOFT DELETE
async function deleteFile(id) {
    if (!confirm("Delete this file?")) return;

    try {
        const response = await fetch(`${API}/files/${id}`, {
            method: "DELETE",
            credentials: "include"
        });

        if (!response.ok) {
            alert("Delete failed.");
            return;
        }

        await loadFiles();
    } catch (error) {
        console.error(error);
        alert("Delete failed.");
    }
}

// LOGOUT
const logoutButton = document.querySelector(".logout-button");

if (logoutButton) {
    logoutButton.addEventListener("click", async event => {
        event.preventDefault();

        try {
            await fetch(`${API}/auth/logout`, {
                method: "POST",
                credentials: "include"
            });
        } finally {
            window.location.href = "/index.html";
        }
    });
}