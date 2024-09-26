# webapp

This repo is made for Network Structure and Cloud Computing course. CSYE6225.

# Spring Boot Application with MySQL

This repository contains a Spring Boot application that connects to a MySQL database. Follow the instructions below to set it up on your local machine.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Clone the Repository](#clone-the-repository)
- [Project Configuration](#project-configuration)
- [Running the Application](#running-the-application)
- [Testing the Application](#testing-the-application)

---

## Prerequisites

Before running the project, ensure that the following software is installed on your local machine:

1. **Java JDK** (version 17 or higher)
   Verify if Java is installed by running:

   ```bash
   java -version
   ```

2. **MySQL** (version 8.0 or higher)
   Verify if MySQL server is installed by running:

   ```bash
   mysql -V
   ```

   If server is installed the ensure the server is up and running. To start the mysql server run:

   ```bash
   brew services start mysql
   ```

3. **Maven**
   The project uses Maven Wrapper, so Maven is not required to be installed globally.
   However, if you'd prefer to install Maven, you can do so using Homebrew:

   ```bash
   brew install maven
   ```

## Clone the Repository

Use the following command to clone the git repo:

```bash
 git clone <repo url>
```

## Project Configuration

Update the MySQL connection settings in the `src/main/resources/application.properties` file to match your database configuration:

```properties
# MySQL Connection Properties
spring.datasource.url=jdbc:mysql://localhost:3306/dbname
spring.datasource.username=username
spring.datasource.password=password

# Hibernate (JPA) Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Running the Application

1. Build the Project: Since the project uses Maven Wrapper (mvnw), you don’t need to install Maven globally. Open command prompt in the root dir of application. To build the project, run:

```bash
./mvnw clean install
```

2. Run the Spring Boot Application: Once the build is complete, you can run the application with:

```bash
./mvnw spring-boot:run
```

## Testing the Application

To run unit and integration tests, use the following command:

```bash
./mvnw test
```

### Author: Aaditya Kasbekar
