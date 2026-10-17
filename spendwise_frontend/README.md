# SpendWise Frontend

Static HTML/CSS/JavaScript frontend for the SpendWise Spring Boot backend.

## Files
- `index.html` - login, dashboard, transactions, budgets, savings goals
- `style.css` - responsive styling
- `app.js` - API calls and UI behaviour

## Run locally
1. Start the Spring Boot backend on `http://localhost:8080`.
2. Open this folder in VS Code.
3. Use the VS Code Live Server extension and open `index.html`, or run a local static server.
   Opening the file directly can cause browser CORS issues.
4. If your backend uses a different address, update `API_BASE` at the top of `app.js`.

## API assumptions
This frontend expects:
- `POST /api/auth/register` with `{name,email,password}`
- `POST /api/auth/login` with `{email,password}`, returning a token field named `token`, `accessToken`, `jwt`, or `access_token`
- `GET /api/transactions`, `POST /api/transactions`, `PUT/DELETE /api/transactions/{id}`
- `GET /api/transactions/summary?month=YYYY-MM`
- `GET/POST /api/budgets`, `GET /api/budgets/month/{month}`, `PUT/DELETE /api/budgets/{id}`
- `GET/POST /api/savings-goals`, `GET/PUT/DELETE /api/savings-goals/{id}`

If your auth response uses a different token property or registration DTO uses different field names, adjust those mappings in `app.js`.
The backend must allow CORS for the frontend origin (commonly `http://127.0.0.1:5500` or `http://localhost:5500`) and the expected endpoints.
