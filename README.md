# API Service (Java)

This service provides the backend API for customer data management. It serves as the middle layer in the three-tier architecture, connecting the frontend console service with the data service.

## Features

- RESTful API endpoints for customer data operations
- Input validation and error handling
- Communication with the data service layer
- Health check endpoint with dependency status

## API Endpoints

- `GET /api/health` - Health check endpoint with dependency status
- `POST /api/customers` - Create a new customer
- `GET /api/customers` - Get all customers
- `GET /api/customers/search` - Search for customers by ID, name, or alias
- `PUT /api/customers/:id` - Update a customer
- `DELETE /api/customers/:id` - Delete a customer

## Setup

1. Build the project:
   ```
   mvn clean package
   ```

2. Configure the data service URL (optional):
   ```
   export DATA_SERVICE_URL=http://localhost:3002
   ```

3. Run the service:
   ```
   mvn spring-boot:run
   ```

The service will run on port 3001 by default and connect to the data service at http://localhost:3002.

## Technology Stack

- Java 11
- Spring Boot 2.7.8
- Spring WebFlux for reactive API calls
- Project Lombok for reducing boilerplate code
