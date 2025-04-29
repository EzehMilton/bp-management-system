# Blood Pressure Tracker

A Spring Boot application for tracking and analyzing blood pressure readings.

## Overview

The Blood Pressure Tracker is a comprehensive application designed to help patients and healthcare providers monitor blood pressure readings over time. It provides functionality for:

- Managing patient records
- Recording and tracking blood pressure readings
- Assessing health risks based on blood pressure data
- AI-powered analysis of blood pressure trends

## Technologies Used

- Java 21
- Spring Boot 3.4.4
- Spring Data JPA
- H2 Database (development)
- PostgreSQL (production)
- Spring AI with OpenAI integration
- Swagger UI for API documentation
- Maven for build management

## Prerequisites

- Java 21 or higher
- Maven
- OpenAI API key (for AI-based risk analysis)

## Setup and Running

### Clone the repository

```bash
git clone https://github.com/yourusername/bptracker.git
cd bptracker
```

### Configure OpenAI API Key

Set your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY=your_openai_api_key
```

### Build the application

```bash
mvn clean install
```

### Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080` by default.

### Accessing the H2 Database Console

The H2 in-memory database console is available at `http://localhost:8080/h2-console` with the following settings:
- JDBC URL: `jdbc:h2:mem:bpdb`
- Username: (leave empty)
- Password: (leave empty)

## API Endpoints

### Patient Management

- **Create a new patient**
  - `POST /v1/api/patient`
  - Request body: Patient information

- **Get all patients**
  - `GET /v1/api/patient`

- **Search patients by name**
  - `GET /v1/api/patient/search?name={name}`

- **Get a patient by ID**
  - `GET /v1/api/patient/{id}`

### Blood Pressure Reading Management

- **Create a new reading**
  - `POST /v1/api/reading`
  - Request body: Reading information

- **Update an existing reading**
  - `PUT /v1/api/reading/{id}`
  - Request body: Updated reading information

- **Get a reading by ID**
  - `GET /v1/api/reading/{id}`

- **Get all readings**
  - `GET /v1/api/reading`

- **Delete a reading**
  - `DELETE /v1/api/reading/{id}`

- **Get recent readings for a patient**
  - `GET /v1/api/reading/patient/{patientId}/recent`

- **Get the latest reading for a patient**
  - `GET /v1/api/reading/patient/{patientId}/latest`

### Risk Assessment

- **Capture and assess immediate reading**
  - `POST /v1/api/risk/{patientId}/immediate`

- **Perform AI-based risk analysis**
  - `GET /v1/api/risk/{patientId}/analyzeAI`

## Production Deployment

For production deployment, configure the application to use PostgreSQL by uncommenting and updating the PostgreSQL configuration in `application.properties`:

```properties
spring.profiles.active=prod
spring.datasource.url=jdbc:postgresql://localhost:5432/bp
spring.datasource.username=bpuser
spring.datasource.password=secret
```

## License

n/a

## Contributing

Just share your ideas and suggestions. Pull requests are welcome!
