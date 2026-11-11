
const servicesContainer = document.getElementById("services-container");

async function loadServices() {
    try {
        const response = await fetch("http://localhost:8080/api/services");

        if (!response.ok) {
            throw new Error("Failed to fetch services");
        }

        const services = await response.json();

        servicesContainer.innerHTML = "";

        if (services.length === 0) {
            servicesContainer.textContent = "No services available.";
            return;
        }

        services.forEach(service => {
            const card = document.createElement("div");
            card.className = "service-card";

            const name = document.createElement("h3");
            name.textContent = service.name;

            const description = document.createElement("p");
            description.textContent = service.description;

            const price = document.createElement("p");
            price.textContent = `Price: ₹${service.price}`;

            const category = document.createElement("p");
            category.textContent = `Category: ${service.category}`;
const bookButton = document.createElement("button");
bookButton.textContent = "Book Service";

bookButton.addEventListener("click", () => {
    window.location.href =
        `booking.html?serviceId=${encodeURIComponent(service.id)}`;
});

card.append(name, description, price, category, bookButton);
servicesContainer.appendChild(card);

        });

    } catch (error) {
        console.error("Error loading services:", error);
        servicesContainer.textContent =
            "Unable to load services. Please check the backend.";
    }
}

loadServices();
