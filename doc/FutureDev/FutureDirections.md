# Future Directions (Explicitly Post-Hackathon) 🔭

*Companion to [PathForward.md](PathForward.md). This doc exists to draw one bright
line: what the hackathon submission **is**, versus what comes **after**. Nothing
below was buildable in hackathon scope — and pretending otherwise would have made
the demo dishonest.*

---

## The short answer

Yes — PathForward's phases are impossible in a hackathon, by design. A hackathon buys
you days, one device, simulated data, and judges' minutes. The future directions need
weeks of wall-clock time (IRB approvals, 30-day retention cohorts, overnight sync
proof), infrastructure that doesn't exist (a backend, clinical partnerships), and
hardware/SDKs outside this repo (Neural Band sensing APIs, Muse Charm, Guard endpoints).
Building the *prototype that proves the thesis* was the right scope. This doc scopes
everything else.

## Why each direction can't be a hackathon task

| Direction | Why not hackathon-feasible | Realistic horizon |
| :--- | :--- | :--- |
| Sensor-verified recovery | Needs days of real telemetry before/after an action + call-log opt-in UX | 2–4 weeks post-submit |
| Real Llama Guard call | Needs a Guard endpoint, keys, failure-mode design, latency budget | 1–2 weeks |
| Background-sync device proof | Needs overnight runs, Doze behavior, battery stats across OEMs | 1–2 weeks of wall-clock |
| PKCE hardening | Standard but fiddly; needs real OAuth round-trips + review | Days (smallest item here) |
| Loop-latency instrumentation | Needs production telemetry + enough events to matter | 2–3 weeks |
| 30×30 retention beta | 30 days is 30 days; cannot be compressed | 6–8 weeks incl. recruiting |
| Backend (Plaid server, VMs) | Architecture + hosting + security review from zero | 2–3 months |
| ExecuTorch local inference | Model packaging, quantization eval, NPU perf tuning | 2–4 months |
| Band sEMG / Charm integration | Depends on Meta hardware + SDKs outside this repo | When Meta ships + months |
| Clinical validation | IRB approval alone exceeds any hackathon; then n=50–300 studies | 6–18 months |
| Regulatory submissions | QMS, pre-subs, MDR/FDA cycles | Years |

## What the hackathon submission froze as

- A working Android prototype proving the closed loop on 5 states, live telemetry,
  and honest offline fallbacks — 33/33 tests green.
- Documentation that labels every simulation, fallback, and roadmap item as such.
- Vision + spec packages (Neural Band tracker, product vision) clearly marked
  *proposal*, not product.

## Guardrails going forward

1. Never demo a Future Direction as a feature. The ladder language
   (Suggested → Logged → Sensor-verified) exists precisely to keep this honest.
2. Promote items out of this doc only with device evidence, not code alone.
   "Implemented" means *measured on hardware*.
3. Re-read this file before every investor/partner deck. The fastest way to kill this
   idea's credibility is to let the roadmap become the pitch.

*Hackathons reward the best honest prototype of a big idea — not a fake finished
product. This repo chose correctly.* 🏆
