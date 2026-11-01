# Hardware Spec: Neural Band Fitness Edition ⌚🔧

*Proposal spec — engineering targets, not shipped-hardware claims. "Shall" = V1
requirement; "should" = stretch goal.*

---

## 1. Form Factor & Wearability

- **Form:** wristband module + swappable straps (solo-loop fluoroelastomer, woven sport,
  leather/metal for lifestyle). Module shall be strap-agnostic.
- **Weight:** shall be < 28 g total on-wrist (module < 18 g). Every gram above 30 g
  measurably hurts 24/7 wear compliance and sleep tracking.
- **Profile:** module shall be < 11 mm thick, curved caseback matching wrist radii
  25–40 mm. No flat glass slab rocking on small wrists.
- **Sizes:** two caseback curvatures (S/M, M/L) + strap lengths covering 130–220 mm wrists.
- **Water:** shall be 5 ATM + IP68 (swim, shower, sweat). Pool chemicals and saltwater
  shall not degrade electrodes over 2 years (accelerated testing per §8).
- **Skin contact:** caseback shall maintain optical + electrode contact through walking,
  typing, and sleep without user-tightening; strap tension guide printed inside.

## 2. sEMG Array (the differentiator)

The band's reason to exist. Everything else matches Apple/Whoop; this is what they
cannot copy without a band redesign.

- **Topology:** shall be 8 differential dry-electrode channels ringing the wrist,
  should be 16 (denser spatial resolution for finger-level gestures + tension mapping).
- **Electrodes:** dry, gold-plated stainless steel, 6×3 mm pads on sprung contacts
  (±0.8 mm travel) to hold skin contact over tendons. No gels, no disposables, ever.
- **Analog front end (per channel):** input impedance shall be > 100 MΩ, CMRR shall be
  > 100 dB at 50/60 Hz, noise floor shall be < 1 µVrms in 5–250 Hz band.
- **Sampling:** shall be 1 kHz/channel at 16-bit minimum; should be 2 kHz at 24-bit
  for research-grade MUAP morphology.
- **Impedance sensing:** shall inject imperceptible AC to measure contact impedance per
  channel at 1 Hz; channels above threshold are flagged bad and excluded from fusion
  (this is how sweat/hair/loose-fit stop corrupting data silently).
- **Motion artifact:** hardware right-leg-drive equivalent + shielded flex routing;
  residual artifact removed in DSP (§software 2.1) using IMU reference.
- **Power:** sEMG subsystem shall draw < 800 µW average in always-on gesture mode
  (duty-cycled windows + wake-on-energy), < 3 mW in continuous-recording mode.

## 3. Optical Sensing (PPG)

- **Array:** shall be 4× green (525 nm), 2× red (660 nm), 2× IR (940 nm) LEDs with 2–4
  large-area photodiodes, center-caseback, recessed 0.3 mm with oleophobic coating.
- **Modes:** HR/HRV (green, 25–100 Hz adaptive), SpO2 (red+IR, spot + overnight
  continuous), respiratory rate (derived).
- **Skin-tone parity (non-negotiable):** validation cohort shall span Fitzpatrick I–VI
  with < 3 bpm mean absolute error *per group* vs ECG chest strap; LED current
  auto-ranges per user (higher melanin → higher drive, compensated in power budget).
- **Motion cancellation:** accelerometer-referenced adaptive filtering in firmware;
  shall hold lock through walking and conversational gesturing; running-grade lock is
  a should (wrist running HR is where everyone fails — winning here is a headline).
- **Ambient rejection:** shall sample ambient phase between pulses; outdoor sunlight
  shall not saturate (auto-gain + optical shielding skirt).

## 4. Motion, Altitude & Thermal

- **IMU:** 6-axis (accel + gyro), accelerometer range shall be ±16 g at ≥ 208 Hz for
  workout transients; gyro shall be ±2000 dps. 9-axis (magnetometer) is a should —
  compass headings help hiking/running but complicate calibration near laptops.
- **Barometer:** shall resolve < 0.5 m altitude (stairs/floors/elevation gain);
  temperature-compensated, vented but waterproof (ePTFE membrane).
- **Skin temperature:** shall be ±0.1 °C accurate, 0.05 °C resolution, sampled every
  minute overnight (illness onset, recovery, cycle tracking inputs). Ambient sensor for
  compensation. Baselines are per-user over 14 nights, never absolute thresholds.
- **Should (V2 options):** EDA/GSR electrodes (stress corroboration with EMG tension),
  single-lead ECG path (crown electrode + caseback, for AFib screening — see §9).

## 5. Compute, Memory & Radios

- **Main SoC:** ultra-low-power apps processor (Cortex-M/A-class hybrid) + dedicated
  NPU (shall be ≥ 1 TOPS INT8) for on-device gesture CNN, activity classifier, and
  HRV quality gating. Raw sEMG shall never require the phone.
- **Sensor hub:** always-on MCU aggregating IMU/PPG/temp at < 100 µW so the main SoC
  sleeps 22+ h/day.
- **Memory:** shall buffer ≥ 7 days of features + 24 h of raw sEMG windows locally
  (NAND), so a dead phone never loses a week.
- **Bluetooth:** LE 5.4 with LE Audio-ready PHY; shall maintain glasses + phone
  dual-link. Periodic bulk sync, not streaming (power).
- **Should:** UWB for find-my + glasses proximity unlock; NFC for identity/payments.
  No Wi-Fi, no cellular in V1 (power + cost); V2 cellular-SKU optional for band-only
  runners.
- **Haptics:** LRA with < 10 ms rise time; shall render distinct "Lumi nudge" textures
  (concern pulse vs celebration shimmer) — the private back-channel from the vision doc.
- **No mic, no speaker, no camera.** The band senses the body and whispers through
  touch. Voice lives on glasses/phone. This is a privacy feature — say so loudly.

## 6. Power Budget (7-day shall)

| Subsystem | Duty strategy | Daily budget |
| :--- | :--- | :--- |
| sEMG gesture mode | Always-on microwatt windows | ~19 mWh |
| PPG day | 25 Hz + auto-gain, paused in deep focus stillness? No — continuous | ~60 mWh |
| PPG night (SpO2 cont.) | Red/IR interleaved, 8 h | ~40 mWh |
| IMU | Always-on 52 Hz, burst 208 Hz in workouts | ~15 mWh |
| Temp/baro | 1/min + 1 Hz | ~3 mWh |
| NPU inference | Event-driven (< 5% duty) | ~10 mWh |
| BLE sync | 4×/day bulk + glasses link | ~15 mWh |
| Haptics/display-less UI | ~30 events/day | ~3 mWh |
| Sleep baseline | | ~25 mWh |
| **Total** | | **~190 mWh/day → ~1.35 Wh cell for 7 days** |

- Cell shall be ≥ 1.4 Wh Li-Po with 80% capacity after 800 cycles (2+ years of weekly
  charging).
- Charging: magnetic puck, 0–80% in shall be < 30 min ("charge while you shower" week).
- Shall expose battery-per-sensor telemetry to the app for transparency.

## 7. Materials & Sustainability

- Case: 100% recycled aluminum or bio-polymer; straps fluoroelastomer (standard) +
  recycled ocean-plastic woven option.
- Electrodes: gold flash over stainless — shall pass nickel-release (REACH) limits for
  24/7 skin contact.
- Packaging plastic-free; trade-in program credit toward glasses (flywheel!).

## 8. Durability & Testing Gates

- MIL-STD-810H vibration/shock (it lives on wrists that slam doors).
- 2-year accelerated sweat/salt/chlorine electrode contact testing; impedance drift
  shall be < 20%.
- Strap retention: shall survive 15 N pull; module eject (for sport swaps) rated
  5,000 cycles.
- Biocompatibility: ISO 10993 cytotoxicity/sensitization for all skin-contact materials.
- 10,000-user closed beta across wrist sizes, skin tones, hair density, and climates
  before GA — sensor products die on narrow validation cohorts.

## 9. Regulatory Hooks (design in now, certify later)

- Keep an **ECG path** in the electrical design (crown + caseback electrode option)
  even if V1 firmware leaves it dark — it preserves a future 510(k) AFib-screening
  submission without a hardware recall.
- SpO2 shall be architected to OTC wellness accuracy but *documented* so a future
  prescription-grade submission doesn't require re-validation of the optical stack.
- Hardware unique ID + secure element for device attestation (pairs with the Secure VM
  story: the cloud can prove *which* band a stream came from).

## 10. Cost Discipline

- Ranked cost drivers: (1) sEMG AFE + electrode assembly, (2) optical array,
  (3) NPU SoC, (4) battery density, (5) waterproofing. Attack in that order.
- Target posture: price at or below Apple Watch SE while out-sensing it — the band is
  a platform wedge, not a margin play. Margin comes from glasses attach and services.
