# Newsletter and Email Campaign Manager API

A backend REST API to manage mailing lists, subscribers and email campaigns.
It is built with Java and Spring Boot. Campaigns can be scheduled for a future
time, and at that time the app sends them to every subscriber on the linked
list. Sending is simulated with log messages instead of a real email server.

## Deployment URL

Live API base URL: **TBD (added after deployment to Render)**

- Swagger UI: `<deployment-url>/swagger-ui.html`

## Features

- User registration and login using JWT authentication
- Only logged in users can use the API, and each user sees only their own data
- Create mailing lists and add or remove subscribers (name and email)
- Email addresses are validated and cannot be repeated inside the same list
- Create and edit campaigns and link each one to a mailing list
- Save a campaign as DRAFT or SCHEDULED
- Schedule a campaign for a future date and time (past times are rejected)
- A background job sends due campaigns, logs one email per subscriber and marks
  the campaign as SENT
- View all campaigns with pagination and filter them by status

## Tech Stack

- Java 21
- Spring Boot 3.5.16
- Spring Security with JWT
- Spring Data JPA and Hibernate
- PostgreSQL (in production) and MySQL (local development) - JPA works with both
- Maven
- Swagger for API docs
- Postman for testing

## Access

Every endpoint except register and login needs a JWT token. A user can only see
and change the mailing lists and campaigns that they created. Trying to open
someone else's list or campaign returns 403.

## Database

The app uses a relational database - PostgreSQL in production (on Render) and MySQL
for local development. The tables are created automatically by Hibernate when the app
starts, so no SQL script is needed. Main tables:

- users, user_roles
- mailing_lists
- subscribers
- campaigns
- email_send_logs

## How to Run

The app reads its database connection and secrets from environment variables (see
`.env.example`). You need a running database (PostgreSQL or MySQL) and these variables:

```
SPRING_DATASOURCE_URL       jdbc:postgresql://HOST:5432/DBNAME   (or a MySQL URL)
SPRING_DATASOURCE_USERNAME  your database user
SPRING_DATASOURCE_PASSWORD  your database password
JWT_SECRET                  a long random string
```

Run from the newsletter folder:

```
cd newsletter
./mvnw spring-boot:run
```

The app starts on http://localhost:8080 (Swagger UI at /swagger-ui.html).

## Deployment

Deployed as a Docker web service on Render, backed by a free PostgreSQL database.

1. Push this repo to GitHub (public).
2. On Render: New -> Web Service -> connect this repo. Set the Root Directory to
   `newsletter` (the Maven project lives there); Render uses the Dockerfile.
3. Create a free PostgreSQL database on Render.
4. Set environment variables on the web service: `SPRING_DATASOURCE_URL` (the JDBC
   form, `jdbc:postgresql://...`), `SPRING_DATASOURCE_USERNAME`,
   `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`. Render provides `PORT` automatically.
5. Deploy. The live URL goes at the top of this README.

Note: on Render's free tier the service sleeps after about 15 minutes of inactivity.
The scheduler that sends due campaigns only runs while the app is awake, so a campaign
scheduled to send during an idle period is sent the next time the app wakes (any
request wakes it).

## API Documentation

- Swagger UI: http://localhost:8080/swagger-ui.html
  Click the Authorize button and paste your token to try the secured endpoints.
- Postman: import the file Newsletter-and-Email-Campaign-Manager-API.postman_collection.json
  from the project root.

## API Endpoints

### Auth

- POST /api/auth/register - create a new user
- POST /api/auth/login - login and get a JWT token

### Mailing Lists

- POST /api/mailing-lists - create a mailing list
- GET /api/mailing-lists - list my mailing lists
- GET /api/mailing-lists/{id} - get one list with its subscribers
- PUT /api/mailing-lists/{id} - rename a list
- DELETE /api/mailing-lists/{id} - delete a list (blocked if a campaign uses it)
- POST /api/mailing-lists/{id}/subscribers - add a subscriber
- DELETE /api/mailing-lists/{id}/subscribers/{subscriberId} - remove a subscriber

### Campaigns

- POST /api/campaigns - create a campaign (DRAFT by default, or SCHEDULED)
- GET /api/campaigns - list my campaigns (supports status, page, size, sort)
- GET /api/campaigns/{id} - get one campaign
- PUT /api/campaigns/{id} - edit a campaign (not allowed once it is SENT)
- POST /api/campaigns/{id}/schedule - schedule a campaign for a future time
- DELETE /api/campaigns/{id} - delete a campaign
- GET /api/campaigns/{id}/logs - see the send log for each subscriber

## Example Requests

Register a user:

```
POST /api/auth/register
{
  "username": "user1",
  "password": "secret123"
}
```

Login:

```
POST /api/auth/login
{
  "username": "user1",
  "password": "secret123"
}
```

The login response contains a token. Send it on the other requests as a header:

```
Authorization: Bearer YOUR_TOKEN
```

Create a mailing list:

```
POST /api/mailing-lists
{
  "name": "Weekly Newsletter"
}
```

Add a subscriber:

```
POST /api/mailing-lists/1/subscribers
{
  "name": "Alice",
  "email": "alice@example.com"
}
```

Create and schedule a campaign:

```
POST /api/campaigns
{
  "name": "Spring Sale",
  "subject": "Our biggest sale is here",
  "content": "Hello, do not miss our spring sale.",
  "mailingListId": 1,
  "status": "SCHEDULED",
  "scheduledTime": "2026-12-31T10:00:00"
}
```

## Campaign Status

- DRAFT - created but not scheduled
- SCHEDULED - has a future send time
- SENT - the scheduled time has passed and the emails were sent

When a scheduled time is reached, a background job runs (about once a minute),
writes one send log row per subscriber and moves the campaign to SENT. The email
sending itself is simulated with a log line, so no real email is sent.
