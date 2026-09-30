# CodeCraftHub

CodeCraftHub is a small Spring Boot REST API for tracking courses. Course records are serialized as JSON and managed through create, read, update, and delete endpoints.

## Features

- Create, list, retrieve, update, and delete courses.
- Store course IDs and creation timestamps on the server.
- Validate required fields, ISO dates, and course status values.
- Return consistent JSON error responses for validation, missing courses, malformed requests, and file-storage failures.
- Persist course data locally as JSON; no database server is required.

## Requirements

- Java 17 or later
- Apache Maven 3.6.3 or later

## Installation

Clone or download the project, then change to its root directory (the directory containing `pom.xml`):

```bash
git clone <repository-url>
cd codecrafthub
```

If you already have the project locally, skip the `git clone` command. Check the installed tools with:

```bash
java -version
mvn -version
```

## Running the application

From the project root, compile and run the tests:

```bash
mvn clean test
```

Start the API:

```bash
mvn spring-boot:run
```

The server listens on `http://localhost:8080` by default. Stop it with `Ctrl+C`.

## API

All endpoints are rooted at `/api/courses`. The examples below use `curl`; PowerShell users can use `curl.exe` to invoke the standard curl executable.

### Course representation

Request bodies for creating and updating a course use these fields:

```json
{
  "name": "Spring Boot Fundamentals",
  "description": "Learn how to build REST APIs with Spring Boot.",
  "target_date": "2026-10-15",
  "status": "In Progress"
}
```

All four fields are required. `target_date` must use `YYYY-MM-DD`. `status` must exactly match one of `Not Started`, `In Progress`, or `Completed`. The server generates `id` and `created_at`; clients should not include them in request bodies.

A successful response contains a course like:

```json
{
  "id": 1,
  "name": "Spring Boot Fundamentals",
  "description": "Learn how to build REST APIs with Spring Boot.",
  "target_date": "2026-10-15",
  "status": "In Progress",
  "created_at": "2026-09-30T12:34:56.789Z"
}
```

### Create a course

`POST /api/courses` returns `201 Created` and a `Location` header for the new resource.

```bash
curl -i -X POST http://localhost:8080/api/courses -H "Content-Type: application/json" -d '{"name":"Spring Boot Fundamentals","description":"Learn to build REST APIs.","target_date":"2026-10-15","status":"In Progress"}'
```

### List courses

`GET /api/courses` returns `200 OK` and a JSON array. An empty store returns `[]`.

```bash
curl http://localhost:8080/api/courses
```

### Get one course

`GET /api/courses/{id}` returns `200 OK`, or `404 Not Found` if the ID does not exist.

```bash
curl http://localhost:8080/api/courses/1
```

### Update a course

`PUT /api/courses/{id}` replaces the editable course fields and returns `200 OK`. The existing ID and `created_at` timestamp are preserved. All required request fields must be sent.

```bash
curl -i -X PUT http://localhost:8080/api/courses/1 -H "Content-Type: application/json" -d '{"name":"Spring Boot Fundamentals","description":"Build and test REST APIs.","target_date":"2026-10-20","status":"Completed"}'
```

### Delete a course

`DELETE /api/courses/{id}` returns `204 No Content`, or `404 Not Found` if the ID does not exist.

```bash
curl -i -X DELETE http://localhost:8080/api/courses/1
```

### Errors

Errors use a JSON object with `timestamp`, `status`, `error`, and `message` fields. Validation errors additionally include a `fields` object keyed by field name. Common response statuses are `400 Bad Request` for invalid request data, `404 Not Found` for unknown course IDs, and `500 Internal Server Error` for storage or unexpected server errors.

## Data storage

The current `CourseService` reads and writes `courses.json` in the application's working directory (normally the project root). If the file is missing, the service creates it with an empty array when the first course operation is made. Back up this file to preserve records.

Note: `src/main/resources/application.properties` defines `app.courses-file=data/courses.json`, and the project also contains `data/courses.json`, but the current `CourseService` does not read that property or that data file. Its active storage path is the root-level `courses.json` file. The sample in `data/courses.json` also uses different field/status spellings than the API contract above; the API expects `target_date` and the title-case status labels.

## Troubleshooting

- **`mvn` or `java` is not recognized:** Install Maven and a JDK 17 or later, then reopen the terminal so updated `PATH` settings take effect. Verify with `mvn -version` and `java -version`.
- **Port 8080 is already in use:** Stop the other process using the port, or change `server.port` in `src/main/resources/application.properties`.
- **A request returns `400 Bad Request`:** Check that all four required fields are present, `target_date` is formatted as `YYYY-MM-DD`, and `status` exactly matches `Not Started`, `In Progress`, or `Completed`. Send JSON with `Content-Type: application/json`.
- **A request returns `404 Not Found`:** Confirm the numeric course ID exists. IDs are generated by the server and start at 1 for an empty store.
- **Course records seem missing:** The service uses `courses.json` in its current working directory, not `data/courses.json`. Start the application from the project root to use the expected root-level file.
- **Maven reports unresolved `com.example.codecrafthub.exception` imports:** Exception classes in this project are under `com.codecrafthub.exception`; check that imports use that package before building.
- **A storage error is returned:** Confirm the application can read and write `courses.json` and has permission to create it in the working directory. Also check that the file contains valid JSON matching the course representation.