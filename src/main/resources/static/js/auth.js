
// ==========================
// REGISTER
// ==========================

const registerForm = document.getElementById("registerForm");

if (registerForm) {

    registerForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const userName = document.getElementById("userName").value;
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;

        const message = document.getElementById("message");

        try {

            const response = await fetch("/api/auth/register", {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    userName: userName,
                    email: email,
                    password: password
                })
            });

            const data = await response.text();

            if (response.ok) {

                message.textContent = "Registration successful!";
                message.style.color = "green";

                registerForm.reset();

                setTimeout(() => {
                    window.location.href = "login.html";
                }, 1000);

            } else {

                message.textContent = data;
                message.style.color = "red";
            }

        } catch (error) {

            message.textContent = "Something went wrong.";
            message.style.color = "red";

            console.error(error);
        }
    });
}


// ==========================
// LOGIN
// ==========================

const loginForm = document.getElementById("loginForm");

if (loginForm) {

    loginForm.addEventListener("submit", async function (event) {

        event.preventDefault();

        const email = document.getElementById("loginEmail").value;
        const password = document.getElementById("loginPassword").value;

        const message = document.getElementById("loginMessage");

        try {

            const response = await fetch("/api/auth/login", {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                credentials: "include",

                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });

            const data = await response.text();

            if (response.ok) {

                message.textContent = "Login successful!";
                message.style.color = "green";

                setTimeout(() => {
                    window.location.href = "dashboard.html";
                }, 1000);

            } else {

                message.textContent = data;
                message.style.color = "red";
            }

        } catch (error) {

            message.textContent = "Something went wrong.";
            message.style.color = "red";

            console.error(error);
        }
    });
}


// ==========================
// LOGOUT
// ==========================

const logoutButton = document.getElementById("logout");

if (logoutButton) {

    logoutButton.addEventListener("click", async function () {

        try {

            const response = await fetch("/api/auth/logout", {
                method: "POST",

                credentials: "include"
            });

            if (response.ok) {

                window.location.href = "login.html";

            } else {

                console.error("Logout failed");
            }

        } catch (error) {

            console.error("Logout error:", error);
        }
    });
}