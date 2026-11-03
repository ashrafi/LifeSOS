# Project Halo-Band: Meta Neural Band as the World's Best Fitness Tracker ⌚🏆

**Thesis:** the band is already on the wrist for input. Adding world-class sensing costs
the user zero behavior change — and turns every glasses buyer into a fitness-tracker
owner, and every fitness-tracker lover into a future glasses buyer. The band is the
catalyst in both directions.

**Design targets (non-negotiable):**
1. Clinical-grade *trends* (absolute accuracy where it matters: HR, SpO2, sleep).
2. 7-day battery with 24/7 sensing — charging is the #1 tracker killer.
3. Zero-maintenance sensing — dry electrodes, no gels, no daily rituals.
4. Privacy-first — raw biosignals never leave the wrist; features only.
5. A coach, not a dashboard — every sensor exists to feed Lumi's Life Engine.

**Contents:**
- [`hardware/spec.md`](hardware/spec.md) — form factor, sEMG array, optical/motion/thermal
  sensors, compute, power budget, materials, durability, manufacturing, regulatory hooks.
- [`software/spec.md`](software/spec.md) — firmware, DSP pipelines, on-device ML,
  metric algorithms, phone SDK, Health Connect + LifeSOS integration, cloud/security,
  validation protocols, competitive teardown, roadmap.

*Status: proposal spec. All figures are engineering targets ("shall") or explicit
stretch goals ("should") — nothing here claims to describe shipped Meta hardware.*

> Schematics: [`hardware/diagrams.md`](hardware/diagrams.md) (system block diagram,
> sEMG signal chain, power tree — hand-authored SVG).
