# SpendWise backend security update

This update adds registration/login using BCrypt + JWT and scopes transactions, budgets and savings goals to the authenticated user.

## Required environment variables (PowerShell)
Set these in the terminal before starting the app; do not commit secrets:
```powershell
$env:DB_PASSWORD="YOUR_NEW_MYSQL_PASSWORD"
$env:JWT_SECRET="BASE64_ENCODED_RANDOM_SECRET_OF_AT_LEAST_32_BYTES"
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```
Generate a strong Base64 secret using a trusted password manager or locally with Java. The default JWT secret in properties is development-only and must be overridden.

## Authentication endpoints
- POST `/api/auth/register` JSON `{ "name":"Anand", "email":"anand@example.com", "password":"A-strong-password-123" }`
- POST `/api/auth/login` JSON `{ "email":"anand@example.com", "password":"A-strong-password-123" }`
- GET `/api/auth/me` with `Authorization: Bearer <token>`

Register/login return `{token, tokenType, userId, name, email}`. Copy token into Postman Authorization > Bearer Token. All transaction, summary, budget and savings endpoints require a token.

## Existing database rows
The update adds nullable `user_id` columns so existing tables can be migrated without dropping data. Old rows with `user_id` NULL will not be shown to new users. Do not assign old financial records to a user unless you know who owns them.

## Main APIs
- Transactions: GET/POST `/api/transactions`, GET/PUT/DELETE `/api/transactions/{id}`
- Summary: GET `/api/transactions/summary?month=2026-10`
- Budgets: GET/POST `/api/budgets`, GET `/api/budgets/month/2026-10`, GET `/api/budgets/month/2026-10/progress`, PUT/DELETE `/api/budgets/{id}`
- Savings: GET/POST `/api/savings-goals`, GET `/api/savings-goals/{id}`, GET `/api/savings-goals/progress`, PUT/DELETE `/api/savings-goals/{id}`

## Notes
- Do not deploy with `ddl-auto=update`; use controlled database migrations for production.
- CORS is not configured yet. If the frontend runs on a different origin, configure CORS for the exact local frontend origin before connecting it.
- This update has not been compiled in this environment because Maven dependency downloads/network access are unavailable here. Run the commands above and fix any reported issue before relying on the code.

## Windows PowerShell example
From the `spendwise_backend` directory:
```powershell
$env:DB_PASSWORD = Read-Host "Enter your NEW MySQL password"
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```
Keep this PowerShell window open while running the application. These environment variables are scoped to that terminal session. Use the new MySQL password after rotating the exposed one in MySQL.

## Postman walkthrough
1. POST `http://localhost:8080/api/auth/register` with JSON body:
```json
{"name":"Demo User","email":"demo@example.com","password":"StrongPass123!"}
```
2. Copy `token` from the response.
3. For protected requests, select Authorization > Bearer Token and paste the token.
4. POST `/api/transactions` with:
```json
{"title":"Salary","amount":25000,"type":"INCOME","category":"Salary","transactionDate":"2026-10-01","description":"Demo income"}
```
5. Try `GET /api/transactions/summary?month=2026-10`, `GET /api/budgets/month/2026-10/progress`, and `GET /api/savings-goals/progress`.
6. Call `GET /api/auth/me` to confirm the logged-in profile. A request to `/api/transactions` without a token should return 401.

## Security reminders
- A token is a credential; never publish it or commit it to GitHub.
- Do not add a public registration flow to a production deployment without rate limiting, email verification, and operational monitoring.
- This is a local-development implementation, not a security audit. Run the build and endpoint tests before relying on it.
