# e-Tour Frontend

React 18 + Vite. Talks to the Spring Boot backend on `http://localhost:8080`.

## Run

```bash
npm install
npm run dev
```

Opens on `http://localhost:5173`.

The backend must be running first. Vite proxies `/api` to port 8080, so there are no CORS
issues in development — and `VITE_API_BASE_URL` in `.env` stays as `/api`.

For a deployed build, set `VITE_API_BASE_URL` to the full backend URL.

## The flow this implements

```
Guest browses          /  ->  /categories/:id  ->  /tours?category=  ->  /tours/:id
Clicks book            ->  /login  (tour selection is saved)
Registers or logs in   ->  returns to /booking/details automatically
Traveller details      /booking/details        live price as ages are entered
Review booking         /booking/review         invoice breakdown
Payment                /booking/payment        mocked gateway
Confirmation           /booking/confirmation/:id   popup, receipt, PDF button
Dashboard              /dashboard              bookings, cancel, review
```

## How the login redirect works

Clicking **Book now** stores the tour and departure in `BookingContext`, which persists to
`sessionStorage`. A guest is sent to `/login` with `state.from = '/booking/details'`.

After signing in the app returns to that exact page with the selection intact — nothing is
re-entered. The draft is cleared once the booking is confirmed.

## Price calculation

`src/utils/pricing.js` mirrors the backend's `FareBandPolicy` so the traveller-details page
can show a live estimate before submitting:

| Condition | Rate |
|---|---|
| Age over 12, travelling with others | `adultPrice` |
| Age over 12, travelling alone | `singlePersonPrice` |
| Age 12 or under, bed requested | `childWithBedPrice` |
| Age 12 or under, no bed | `childWithoutBedPrice` |

Age is computed at the **departure date**, matching BRD-065.

> The backend recalculates everything on `POST /api/bookings/place`. The frontend figure is
> a preview only — the server total is authoritative and is what appears on the receipt.

## PDF receipt

The confirmation page has a print stylesheet. **Download PDF receipt** calls `window.print()`,
and the browser's print dialog offers *Save as PDF*. Navigation, buttons and the stepper are
hidden in print.

The confirmation email is simulated by the backend — the response message is shown on screen.

## Structure

```
src/
├── api/          one module per backend controller
├── context/      AuthContext (JWT + user), BookingContext (draft)
├── components/   Layout, Header, cards, Stepper, Modal, ProtectedRoute
├── pages/        browse, auth, booking/ (4 steps), dashboard
├── routes/       AppRoutes
└── utils/        format.js, pricing.js
```

## Notes

- JWT is kept in `localStorage`; the API client attaches it to authenticated calls.
- A 401 clears the token automatically.
- `ProtectedRoute` bounces guests to `/login` and remembers where they were going.
- Category navigation follows the server's `nextAction` field, so the tree can grow to any
  depth without a frontend change.
