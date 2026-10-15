# Development Notes

## Overall approach

A Spring Boot 3.5 / Java 21 REST service exposing `POST /applications`. A request is validated, priced,
checked against eligibility rules, and answered with either a single offer or a list of rejection reasons.
Every decision, approved or rejected, is written to an H2 audit table.

Processing pipeline (`LoanApplicationServiceImpl`):

```
request -> classify risk band -> compute interest rate -> compute EMI
        -> evaluate eligibility rules -> build offer (if eligible) -> persist audit record -> response
```

The work was built bottom-up: pure calculation components first (EMI, risk, rate, eligibility), each with
unit tests, then the orchestration, persistence and HTTP layers on top.

## Package layout

| Package | Responsibility |
|---|---|
| `controller` | HTTP only; delegates to the service. |
| `dto.request` / `dto.response` | API contract, with Bean Validation on the request records. |
| `service` | Business logic, one small interface and implementation per concern. |
| `rules` | One `EligibilityRule` per eligibility check. |
| `domain.model` / `domain.enums` | Framework-free records and enums used by the services. |
| `entity` / `repository` | JPA audit record and Spring Data repository. |
| `mapper` | DTO, domain, entity and response translation. |
| `exception` | `GlobalExceptionHandler` mapping failures to HTTP 400 `ErrorResponse`. |
| `config` | `LendingProperties` (base interest rate, bound from `application.yml`). |

## Key design decisions

- **Domain separate from DTOs.** Services work on `Applicant` and `Loan` records, not request objects, so
  business logic does not depend on the HTTP contract.
- **Eligibility as a list of rules.** `EligibilityServiceImpl` receives every `EligibilityRule` bean and runs all of
  them, so a rejection reports all failing reasons (as the sample response shows) and a new rule is a new class
  with no change to existing code.
- **`BigDecimal` throughout.** EMI uses 20 significant digits for intermediate values (`(1+r)^n` for 360 months
  loses cents at lower precision) and rounds once at the end to scale 2, `HALF_UP`. Rates are returned at scale 2.
- **Age and tenure compared in months** (`age*12 + tenureMonths > 65*12`) so a fractional tenure such as 18 months
  is not truncated to 1 year.
- **Validation on the request records** with explicit messages. `GlobalExceptionHandler` also handles malformed
  JSON and unknown enum values (for example `FREELANCER`) with a message listing the allowed values.
- **Audit record is write-once.** `LoanApplicationEntity` has no setters. It stores the applicant and loan inputs,
  the outcome, the pricing and the rejection reasons, so a decision can be reconstructed later.
- **Base rate in configuration** (`lending.base-rate-percent`) rather than a constant.

## Assumptions

1. **Two EMI thresholds.** The spec has a 60% eligibility rule and a 50% offer rule. The 60% rule is therefore
   redundant for approvals. I kept both: EMI above 60% of income returns `EMI_EXCEEDS_60_PERCENT`; EMI above 50% but
   not above 60% returns `EMI_EXCEEDS_50_PERCENT_OFFER_LIMIT`. Exactly 50% is allowed ("EMI <= 50%").
2. **Risk band below 600.** There is no band for scores under 600, so the response has `riskBand: null` as in the
   spec. The EMI is still computed, using HIGH-band pricing, so affordability failures are reported alongside
   `CREDIT_SCORE_TOO_LOW`. The audit record stores the band only when one exists.
3. **Rejected responses always have `riskBand: null`**, including for applicants who do have a band (for example
   age or EMI rejections), following the spec's rejected example.
4. **Loan size premium** applies when amount is strictly greater than 10,00,000.
5. **HTTP status.** Both approved and rejected decisions return `201 Created`, since both create a stored
   application. Validation failures return `400`.
6. **Sample figures in the spec are illustrative.** 5,00,000 at 13.5% over 36 months gives an EMI of 16,967.64 under
   the stated formula, not the 16,234.23 shown. I followed the formula, not the sample.
7. **Missing numeric fields** (`age`, `creditScore`, `tenureMonths`) are primitives and default to 0, so they fail
   their range check with a range message rather than a "required" message.
8. **Total payable** is `EMI x tenure`, so it includes rounding of the EMI.

## Trade-offs considered

- **Interfaces for every service.** They make unit tests and substitution simple but add files. I kept them for
  consistency with the requested layered structure.
- **Rules as separate beans vs. one eligibility method.** More classes, but each rule is tested in isolation and
  the service stays closed to modification.
- **H2 in-memory.** Zero setup for a coding exercise, but data is lost on restart.
- **Hand-written mapper and entity accessors** instead of MapStruct or Lombok: more boilerplate, but no extra
  dependencies or annotation processing.
- **Computing EMI at HIGH pricing for sub-600 scores** is a conservative simplification rather than a pricing
  decision, since those applications are always rejected.

## Improvements with more time

- Replace H2 with PostgreSQL, add Flyway migrations, and add a `GET /applications/{id}` endpoint to read back
  stored decisions.
- Add idempotency (an idempotency key header) so retries do not create duplicate audit records.
- Make all thresholds (score cutoffs, premiums, EMI limits, age limit) configurable properties.
- Use `@NotNull` wrapper types for numeric request fields so missing fields get a "required" message.
- Add structured logging with a correlation ID, metrics for approval and rejection rates, and OpenAPI docs.
- Add property-based tests for EMI (for example, total payable is at least the principal) and a contract test
  for the JSON shape.
- Store money with `BigDecimal` in a `Money` value type to make the scale and rounding rules explicit.
