# e-Tour — four new features, and how to run them

Covers form validation, Lottie animations, Razorpay test payments and Excel
tour import. Everything lands in the existing three folders — `E-Tour` (Java),
`ETour-DotNet` (.NET), `E-tour-FrontEnd` (React). No new top-level directories.

**Read this first:** none of this code has been compiled or run. There is no
Maven, no dotnet SDK and no npm registry in the environment it was written in.
Every check below is static — parse checks, brace balance, route-parity diffs,
symbol resolution. Budget an hour to build and fix small things before the 12th.

---

## 0. Install the two new frontend packages

```bash
cd E-tour-FrontEnd
npm install
```

`package.json` now asks for:

| package | why | size |
|---|---|---|
| `lottie-react` | plays the animation JSON | ~250 KB, lazy-loaded |
| `xlsx` | reads the spreadsheet in the browser | ~400 KB, lazy-loaded |

Both are behind dynamic `import()`, so neither is in the main bundle. Someone
browsing tours downloads neither.

---

## 1. Form validation

Nothing to install or configure. Three new files do the work:

```
src/validation/rules.js       composable validators
src/validation/schemas.js     one schema per form
src/hooks/useForm.js          values, errors, and WHEN an error may show
src/components/FieldError.jsx the message, with role="alert"
```

**The design decision worth defending in a viva:** errors do not appear while
you type. `useForm` tracks a `touched` map and a `submitted` flag, and
`f.error(name)` returns `null` until the field has been blurred or the form
submitted. Validating on every keystroke means "Email is invalid" flashes at
someone the moment they type `a` — which is technically correct and genuinely
annoying.

Converting another form is three steps:

```jsx
const f = useForm({ email: '', phone: '' }, myScheme)
const submit = f.onSubmit(async (values) => { /* only runs if valid */ })

<form noValidate onSubmit={submit}>
  <input className="input" {...f.field('email')} />
  <FieldError message={f.error('email')} />
</form>
```

`noValidate` is not optional. Without it the browser's own validation bubbles
fire first, and you get two competing error systems — one of which you cannot
style or translate.

**Done:** Register, Login, Search.
**Still on the old pattern:** `FeedbackPage`, `BookingDetailsPage`,
`AdminTourFormPage`. They work as before; they just do not use the shared layer
yet. If time is short before the 12th, leave them.

---

## 2. Lottie animations

Four animations live in `src/animations/`, hand-authored as Lottie v5.7.4 JSON
in the project palette:

| file | size | used by |
|---|---|---|
| `loading.json` | 1.6 KB | `<Loader />` — every page that waits |
| `success.json` | 1.6 KB | booking confirmation |
| `empty.json` | 2.2 KB | search with no results, import with no rows |
| `error.json` | 1.9 KB | `<ErrorState />` |

16 KB total. For comparison, a typical illustrated LottieFiles download is
300–500 KB — larger than the image it replaced.

### Swapping in a real LottieFiles animation

1. lottiefiles.com → pick a free one → **Download → Lottie JSON**
2. Save it into `src/animations/`
3. Change one line in `src/components/animations.js`

That registry file exists precisely so the swap is one line in one place rather
than a find-and-replace across pages.

### Loading them efficiently — the three things that matter

**Lazy-load the player, not just the JSON.** `LottieBox` does
`lazy(() => import('lottie-react'))`. The animation JSON is a couple of KB; the
*player* is 250 KB. Importing it at the top of `Loader.jsx` would put it in the
main bundle and slow down the home page for an animation the home page rarely
shows.

**Reserve the space.** The `<Suspense>` fallback is an empty box of the same
width and height, so nothing on the page jumps while the chunk downloads.

**Respect `prefers-reduced-motion`.** People who have asked their OS to stop
animations get a static first frame. Two lines, and it is the accessibility
point an examiner is most likely to ask about.

```jsx
import LottieBox from '../components/LottieBox.jsx'
import { ANIM } from '../components/animations.js'

<LottieBox animation={ANIM.success} size={120} loop={false} />
```

`Loader.jsx` also exports `EmptyState`, `ErrorState`, `SuccessState` and
`ButtonSpinner` — pre-wrapped animation + heading + message for the common
cases.

### Where the loading animation actually shows up

`<Loader />` is used on 17 pages and every one of them now plays the Lottie
instead of the CSS spinner — no change needed at the call sites. Ten of the
longer waits also pass a caption, because "Searching…" tells you more than a
spinning shape:

| page | caption |
|---|---|
| Tour list | Finding tours for you… |
| Tour detail | Loading this tour… |
| Search | Searching… |
| Dashboard | Loading your bookings… |
| Confirmation | Fetching your receipt… |
| OAuth callback | Signing you in with Google… |
| Categories | Loading categories… |
| Admin tours / bookings / users | Loading the catalogue / bookings / customers… |

`ButtonSpinner` is an 18px inline version for buttons mid-request — **Sign in**,
**Register**, **Pay**, **Import**. A disabled button whose label changed is easy
to miss; people click again or assume nothing happened. The box is a fixed 18px
so the button does not resize when it appears.

---

## 3. Razorpay test payments

### What replaced what

The mock gateway is **still there and still the default**. `etour.payment.provider`
picks between them, and nothing else in either backend knows the difference —
everything depends on `PaymentGatewayService` / `IPaymentGatewayService`.

That switch is deliberate. Replacing the mock outright means your booking demo
on the 12th needs working internet and live keys. One environment variable
gets you back to a gateway that always succeeds and never leaves the machine.

### Getting keys (5 minutes, free, no documents)

1. Sign up at **dashboard.razorpay.com**
2. Toggle to **Test Mode** (top-right — check this, it is easy to miss)
3. **Settings → API Keys → Generate Test Key**
4. You get a **Key Id** (`rzp_test_...`) and a **Key Secret**, shown once

The **Key Id is public** — it goes to the browser, that is how the modal knows
which account to pay. The **Key Secret never leaves the server**; it is the HMAC
key that makes signature verification mean anything. If it reaches the browser,
anyone can forge a payment.

### Where the keys go

**One place: `Final_Project_Workspace/.env`.** It is gitignored. Nothing else
needs editing.

```bash
PAYMENT_PROVIDER=razorpay
RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxxxxxxxxxx
PAYMENT_CURRENCY=INR
```

`docker-compose.yml` forwards those same four values to **both** backends, under
each framework's own key names:

| `.env` | Java service | .NET service |
|---|---|---|
| `PAYMENT_PROVIDER` | `PAYMENT_PROVIDER` | `ETour__Payment__Provider` |
| `RAZORPAY_KEY_ID` | `RAZORPAY_KEY_ID` | `ETour__Payment__Razorpay__KeyId` |
| `RAZORPAY_KEY_SECRET` | `RAZORPAY_KEY_SECRET` | `ETour__Payment__Razorpay__KeySecret` |
| `PAYMENT_CURRENCY` | `PAYMENT_CURRENCY` | `ETour__Payment__Currency` |

The .NET names use a **double underscore where the config path has a colon** —
`ETour__Payment__Razorpay__KeyId` is read as `ETour:Payment:Razorpay:KeyId`.
Getting that wrong is silent: a misspelled key is not an error to the
configuration system, it is an *absent* key, and absent keys fall back to
defaults. That is exactly how OAuth and email broke in Docker earlier.

Every entry has a `:-` default (`${PAYMENT_PROVIDER:-mock}`), so a missing
`.env` line falls back to the mock gateway rather than starting a broken
checkout. To go back to the mock deliberately: `PAYMENT_PROVIDER=mock`, restart.

Then:

```bash
docker compose config | grep -i razorpay   # check they resolved
docker compose up -d --build
```

Running outside Docker: Java reads the same env vars; .NET wants
`ETour__Payment__Provider=razorpay` exported, or edit `appsettings.json`.

`.env.example` has all of this as commented placeholders for the rest of the
team — **placeholders only**, never a real secret.

Running outside Docker: the Java app reads the same env vars; for .NET either
export `ETour__Payment__Provider=razorpay` or edit `appsettings.json`.

### Test cards

| what | value |
|---|---|
| Card that succeeds | `4111 1111 1111 1111` |
| Card that fails | `5104 0600 0000 0008` |
| Expiry / CVV | any future date, any 3 digits |
| OTP | `1111` |
| UPI success | `success@razorpay` |
| UPI failure | `failure@razorpay` |

No real money moves in test mode.

### The flow, and why it is in that order

```
browser                         our server                    Razorpay
   |                                |                             |
   |-- POST /api/payments/order --->|                             |
   |                                |-- POST /v1/orders --------->|
   |<-- { orderId, keyId, ... } ----|<-- order_XXX ---------------|
   |                                                              |
   |-- opens checkout modal, user enters card ------------------->|
   |<-- razorpay_order_id, razorpay_payment_id, razorpay_signature |
   |                                |                             |
   |-- POST /api/bookings/place --->|                             |
   |    (+ the three values)        |-- HMAC check (local) ------  |
   |                                |-- GET /v1/payments/{id} --->|
   |<-- booking confirmed ----------|<-- captured, 500000 paise ---|
```

**The booking is created only after payment succeeds.** The other order —
booking first, then pay — leaves an unpaid booking holding seats every time
someone closes the modal.

**The three values from the browser are a claim, not proof.** Anyone can POST a
made-up payment id. Razorpay signs `orderId|paymentId` with your key secret, so
the server recomputes the HMAC and compares:

```java
String expected = hmacSha256(orderId + "|" + paymentId, keySecret);
if (!constantTimeEquals(expected, signature)) reject();
```

A forged pair cannot produce a matching signature without the secret. This is
the single most important line in the feature.

**Then it checks the amount.** The signature only proves the ids are genuine —
not that money moved, and not how much. So the server also calls
`GET /v1/payments/{id}` and compares the paid amount against the total *it*
recomputed from the fare bands. That is what stops someone creating a ₹1 order
and using it to book a ₹5000 tour.

**Constant-time comparison** (`constantTimeEquals` in Java,
`CryptographicOperations.FixedTimeEquals` in .NET) instead of `equals`. String
equality returns early on the first differing byte, which leaks — through
timing — how many leading characters were right. Overkill for a college
project, correct, and one sentence to explain.

### The three exits, which is what most implementations get wrong

`src/utils/razorpay.js` wraps the checkout in a promise with **three** exits:

1. `handler` — paid
2. `modal.ondismiss` — user pressed Escape or clicked outside
3. `rzp.on('payment.failed')` — the bank declined

Wire up only the first and your spinner spins forever when someone closes the
modal. A `settled` flag guards against Razorpay firing both success and dismiss,
which it does on some browsers.

Failure and dismissal **resolve**, they do not reject — a declined card is a
normal outcome of a payment page, not an exception.

### No SDK

Neither backend uses `com.razorpay:razorpay-java` or a .NET Razorpay package.
Two HTTP calls and one HMAC do not justify the transitive dependencies, and the
signature check is exactly the part you want to be able to explain rather than
point at.

---

## 4. Import tours from a spreadsheet

**Admin → Tours → Import from Excel**, or `/admin/tours/import`.

Upload → **preview** → confirm. Nothing is written until you press Import.

A sample file with six good tours and two deliberately broken rows is at
`E-tour-FrontEnd/public/sample-tours.xlsx` — served at `/sample-tours.xlsx`.
The page also generates a blank template on demand.

### Why the browser parses the file

The obvious design is to POST the `.xlsx` and parse it server-side. That would
mean **Apache POI in Java and ClosedXML in .NET** — two libraries reading the
same file with different quirks, kept in step by hand, plus two new
dependencies. Parsing in the browser means:

- one parser
- the admin sees the parsed rows *before* anything is sent, so a wrong column
  heading is caught by eye rather than by a 500 after twelve tours went in
- no new backend dependency in either language

The tradeoff is that a 5000-row sheet would be slow in the browser. Tours are
created in tens.

**Nothing is trusted because of this.** Every row still goes through
`createTour()` / `CreateTourAsync()` — the same method the admin form calls,
with the same bean validation. The browser doing the parsing buys convenience,
not privilege.

### Column matching

Headings are normalised — lowercased with all non-letters stripped — so
`Tour Name`, `tour_name`, `TOURNAME` and `Tour-Name` all match. Real
spreadsheets are typed by people.

Only **Tour Name** is required. Unrecognised columns become a warning in the
preview rather than an error. `₹ 12,000` parses as `12000`.

### Import behaviour

Row by row, **no outer transaction**. One bad date should not roll back the
thirty-nine good rows above it — the response gives a verdict per row with the
*sheet* row number, so a message is findable.

Duplicates are **skipped, not merged**. Re-uploading the same file is the most
likely mistake, and silently doubling the catalogue is worse than saying
"already exists". Which also means: fix the bad rows and re-upload the whole
sheet — the successful ones skip themselves.

### New endpoint (identical in both backends)

```
POST /api/admin/tours/import
  { "tours": [ { "tourName": "...", "days": 4, ... } ] }

200 { "total": 8, "imported": 6, "failed": 2,
      "rows": [ { "rowNumber": 2, "tourName": "...",
                  "success": true, "tourId": 32, "message": "Imported" } ] }
```

---

## What was verified, and how

| check | result |
|---|---|
| 17 new/changed JS+JSX files parse (`@babel/parser`, JSX plugin) | pass |
| 4 Lottie JSON files are valid JSON | pass |
| 13 Java files — brace/paren balance with strings and comments stripped | pass |
| 11 C# files — brace balance | pass |
| Java ↔ .NET payment route parity, normalised for the leading slash | `/api/payments/config`, `/api/payments/order` — identical |
| `/api/admin/tours/import` present in both backends | pass |
| Cross-references resolve (`importTours`, `BulkImportDTO`, `VerificationRequest`, `existsByTourNameIgnoreCase`, `transactionRef`) | pass |
| `lottie-react` and `xlsx` declared in `package.json` | pass |
| No `axios` anywhere in `src/` | pass — fetch only |
| No live or test keys committed anywhere | pass |
| HMAC agreement | both use standard HMAC-SHA256 over `orderId\|paymentId`, lowercase hex — identical by construction |

**Not verified, because it cannot be here:** it does not compile, `npm run build`
has not run, no migration has been applied, and no Razorpay call has been made.

---

## Things to watch

**`transaction_ref` is a new column** on `payment`, nullable. Java's
`ddl-auto=update` adds it on next startup. **Start the Java backend at least
once before running .NET against a fresh database** — .NET has no migrations and
maps a column it expects to exist. This is not new; the whole schema already
works this way.

**Razorpay needs outbound internet from the backend container.** If the venue
blocks it, `PAYMENT_PROVIDER=mock` and you lose nothing but the modal.

**The `xlsx` package is unmaintained on npm** (0.18.5 is the last registry
release; the author moved to a self-hosted CDN). It is fine for a project of
this size and it is what every tutorial uses, but it is worth knowing if
someone asks about dependency health.

**Test the Razorpay path once before the 12th**, end to end, with real test
keys. The mock path and the Razorpay path share a booking flow but exercise
different branches, and the Razorpay branch has never run.
