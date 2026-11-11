
const API_URL = "http://127.0.0.1:8080/api";

const bookingsContainer = document.getElementById("bookings-container");
const bookingsMessage = document.getElementById("bookings-message");

// Check whether the logged-in user is a customer
async function checkCustomerAccess() {
    try {
        const response = await fetch(`${API_URL}/auth/me`, {
            credentials: "include"
        });

        if (!response.ok) {
            window.location.href = "login.html";
            return false;
        }

        const user = await response.json();

        if (user.role !== "ROLE_CUSTOMER") {
            if (user.role === "ROLE_ADMIN") {
                window.location.href = "/frontend/admin/admin.bookings.html";
            } else {
                window.location.href = "login.html";
            }

            return false;
        }

        return true;

    } catch (error) {
        console.error("Access verification failed:", error);
        window.location.href = "login.html";
        return false;
    }
}

// Cancel a booking
async function cancelBooking(bookingId) {
    const confirmed = confirm(
        "Are you sure you want to cancel this booking?"
    );

    if (!confirmed) {
        return;
    }

    try {
        // Get CSRF token
        const csrfResponse = await fetch(`${API_URL}/csrf`, {
            credentials: "include"
        });

        if (!csrfResponse.ok) {
            throw new Error("Unable to get security token.");
        }

        const csrfData = await csrfResponse.json();

        // Send cancellation request
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
                    status: "CANCELLED"
                })
            }
        );

        if (response.status === 401) {
            window.location.href = "login.html";
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to cancel booking.");
        }

        bookingsMessage.textContent =
            "Booking cancelled successfully.";

        await loadBookings();

    } catch (error) {
        console.error("Cancellation error:", error);
        bookingsMessage.textContent = error.message;
    }
}

// Load customer bookings
async function loadBookings() {
    try {
        const response = await fetch(`${API_URL}/bookings`, {
            credentials: "include"
        });

        if (response.status === 401) {
            window.location.href = "login.html";
            return;
        }

        if (response.status === 403) {
            bookingsMessage.textContent =
                "You are not authorized to view these bookings.";
            bookingsContainer.innerHTML = "";
            return;
        }

        if (!response.ok) {
            throw new Error("Unable to load bookings.");
        }

        const bookings = await response.json();

        bookingsContainer.innerHTML = "";

        if (bookings.length === 0) {
            bookingsContainer.innerHTML =
                "<p>You have not made any bookings yet.</p>";
            return;
        }

        bookings.forEach(booking => {
            const card = document.createElement("div");
            card.className = "booking-card";

            const serviceName = booking.service?.name ?? "N/A";
            const customerId = booking.customer?.id ?? "N/A";
            const providerName = booking.provider?.name ?? "N/A";

            const bookingDate = booking.bookingDateTime
                ? new Date(booking.bookingDateTime).toLocaleString()
                : "N/A";

            const price = booking.price ??
                booking.service?.price ?? "N/A";

            const heading = document.createElement("h3");
heading.textContent = serviceName;

card.appendChild(heading);

const details = [
    ["Booking ID", booking.id],
    ["Customer ID", customerId],
    ["Provider", providerName],
    ["Date & Time", bookingDate],
    ["Price", price],
    ["Status", booking.status]
];

details.forEach(([label, value]) => {
    const paragraph = document.createElement("p");

    const strong = document.createElement("strong");
    strong.textContent = `${label}: `;

    paragraph.append(strong, document.createTextNode(value));
    card.appendChild(paragraph);
});

if (booking.status === "PENDING") {
    const cancelButton = document.createElement("button");

    cancelButton.className = "cancel-btn";
    cancelButton.textContent = "Cancel Booking";

    cancelButton.addEventListener("click", () => {
        cancelBooking(booking.id);
    });

    card.appendChild(cancelButton);
}

            bookingsContainer.appendChild(card);
        });

    } catch (error) {
        console.error("Booking loading error:", error);
        bookingsMessage.textContent = error.message;
        bookingsContainer.innerHTML = "";
    }
}

// Verify access before loading bookings
checkCustomerAccess().then(isCustomer => {
    if (isCustomer) {
        loadBookings();
    }
});
