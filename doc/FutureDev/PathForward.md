# Path Forward: From Hackathon to Product 🛤️

*My thoughts on where MetaMuse goes from here — written Nov 2026, grounded in the
actual state of this repo: what works, what's honest, and what's still theater.*

> **How to read the FutureDev folder:** this file is the growth path — *what* to build,
> *why*, and *in what order*. Its companion, [FutureDirections.md](FutureDirections.md),
> is the scope boundary — *why* each phase lives beyond the hackathon, with realistic
> horizons per item. Read that file before committing to any date below.

---

## 0. Brutally honest snapshot (today)

**Real:** 5-state demo, live cross-channel rule engine on genuine telemetry, Muse Spark
integration with honest fallback provenance, BYOK Keystore security, background sync
worker, 33/33 green suite, full docs. The *detection* half of the thesis is true.

**Theater (labeled as such, but still theater):** recovery is self-reported, not
sensor-verified; "AI reasoning" is one prompt call; Guard alignment is prompt-level;
PKCE, change-token sync, and ExecuTorch are roadmap text. Nothing here is dishonest
anymore — but the gap between demo and product lives exactly in these five items.

**Rule for everything below:** close the honesty gaps in the order a skeptical judge
would probe them. Trust compounds like interest; so does the lack of it.

---

## Phase 1 — Harden the truth (weeks 1–4, same team)

*Feasibility notes per item: [FutureDirections.md](FutureDirections.md).*

Make every remaining claim literally true. No new features.

1. **Sensor-verified recovery (rung 3).** When a user logs follow-through in Live mode,
   schedule a verification check on next sync: sleep actually rose / delivery count
   actually fell / a call actually happened (call-log permission, opt-in). Expose the
   three states in UI: Suggested → Logged → Sensor-Verified. This single feature is the
   moat the review named — build it first.
2. **Real Llama Guard call.** Route every companion response through an actual Guard
   classify call; display its verdict instead of model self-report. Highest
   credibility-per-line-of-code item left.
3. **Prove background sync on-device.** Grant background Health Connect access, run the
   overnight test from the final checklist, and log loop-latency numbers. If Doze kills
   reliability, document the exact behavior — then decide whether change-tokens are next.
4. **PKCE for real.** Replace the token/callback shortcut with a proper code-verifier
   flow. Small, standard, removes the last "docs say X, code does Y."
5. **Instrument loop latency.** Detection → nudge → logged → sensor-verified, timed in
   production telemetry. You cannot claim "frictionless" without this number.

**Exit gate:** an external reviewer re-running the last audit finds zero corrections.

## Phase 2 — Prove retention (months 2–3, +1 designer)

*Why this can't compress: [FutureDirections.md](FutureDirections.md) (30 days is 30 days).*

A Tamagotchi that nobody opens on day 30 is a demo, not a product. Answer one question:
*does Lumi change Tuesday?*

1. **30-user, 30-day closed beta.** Real phones, real data, weekly EMA check-ins
   ("did Lumi notice something true this week?"). Pre-register the metrics before
   starting: D7/D30 open rate, nudges accepted, logged recoveries, and one honest
   leading indicator — unprompted "she was right" messages.
2. **Longitudinal demo mode.** A "30 days later" simulator compressing compounding wins,
   so prospects *feel* the 40-year thesis in 30 seconds. This doubles as the investor
   demo.
3. **Anti-annoyance contract in code.** Hard daily nudge budget, quiet hours, intensity
   slider, one-tap "leave me alone this week." Retention insurance must be enforceable,
   not promised.
4. **Kill or keep the Charm question.** Prototype Lumi as a lock-screen widget +
   persistent notification presence. If that satisfies "always there," the hardware
   dependency weakens and the roadmap gets cheaper. If not, the Charm case is proven
   by data, not vibes.

**Exit gate:** D30 > 40% and a majority agree "Lumi noticed something I hadn't."
Below that, fix the product before scaling anything.

## Phase 3 — Go real (months 4–9, +backend + ML)

1. **Backend, minimally.** Plaid link-token service first (production security blocker),
   then Muse Secure-VM-style per-user processing, then Sentinel-grade egress policy.
   Nothing else goes server-side until it must.
2. **ExecuTorch local inference.** Replace the rule-template offline engine with
   quantized Llama 3.2 1B/3B on-device. This is when "Offline Engine" earns a Llama
   name again — and the privacy story becomes mathematically complete.
3. **Wearable depth.** Band sEMG integration (tension index live), Wear OS glanceable
   face, Health Connect *write* (contribute band data back to the ecosystem).
4. **Clinical toe-dip.** IRB-approved pilot (n=50): sleep vs PSG subset, HRV vs Holter,
   EMA-validated stress index. Not for claims yet — for learning what breaks.

**Exit gate:** a doctor looks at a Lumi export and says "this is actually useful."

## Phase 4 — Scale (year 2+, company-shaped)

*The possible long-term options — backend, ExecuTorch, wearables, clinical, regulatory —
are itemized with horizons in [FutureDirections.md](FutureDirections.md).*

Household mode, clinician channel with reimbursement path, population-level verified
outcomes research, Confidential-VM zero-knowledge, regulatory submissions where the
data justifies them. Details live in the [product vision](../meta-product-vision.md)
and [band specs](../neural-band-fitness-tracker/) — don't build any of it early.

---

## Anti-roadmap (do NOT build)

- **More dashboards.** Every chart added weakens "Lumi is the product." Vitals exist to
  feed her, not to be screens.
- **Streaks, badges, gamified junk.** Outcome metrics only. The day Lumi rewards tapping
  instead of recovering, the thesis dies.
- **A second companion / skins / marketplace.** Depth of one relationship beats breadth
  of ten mascots.
- **Clinical claims before clinical data.** One premature "detects X" ends trust
  permanently — and invites regulators early.

## Decision gates (write these down now)

- If Phase 1 takes > 6 weeks, the codebase needs a senior Android lead, not more time.
- If Phase 2 D30 < 25% after two full iteration cycles, pivot the interface (widget?
  messaging-native? glasses-first?) before adding features.
- If a platform owner (Meta, Apple, Google) ships connection-debt-grade social sensing
  first, the moat thesis breaks — compete on coaching depth or find a new wedge fast.

## Who this needs

Today: 1 Android + AI time (done). Phase 1: same. Phase 2: +1 product designer
obsessed with restraint. Phase 3: +1 backend/security engineer, +1 ML engineer (edge),
+1 clinical advisor (part-time). Never hire growth before retention is proven.

---

*Final thought: this project has survived three things most hackathon ideas don't —
an honest audit, a hostile review, and its own authors asking "is this true?" Keep
that loop running and the path above is walkable. The moment feeling replaces
measuring, come back and re-read Phase 2.* 🪽
