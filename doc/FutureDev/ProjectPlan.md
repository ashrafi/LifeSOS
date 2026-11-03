# LifeSOS Product Development Plan: Client + Server 📋

*Program plan implementing [PathForward.md](PathForward.md), scoped by
[FutureDirections.md](FutureDirections.md). Read those two first; this file turns
their phases into ordered, ownable, verifiable work. Status: proposed, Nov 2026.*

## Goal

Grow the hackathon prototype into a retention-proven, production-grade
bio-responsive companion — Android client first, minimal backend second — sequencing
strictly by trust (honesty gaps), then retention, then scale. Every phase ends at a
measurable exit gate; nothing advances on vibes.

## Success Criteria

- Phase 1: an external reviewer re-running the last audit finds zero corrections;
  recovery evidence is 3-state; Guard verdicts are real API responses.
- Phase 2: 30-day beta hits D30 > 40% with a majority agreeing "Lumi noticed
  something I hadn't" (pre-registered before the beta starts).
- Phase 3: a doctor calls a Lumi export "actually useful"; Plaid is server-side;
  offline inference runs local weights; wearables feed the Life Engine.
- Always: unit suite green at every milestone; no Future Direction demoed as a
  feature (ladder language enforced in review).

## Approach

- **Trust-first sequencing** (the PathForward rule): close every gap a skeptical judge
  would probe before adding any capability.
- **Client leads, server follows.** Phases 1–2 are client-only (telemetry stays
  on-device; beta data exports locally). The server appears in Phase 3, minimally:
  Plaid service first, per-user processing second, everything else only when forced.
- **Assumptions:** staffing per PathForward (Android now; +design Ph2; +backend, edge
  ML, clinical advisor Ph3); Meta Model API + Guard endpoints obtainable with keys;
  Health Connect background access grantable on test devices; no new Meta hardware
  SDKs before Phase 3 (band/Charm items stay conditional).
- **Anti-roadmap enforced** (no dashboards-as-product, no streaks, no second mascot,
  no clinical claims before clinical data) — any work unit violating it is rejected
  in review, not debated.

## Steps

### Phase 0 — Submit (owner, this week; baseline, not a milestone)
- P0.1 Execute [`../final-checklist.md`](../final-checklist.md) end-to-end; freeze the
  submission commit hash here once pushed: `____________`.

### Phase 1 — Harden the truth (weeks 1–4, Android only)
- **1.1 Sensor-verified recovery (client).** Data model: `RecoveryEvidence`
  (Suggested/Logged/SensorVerified) on each recovery; verification evaluators per rule
  (sleep delta via Health Connect re-read, delivery count via finance re-sync,
  sibling call via *opt-in* call-log permission); 3-state chip in the recovery card;
  ViewModel exposes evidence state. Reuses `LifeRuleEngine` + `SerenityRepository`
  rebuild path. Tests: JVM evaluator tests + Robolectric state-machine test.
- **1.2 Real Llama Guard call (client→API).** Guard classify client inside
  `MuseService`; every companion response carries the API verdict, replacing model
  self-report. Failure policy: fail-open with explicit "Safety screening
  unavailable" label (documented; fail-closed would brick offline mode). Tests: fake
  HTTP layer test for pass/flag/unreachable shapes. *Blocked on: Guard endpoint +
  key source (owner decision).*
- **1.3 Background-sync proof (client+field).** Ship the persisted sync epoch to a
  debug surface; run 5 overnight syncs × 2 OEMs; record Doze behavior + battery
  delta; write the change-tokens yes/no decision doc. No code unless the data
  demands it.
- **1.4 PKCE for real (client).** Code-verifier flow in `FacebookSocialConnector`;
  keep the deep-link callback; Robolectric round-trip test with a fake browser result.
- **1.5 Loop-latency instrumentation (client, local-first).** Timestamp
  detected→nudged→logged→sensor-verified per loop; on-device ring log + export;
  server ingestion explicitly deferred to Phase 3.

### Phase 2 — Prove retention (months 2–3, +1 designer)
- **2.1 Beta harness (client).** Weekly EMA prompt UI ("did Lumi notice something true
  this week?"); privacy-preserving on-device metric aggregates; local export for
  analysis (no server). Pre-register D7/D30, nudges accepted, logged recoveries,
  unprompted "she was right" count *before* recruiting.
- **2.2 Longitudinal demo mode (client, flagged off in prod).** Time-compressed
  snapshot simulator ("30 days later") reusing `LifeRuleEngine` + snapshot builders;
  doubles as the investor demo. Never shippable as a feature — flag-gated + labeled.
- **2.3 Anti-annoyance contract in code (client).** Daily nudge budget store, quiet
  hours, intensity slider, one-tap "quiet week"; single choke point gating every
  nudge emission in the repository. Robolectric enforcement tests.
- **2.4 Presence spike (client).** Lock-screen widget + persistent notification Lumi;
  beta A/B vs app-only for "always there" feeling. Decides the Charm-dependency
  question with data. *Depends on 2.1 cohort.*

### Phase 3 — Go real (months 4–9, +backend, edge ML, clinical)
**Server workstream** (new repo/service; nothing here touches the app until contracted):
- **3.1 Plaid service.** Link-token creation + exchange + webhook receiver; secrets in
  a managed vault, never in the app; versioned API (`POST /v1/plaid/link-token`,
  `POST /v1/plaid/exchange`, webhook intake). Client migrates `BankFinanceConnector`
  to it. Tests: API contract tests + Plaid-sandbox end-to-end. *Unblocks any
  production finance claim.*
- **3.2 Per-user processing isolation.** Job queue + per-user workers; credential
  vaulting (model calls never see secrets); immutable audit log of accesses.
- **3.3 Egress policy engine.** Allowlist per integration, taint tags on health/finance
  fields, deny-by-default; red-team test: a prompt-injection exfil attempt must fail
  closed with an audit entry. *Depends on 3.2.*
- **3.4 Aggregate telemetry ingestion.** On-device aggregates only (Phase 1.5 format),
  bounded retention windows, one-tap cryptographic user purge. No raw biosignals,
  ever — enforced by schema (server rejects unknown fields).
**Client workstream:**
- **3.5 ExecuTorch local inference.** Quantized 1B packaged via ExecuTorch; NPU perf
  budget set by measurement on mid-tier hardware (target TBD, not invented); quality
  rubric A/B vs rules engine; fallback chain preserved. "Offline Engine" earns a
  Llama name only after this ships.
- **3.6 Wearable depth.** Band tension-index ingestion behind a capability flag
  (*conditional on Meta SDK availability*); Wear OS glanceable face; Health Connect
  *write* contributor for band data.
- **3.7 Clinician export.** One-tap PDF: sleep/HRV/adherence timelines + verified
  recoveries only; share-sheet delivery; no diagnosis language anywhere.
- **3.8 Clinical pilot support.** Consent UX, study data export, prespecified
  endpoints (sleep subset vs PSG, HRV vs Holter, EMA stress validity).
  *Depends on 3.7; IRB approval is the long pole.*

### Phase 4 — Scale (year 2+, company-shaped)
Household mode, clinician channel + reimbursement, population outcomes research,
Confidential-VM zero-knowledge, regulatory submissions. Entry criterion: Phase 3 exit
gate met. No task breakdown — planning it honestly today is impossible; see the
[product vision](../meta-product-vision.md) and [band specs](../neural-band-fitness-tracker/).

## Validation Plan

- **Every phase:** `./gradlew testDebugUnitTest --offline` green (grows with each
  unit per the repo's test-collateral convention); `:app:assembleDebug` clean.
- **Phase 1:** external re-audit checklist (zero findings); Guard verdict end-to-end
  on device incl. unreachable label; 5-night sync log + Doze doc; PKCE round-trip on
  device; 10+ timed loops in the latency log.
- **Phase 2:** pre-registered beta metrics computed from local exports; D30 method
  documented before recruiting; demo-mode script rehearsed; nudge-budget
  enforcement proven by test + spot-checked on device.
- **Phase 3:** Plaid sandbox end-to-end through the server; secret-extraction attempt
  fails; egress red-team test green; ExecuTorch rubric + measured perf budget;
  clinician-export hallway test ("actually useful"); pilot endpoints locked pre-study.
- **Highest-risk validation:** sensor-verified recovery end-to-end — it needs real
  multi-day telemetry and cannot be faked in unit tests; start its field clock first.

## Risks / Open Questions

- **Risks:** OEM Doze/aggressive killers breaking sync proof; Guard endpoint access or
  pricing; ExecuTorch NPU perf on mid-tier devices; beta recruitment bias; scope
  creep into dashboards (rejected in review per anti-roadmap).
- **Open questions** (owner decisions; only the first blocks work):
  1. Guard API endpoint + key source? (blocks 1.2 — everything else in Phase 1 proceeds)
  2. Plaid production approval timeline? (blocks production finance claims, not 3.1 build)
  3. Which 2 OEMs for the sync proof? (any two mainstream ones; reversible)
