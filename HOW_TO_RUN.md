# How to Run the Blood Pressure Tracker Application

This guide provides detailed instructions on how to run the Blood Pressure Tracker application.

## Prerequisites

Before running the application, ensure you have the following installed:

1. **Java 21 or higher** - The application is built using Java 21.
   - To check your Java version, run: `java -version`
   - If you need to install or update Java, download it from [Oracle's website](https://www.oracle.com/java/technologies/downloads/) or use a package manager like SDKMAN or Homebrew.

2. **Maven** - The application uses Maven for dependency management and building.
   - To check if Maven is installed, run: `mvn -version`
   - If you need to install Maven, follow the instructions on the [Maven website](https://maven.apache.org/install.html) or use a package manager.

3. **OpenAI API Key** (optional) - Required only if you want to use the AI-powered risk analysis feature.
   - You can get an API key from [OpenAI's website](https://platform.openai.com/).

## Method 1: Running with Maven

This is the recommended method for development.

### Step 1: Clone the repository (if you haven't already)

```bash
git clone https://github.com/yourusername/bptracker.git
cd bptracker
```

### Step 2: Configure OpenAI API Key (optional)

If you want to use the AI-powered risk analysis feature, set your OpenAI API key as an environment variable:

**On Linux/macOS:**
```bash
export OPENAI_API_KEY=your_openai_api_key
```

**On Windows (Command Prompt):**
```cmd
set OPENAI_API_KEY=your_openai_api_key
```

**On Windows (PowerShell):**
```powershell
$env:OPENAI_API_KEY="your_openai_api_key"
```

### Step 3: Build the application

```bash
mvn clean install
```

This command will:
- Download all the required dependencies
- Compile the source code
- Run the tests
- Package the application into a JAR file

### Step 4: Run the application

```bash
mvn spring-boot:run
```

The application will start and be accessible at `http://localhost:8080`.

## Method 2: Running with Java (using the pre-built JAR)

If you already have a built JAR file, you can run it directly with Java.

### Step 1: Configure OpenAI API Key (optional)

Set your OpenAI API key as an environment variable as described in Method 1, Step 2.

### Step 2: Run the JAR file

```bash
java -jar target/bptracker-0.0.1-SNAPSHOT.jar
```

The application will start and be accessible at `http://localhost:8080`.

## Accessing the Application

Once the application is running, you can access it through your web browser:

1. **Web Interface**: Open your browser and go to `http://localhost:8080`
   - This will take you to the main dashboard of the Blood Pressure Tracker application.

2. **API Documentation**: The Swagger UI is available at `http://localhost:8080/swagger-ui.html`
   - This provides interactive documentation for all the API endpoints.

3. **H2 Database Console**: For development purposes, you can access the H2 database console at `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:bpdb`
   - Username: (leave empty)
   - Password: (leave empty)

## Troubleshooting

### Common Issues

1. **Port already in use**:
   - If port 8080 is already in use, you can specify a different port:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
   ```
   or
   ```bash
   java -jar target/bptracker-0.0.1-SNAPSHOT.jar --server.port=8081
   ```

2. **OpenAI API Key issues**:
   - If you're getting errors related to the OpenAI API, make sure your API key is correctly set as an environment variable.
   - The AI features will not work without a valid API key, but the rest of the application should function normally.

3. **Java version issues**:
   - If you get errors about Java version compatibility, make sure you're using Java 21 or higher.
   - You can specify which Java version to use with Maven by setting the JAVA_HOME environment variable.

## Production Deployment

For production deployment, it's recommended to use PostgreSQL instead of the in-memory H2 database. To do this:

1. Uncomment and update the PostgreSQL configuration in `src/main/resources/application.properties`:
```properties
spring.profiles.active=prod
spring.datasource.url=jdbc:postgresql://localhost:5432/bp
spring.datasource.username=bpuser
spring.datasource.password=secret
```

2. Make sure PostgreSQL is installed and running, and that the specified database, username, and password are correctly configured.

3. Run the application as described above.