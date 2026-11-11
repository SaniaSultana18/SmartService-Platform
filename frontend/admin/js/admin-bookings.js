// Check whether the logged-in user is an admin
async function checkAdminAccess() {
    try {
        const response = await fetch(
            "http://127.0.0.1:8080/api/auth/me",
            {
                credentials: "include"
            }
        );

        if (!response.ok) {
            window.location.href = "../login.html";
            return false;
        }

        const user = await response.json();

        if (user.role !== "ROLE_ADMIN") {
            alert("Access denied! Admins only.");
            window.location.href = "../my-bookings.html";
            return false;
        }

        return true;

    } catch (error) {
        alert("Unable to verify your login.");
        window.location.href = "../login.html";
        return false;
    }
}
const API_URL = "http://127.0.0.1:8080/api";

const tableBody = document.getElementById("bookingTableBody");
const message = document.getElementById("message");

// Load all bookings
async function loadBookings() {
    try {
        const response = await fetch(`${API_URL}/bookings`, {
            credentials: "include"
        });

        if (response.status === 401 || response.status === 403) {
            message.textContent = "Please log in with an authorized account.";
            tableBody.innerHTML = "";
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to load bookings");
        }

        const bookings = await response.json();

        updateStatistics(bookings);
        displayBookings(bookings);

    } catch (error) {
        message.textContent = error.message;
        tableBody.innerHTML = "";
    }
}

// Update dashboard statistics
function updateStatistics(bookings) {
    document.getElementById("totalBookings").textContent =
        bookings.length;

    document.getElementById("pendingBookings").textContent =
        bookings.filter(b => b.status === "PENDING").length;

    document.getElementById("confirmedBookings").textContent =
        bookings.filter(b => b.status === "CONFIRMED").length;

    document.getElementById("completedBookings").textContent =
        bookings.filter(b => b.status === "COMPLETED").length;
}

// Display bookings in table

function displayBookings(bookings) {
    tableBody.innerHTML = "";

    if (bookings.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 7;
        cell.textContent = "No bookings found.";

        row.appendChild(cell);
        tableBody.appendChild(row);
        return;
    }

    bookings.forEach(booking => {
        const row = document.createElement("tr");

        const customerName = booking.customer?.name ?? "N/A";
        const serviceName = booking.service?.name ?? "N/A";
        const providerName = booking.provider?.name ?? "N/A";
        const date = new Date(booking.bookingDateTime).toLocaleString();

        const values = [
            booking.id,
            customerName,
            serviceName,
            providerName,
            date,
            booking.status
        ];

        values.forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value;
            row.appendChild(cell);
        });

        const actionCell = document.createElement("td");

        if (booking.status === "PENDING" ||
            booking.status === "CONFIRMED") {

            const select = document.createElement("select");
            select.className = "status-select";
            select.id = `status-${booking.id}`;

            const statuses = booking.status === "PENDING"
                ? ["CONFIRMED", "CANCELLED"]
                : ["COMPLETED", "CANCELLED"];

            statuses.forEach(status => {
                const option = document.createElement("option");
                option.value = status;
                option.textContent = status;
                select.appendChild(option);
            });

            const button = document.createElement("button");
            button.className = "update-btn";
            button.textContent = "Update";
            button.addEventListener("click", () => {
                updateBookingStatus(booking.id);
            });

            actionCell.append(select, button);
        } else {
            actionCell.textContent = "No further changes";
        }

        row.appendChild(actionCell);
        tableBody.appendChild(row);
    });
}


// Update booking status
async function updateBookingStatus(bookingId) {
    const select = document.getElementById(`status-${bookingId}`);
    const newStatus = select.value;

    try {
        const csrfResponse = await fetch(`${API_URL}/csrf`, {
            credentials: "include"
        });

        const csrfData = await csrfResponse.json();

        const response = await fetch(
            `${API_URL}/bookings/${bookingId}/status`,
            {
                method: "PUT",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    [csrfData.headerName]: csrfData.token
                },
                body: JSON.stringify({
                    status: newStatus
                })
            }
        );

        if (!response.ok) {
            throw new Error("Status update failed: " + response.status);
        }

        message.textContent =
            `Booking ${bookingId} updated successfully.`;

        await loadBookings();

    } catch (error) {
        message.textContent = error.message;
    }
}
// Check access before loading bookings
checkAdminAccess().then(isAdmin => {
    if (isAdmin) {
        loadBookings();
    }
});
document.getElementById("logoutBtn").addEventListener("click", async function () {
    try {
        const csrfResponse = await fetch(`${API_URL}/csrf`, {
            credentials: "include"
        });

        const csrfData = await csrfResponse.json();

        const logoutResponse = await fetch(`${API_URL}/users/logout`, {
            method: "POST",
            credentials: "include",
            headers: {
                [csrfData.headerName]: csrfData.token
            }
        });

        if (logoutResponse.ok) {
            alert("Logged out successfully!");
            window.location.href = "../login.html";
        } else {
            alert("Logout failed. Please try again.");
        }
    } catch (error) {
        console.error("Logout error:", error);
        alert("Something went wrong during logout.");
    }
});