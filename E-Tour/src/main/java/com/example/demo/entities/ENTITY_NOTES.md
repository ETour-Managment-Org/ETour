# Entity Layer — Strict to the Final ER

**Package:** `com.example.demo.entities`
**Source:** `Final ER-a3795530.pdf` (18 entities) + the BRD patches you specified
**Regenerated:** 29 July 2026

---

## Conformance

| Entity | ER columns | Generated | Result |
|---|---:|---:|---|
| Role | 3 | 3 | ✅ exact |
| User | 11 | 11 | ✅ exact |
| Category | 5 | 5 | ✅ exact |
| SubCategoryMaster | 6 | 6 | ✅ exact |
| Tour | 15 | 15 | ✅ exact |
| Schedule | 6 | 6 | ✅ exact |
| Itinerary | 5 | 5 | ✅ exact |
| Journey | 6 | 6 | ✅ exact |
| TourImages | 6 | 6 | ✅ exact |
| Cost | 10 | 10 | ✅ exact |
| **Booking** | **6** | **6** | ✅ exact |
| PassengerDetails | 8 | 8 | ✅ exact |
| Payment | 6 | 6 | ✅ exact |
| Cancellation | 7 | 7 | ✅ exact |
| Review | 9 | 9 | ✅ exact |
| Ads | 8 | 8 | ✅ exact |
| Language | 3 | 3 | ✅ exact |
| Notification | 6 | 6 | ✅ exact |

**126 columns · 0 missing · 0 extra · 18 entities.**

All 34 of my previous additions are gone. `SiteContent` and `CrawlText` are withdrawn —
those two files now declare no type and can be deleted.

---

## What is included beyond the raw column list

Only three things, none of which add columns:

1. **`Tour.category` and `Tour.subCategory` are mapped as `@ManyToOne`.** The ER draws
   *classifies* (CATEGORY → TOUR) and *categorizes* (SUB_CATEGORY_MASTER → TOUR) as
   relationships, so they are mapped as relationships. The join columns are
   `category_id` and `subcat_id`.
   *(A literal `VARCHAR Category` cannot form a foreign key to `category_id INT` — MySQL
   will reject it — so the ER's own relationship lines are the authority here.)*

2. **Inverse collections** (`@OneToMany` / `@OneToOne` with `mappedBy`) for every
   relationship the ER draws. These create no database columns.

3. **Lombok safety on `@Data`** — two annotations, no schema effect:
   - `@ToString.Exclude` on every association. Without it, `Booking ↔ PassengerDetails`
     recurses to **StackOverflowError** the first time you log or print an entity.
   - `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` with the PK included. Otherwise
     `equals()` triggers lazy loads, and an entity's hash code changes when it is saved —
     so it gets lost inside any `HashSet` it joined beforehand.

Money uses `BigDecimal` in `Cost`, `Booking`, `Payment` and `Cancellation`, matching the
ER's `DECIMAL`. `Tour.price` stays `Float` as the ER specifies — it is an indicative
"from" price on listing cards; the chargeable rates live in `Cost` as `DECIMAL`.

---

## Verification performed

- ✅ 126/126 columns match, zero drift in either direction
- ✅ All 18 owning-side `@JoinColumn` targets resolve to real entities
- ✅ All inverse sides resolve to a field of the correct type
- ✅ Every association excluded from `toString()`
- ✅ Every entity uses id-only `equals`/`hashCode`
- ✅ No MySQL reserved words as table or column names
- ⚠️ **Not compiled** — no Maven or Maven Central access in this environment. Run
  `mvn -q clean compile`.

---

## Two things to be aware of later

Not changed — recorded so they are a decision rather than a surprise.

### 1. `Booking` does not reference `Schedule`

`Booking` links to `Tour` only. `Schedule` holds the departure dates and
`Available_Seats` / `Total_Seats`.

Consequence: the system can record *that* a tour was booked, but not *which departure* —
so `Available_Seats` cannot be decremented, and BRD-040's "all schedule dates of the
selected tour" has no booking-side counterpart.

This becomes a live problem when the booking service is written. The fix is one column
(`schedule_id`) whenever you want it.

### 2. `PassengerDetails` stores both `birth_date` and `age`

BRD-065 requires age *"as on departure date"*. A stored `age` is correct only on the day
it was written, so treat `birth_date` as the source of truth and compute age at pricing
time from the departure date.

---

## Next steps

1. `mvn clean compile`
2. Delete `SiteContent.java` and `CrawlText.java` (now empty)
3. Seed `role` (CUSTOMER, ADMIN) and `language` (EN)
4. Repositories, then the in-memory tours endpoint and the React details page
