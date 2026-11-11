
const API_URL = "http://127.0.0.1:8080/api";

async function loadDashboard() {
    const totalBookings = document.getElementById("totalBookings");
    const completedBookings = document.getElementById("completedBookings");
    const recentBookings = document.getElementById("recentBookings");

    try {
        const response = await fetch(`${API_URL}/bookings`, {
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("Unable to load booking data.");
        }

        const bookings = await response.json();

        // Update booking statistics
        totalBookings.textContent = bookings.length;

        completedBookings.textContent = bookings.filter(
            booking => booking.status === "COMPLETED"
        ).length;

        // Show the five most recent bookings
        recentBookings.innerHTML = "";

        const latestBookings = [...bookings]
            .sort((a, b) => new Date(b.bookingDateTime) - new Date(a.bookingDateTime))
            .slice(0, 5);

        if (latestBookings.length === 0) {
            recentBookings.textContent = "No bookings found.";
            return;
        }

        latestBookings.forEach(booking => {
            const card = document.createElement("div");
            card.className = "booking-card";

            const details = [
                `Customer: ${booking.customer?.name ?? "N/A"}`,
                `Service: ${booking.service?.name ?? "N/A"}`,
                `Provider: ${booking.provider?.name ?? "N/A"}`,
                `Status: ${booking.status}`
            ];

            details.forEach(detail => {
                const paragraph = document.createElement("p");
                paragraph.textContent = detail;
                card.appendChild(paragraph);
            });

            recentBookings.appendChild(card);
        });

    } catch (error) {
        totalBookings.textContent = "—";
        completedBookings.textContent = "—";
        recentBookings.textContent = error.message;
    }
}

// Load dashboard data
loadDashboard();


document.getElementById("logoutBtn").addEventListener("click", async function (event) {
    event.preventDefault();

    try {
        const csrfResponse = await fetch(`${API_URL}/csrf`, {
            credentials: "include"
        });

        if (!csrfResponse.ok) {
            throw new Error("Unable to obtain CSRF token.");
        }

        const csrfData = await csrfResponse.json();

        const logoutResponse = await fetch(`${API_URL}/users/logout`, {
            method: "POST",
            credentials: "include",
            headers: {
                [csrfData.headerName]: csrfData.token
            }
        });

        if (!logoutResponse.ok) {
            throw new Error("Logout failed.");
        }

        alert("Logged out successfully!");
        window.location.href = "../login.html";

    } catch (error) {
        alert(error.message);
    }
});
