# websocket-chat

A real-time chat system using WebSocket, developed in Java with Spring Boot, MongoDB, and Redis.

## Features

- Real-time communication via WebSocket
- Authentication based on JWKS
- User data persistence with MongoDB
- Session caching using Redis

## Technologies

- Java 17+
- Spring Boot
- MongoDB
- Redis
- Maven

## Configuration

Main settings are in `src/main/resources/application.yaml`. Supported environment variables:

## How to run

1. Make sure MongoDB and Redis are running locally.
2. Install dependencies: `mvn clean install`
3. Run the application: `mvn spring-boot:run`
4. Access the application at `http://localhost:3030`
5. Use a WebSocket client to connect to `ws://localhost:3030/chat` with a valid JWT token.
6. Send and receive messages in real-time.