#  SlothMQ (Backend)

**SlothMQ** is an experimental, lightweight message queue and monitoring system built in Java SE + Netty. Designed to challenge the stereotype that Java is slow, **SlothMQ** proves that you can build fast and modern systems in pure Java — the "Sloth" is just a joke. 🐌💨

This is the backend component of the project. It handles message queues, logs, user authentication with JWT, and provides a WebSocket-enabled architecture for real-time updates.

> This project is **Alpha** and under active development. It is not production-ready. Use it at your own risk.

---

##  Features

-  JWT-based authentication
-  Role-based access control (RBAC)
-  Real-time log streaming via WebSocket
-  RESTful endpoints for queue publishing and consumption
-  Metrics endpoints (CPU, Memory, etc.)
-  MongoDB-based persistence
- ⚙ Lightweight Java SE + Netty server (no Spring)
-  CORS configurable
-  Easy-to-extend connector interface

---

## 📦 Getting Started

### Requirements

- Java 17+
- MongoDB (running locally or on URI)
- Gradle

### Setup

```bash
git clone https://github.com/rafaelingaramo/slothmq.git
cd slothmq
./gradlew build
java -jar build/libs/slothmq.jar
```

> The server will start on `http://localhost:8080`.

---

## 📚 API Overview

### Authentication

- `POST /api/login`  
  Receives JSON: `{ "userName": "...", "passkey": "..." }`  
  Returns: `{ "token": "...", "exp": 1234567890 }`

### Queue Endpoints

- `POST /api/queue/{topic}` – Publish message
- `GET /api/queue/{topic}` – Consume message

### User Management

- `GET /api/users`
- `POST /api/users`
- `PUT /api/users`
- `DELETE /api/users/{id}`

### Logs & Metrics

- `GET /api/logs/stream` (WebSocket)
- `GET /api/metrics`

---

##  TODO / Roadmap

-  Basic WebSocket streaming
-  JWT authentication
-  Unit and integration testing
-  Java Connector library
-  NodeJS and Go connectors
-  Frontend improvements (React ESLint setup, test coverage)
-  CI/CD pipeline
-  Docker image packaging

---

## 🛡 License

This project is licensed under the MIT License.  
See [LICENSE](./LICENSE) for details.

> ⚠️ SlothMQ is in ALPHA stage and not tested for production use.  
> The author assumes **no responsibility for any damage or data loss**.  
> Contributions are welcome!

---

##  Related Projects

-  Frontend SPA (React): [https://github.com/rafaelingaramo/slothui](https://github.com/rafaelingaramo/slothui)
-  Backend Source Code: [https://github.com/rafaelingaramo/slothmq](https://github.com/rafaelingaramo/slothmq)

---

##  Author

Made with ❤️ by [Rafael Ingaramo](https://www.linkedin.com/in/rafaelingaramo/)

> Challenging Java's reputation, one Sloth at a time.
