# Car Platform – Microservices Architecture

This project is a Spring Boot + Maven based microservices system
for managing car catalog, inventory, orders, and users.

## Architecture

Services:
- API Gateway – Entry point and routing
- Car Catalog Service – Car master data
- Inventory Service – Stock and availability
- Order Service – Order lifecycle
- User Service – User identity and roles

All services are independent Spring Boot applications.

## Technology Stack

- Java 21
- Spring Boot 3.3.0
- Maven
- REST APIs

## Services (To Be Implemented)

- api-gateway : 8080
- car-catalog-service : 8081
- inventory-service : 8082
- order-service : 8083
- user-service : 8084

## How to Run

Each service will be started independently using:
- Maven
- Embedded Tomcat

- For now we are using powershell(will see for ubuntu in future) 
- Developement mode ./mwvn spring-boot:run to run individual services 
in their respective ports
- For clean installs after pom changes I use mvn clean install in respective service folders

- Later on ./mvnw clean package
java -jar target/your-service.jar
for (Packaged Mode)





