
const API_URL = "http://127.0.0.1:8080/api";

const serviceContainer = document.getElementById("selected-service");
const providerSelect = document.getElementById("providerId");
const bookingForm = document.getElementById("booking-form");
const bookingMessage = document.getElementById("booking-message");

const params = new URLSearchParams(window.location.search);
const serviceId = params.get("serviceId");

// Keep the selected service ID available for booking submission.
let selectedService = null;

// Set the minimum booking date to today.
const dateInput = document.getElementById("bookingDate");

const today = new Date();

const localToday = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, "0"),
    String(today.getDate()).padStart(2, "0")
].join("-");

dateInput.min = localToday;


// Load selected service.
async function loadSelectedService() {
    if (!serviceId || !/^\d+$/.test(serviceId)) {
        serviceContainer.textContent =
            "Please select a service from the Services page.";
        return;
    }

    try {
        const response = await fetch(
            `${API_URL}/services/${encodeURIComponent(serviceId)}`
        );

        if (!response.ok) {
            throw new Error("Service not found.");
        }

        selectedService = await response.json();

        serviceContainer.replaceChildren();

        const name = document.createElement("h4");
        name.textContent = selectedService.name;

        const description = document.createElement("p");
        description.textContent = selectedService.description;

        const price = document.createElement("p");
        price.textContent = `Price: ₹${selectedService.price}`;

        const category = document.createElement("p");
        category.textContent = `Category: ${selectedService.category}`;

        serviceContainer.append(
            name,
            description,
            price,
            category
        );

    } catch (error) {
        console.error("Error loading service:", error);

        serviceContainer.textContent =
            "Unable to load this service. Please return to the Services page.";
    }
}


// Load available providers.
async function loadProviders() {
    try {
        const response = await fetch(`${API_URL}/providers`);

        if (!response.ok) {
            throw new Error("Unable to fetch providers.");
        }

        const providers = await response.json();

        providerSelect.replaceChildren();

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "Select a provider";

        providerSelect.appendChild(placeholder);

        const availableProviders = providers.filter(
            provider => provider.available === true
        );

        availableProviders.forEach(provider => {
            const option = document.createElement("option");

            option.value = provider.id;

            option.textContent =
                `${provider.name} — ${provider.location}`;

            providerSelect.appendChild(option);
        });

        if (availableProviders.length === 0) {
            placeholder.textContent =
                "No providers currently available";
        }

    } catch (error) {
        console.error("Error loading providers:", error);

        providerSelect.replaceChildren();

        const option = document.createElement("option");
        option.value = "";
        option.textContent = "Unable to load providers";

        providerSelect.appendChild(option);
    }
}


// Initialize the page.
loadSelectedService();
loadProviders();


// Submit booking.
bookingForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    if (!selectedService) {
        bookingMessage.textContent =
            "Please select a valid service.";
        return;
    }

    const customerId = Number(
        document.getElementById("customerId").value
    );

    const providerId = Number(providerSelect.value);

    const bookingDate = dateInput.value;

    const bookingTime =
        document.getElementById("bookingTime").value;

    if (!customerId || !providerId || !bookingDate || !bookingTime) {
        bookingMessage.textContent =
            "Please fill in all booking details.";
        return;
    }

    const bookingDateTime =
        `${bookingDate}T${bookingTime}:00`;

    const bookingData = {
    serviceId: Number(selectedService.id),
    providerId: Number(providerId),
    bookingDateTime: bookingDateTime
};

    try {
        bookingMessage.textContent = "Submitting booking...";

        // Get CSRF token.
        const csrfResponse = await fetch(`${API_URL}/csrf`, {
            credentials: "include"
        });

        if (!csrfResponse.ok) {
            throw new Error("Unable to obtain CSRF token.");
        }

        const csrfData = await csrfResponse.json();

        // Submit booking.
        const response = await fetch(`${API_URL}/bookings`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                [csrfData.headerName]: csrfData.token
            },
            body: JSON.stringify(bookingData)
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(
                result.message ||
                result.detail ||
                result.error ||
                "Booking failed."
            );
        }

        bookingMessage.textContent =
            `Booking successful! Booking ID: ${result.id}`;

        bookingForm.reset();

        dateInput.min = localToday;

    } catch (error) {
        console.error("Booking error:", error);

        bookingMessage.textContent = error.message;
    }
});
