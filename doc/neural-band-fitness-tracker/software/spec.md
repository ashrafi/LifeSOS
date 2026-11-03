# Software Spec: Neural Band Fitness Stack ⌚💻

*Proposal spec. "Shall" = V1 requirement; "should" = stretch. All on-device ML runs
on the band NPU; the phone is a display, sync hub, and Life Engine host.*

---

## 1. Firmware & Platform

- **RTOS** on sensor hub (bare-metal scheduling, < 100 µW floor); lightweight OS on
  apps core. No Android-on-wrist — power budget forbids it.
- **Power states:** DEEP (hub only) / SENSE (sEMG+IMU windows) / ACTIVE (PPG+NPU) /
  SYNC (radios). Shall spend > 90% of wall-clock in DEEP+SENSE.
- **OTA:** A/B partitions, shall support delta sensor-firmware + full ML-model swaps;
  models versioned independently of firmware (ship a better sleep stager without
  touching the radio stack).
- **Crash/anon telemetry:** shall log sensor-quality stats (impedance, PPG lock %),
  never raw biosignals; user-toggleable, default ON for quality stats only.
- **Time sync:** shall discipline the band clock to phone time on every sync (sleep
  staging and multi-device fusion require < 1 s skew).

## 2. DSP Pipelines (firmware, always-on capable)

### 2.1 sEMG (5–250 Hz band)
1. Hardware notch 50/60 Hz → digital bandpass 5–250 Hz → per-channel impedance gate
   (drop bad channels, don't interpolate silently).
2. Windowed RMS (125 ms windows, 50% overlap) → resting baseline via 10-min rolling
   median → normalized activation envelopes.
3. Outputs: gesture tokens (to §3.1), **tension index** (sustained high-RMS-while-
   still = stress/exertion/focus proxy for Lumi), activity-onset edges.

### 2.2 PPG
1. Ambient-phase subtraction → auto-gain LED drive → accelerometer-referenced NLMS
   adaptive motion cancellation.
2. Beat detection (template + slope) → inter-beat intervals → HR + HRV (RMSSD, SDNN,
   pNN50) in 5-min windows; SpO2 via red/IR ratio-of-ratios with per-user calibration.
3. Signal-quality score per window (0–100); windows < 40 are discarded, not faked —
   missing data beats wrong data for a coach's credibility.

### 2.3 IMU + baro + temp
1. Madgwick/Mahony fusion → orientation + gravity-removed linear accel.
2. Step detection (peak + cadence lock), fall/impact events, sleep actigraphy channel.
3. Baro elevation gain (hysteresis-filtered floors), temp baselines (14-night rolling).

## 3. On-Device ML Models (NPU, INT8 quantized)

| Model | Input | Output | Target |
| :--- | :--- | :--- | :--- |
| Gesture CNN | 8–16 ch sEMG envelopes, 500 ms | pinch, drag, flick, fist, rest + tension | > 90% top-1 wrist accuracy (per article) |
| Activity classifier | IMU + baro + HR, 10 s windows | still/walk/run/cycle/swim/weights/sleep | > 95% on locomotion classes |
| Sleep stager | Actigraphy + HR + HRV + temp + SpO2 | wake/REM/light/deep, 30 s epochs | ≥ 0.75 κ vs PSG (match Oura-class) |
| HRV quality gate | PPG morphology + motion | accept/reject window | < 5% bad-window leakage |
| Stress index | EMG tension + HRV + context (calendar/typing) | 0–100, 15-min cadence | Directional validity vs EMA self-report r > 0.5 |
| Arrhythmia screen (should, regulated) | PPG tachogram | AFib-risk flag → confirm via ECG path | Sensitivity/specificity per FDA guidance |

- Models shall be **personalized on-device** (per-user thresholds/baselines; no raw
  data upload for training by default). Federated learning is a should with explicit opt-in.
- Model cards (accuracy by skin tone, age, BMI band) shall ship in-app. Transparency
  is the trust moat.

## 4. Metric Algorithms (what the user sees)

| Metric | Inputs | Cadence | Accuracy target |
| :--- | :--- | :--- | :--- |
| Resting HR | Overnight PPG | Daily | ±2 bpm vs Holter |
| HRV (RMSSD) | Overnight 5-min windows | Daily + trend | ±10% vs ECG |
| SpO2 | Overnight continuous | Nightly avg + dips | ±2% (70–100%) |
| Sleep stages + score | §3 stager | Morning | Staging κ ≥ 0.75 |
| Steps / distance | IMU + stride model | Real-time | ±5% on 10k-step course |
| Workouts (auto) | Activity + HR zones | Auto + manual | > 95% detection, < 1 false/day |
| VO2max estimate | HR-vs-pace + HRV | Weekly | ±3 ml/kg/min vs lab |
| Training load / recovery | 7-day load + sleep + HRV + RHR drift | Daily | Directional + injury-risk flags |
| Stress / tension | EMG + HRV + context | 15-min | EMA-validated (above) |
| Respiratory rate | PPG + stillness | Nightly | ±2 brpm |
| Skin-temp deviation | 14-night baseline | Nightly flag | Illness-onset sensitivity study |
| Energy expenditure | HR + motion + profile | Daily | ±15% vs doubly-labeled water cohort |

## 5. Phone Stack & APIs

- **Meta Wearables SDK (DAT extension):** `BandFitness` API surface — live
  `DeviceMotionEvent`/orientation (existing) + new `BandVitals` stream (HR, tension,
  activity tokens) + historical batch sync. Third-party apps get *user-consented,
  per-stream* access; raw sEMG is never exposed. Ever.
- **Health Connect: write, don't just read.** The band shall *contribute*
  Steps/Sleep/HeartRate/SpO2 records to Health Connect, making Meta a first-class
  citizen in Android's health graph (and feeding every HC reader, including competitors'
  apps — confidence as strategy).
- **LifeSOS Life Engine feed:** macro-state (Health Connect batch: sleep, RHR, HRV)
  + micro-state (band real-time: tension spike? just stood up? mid-workout?) — exactly
  the article's split. Life Engine rules (§repo `LifeRuleEngine`) gain band-native
  triggers: `TENSION_SUSTAINED_30MIN`, `SEDENTARY_WITH_ELEVATED_HR`, `WORKOUT_COMPLETED`.
- **Background:** phone-side `WorkManager` sync (already prototyped in-repo as
  `LumiBackgroundSyncWorker`) pulls band batches 4×/day + on significant events.

## 6. Cloud, Security & Privacy

- Raw sEMG/PPG waveforms shall never leave the wrist; only features, windows, and
  consent-scoped aggregates sync.
- Per-user **Muse Secure VM** processing (systemd-nspawn isolation), **Sentinel**
  deterministic policy gate + eBPF taint tracking on every egress, trajectory to
  **Confidential VMs** (user-held keys; not even Meta can read companion state).
- Retention: raw windows 24 h on-band rolling; features 2 years in-VM; user purge =
  cryptographic wipe, one tap, no dark patterns.
- Research opt-in (federated/pseudonymized) funds the model-card transparency loop.

## 7. Validation Protocols (how we prove "world's best")

1. **Bench:** ECG/SpO2 simulators, shaker-table IMU, electrode impedance rigs.
2. **Human factors:** Fitzpatrick I–VI × wrist 130–220 mm × hair density × climate
   chamber; comfort/wear-compliance diary (target: > 90% nights worn at 6 months).
3. **Clinical (IRB, pre-registered):** sleep vs PSG (n ≥ 200 nights), HR/AFib vs Holter
   (n ≥ 300), VO2max vs metabolic cart (n ≥ 120), EE vs doubly-labeled water subset.
4. **Field beta:** 10,000 users, 3 months, pre-registered endpoints (RHR error, staging
   κ, SpO2 ARMS, gesture accuracy, battery distribution, adverse skin events).
5. **Publish everything:** model cards + validation whitepapers. Oura/Whoop won trust
   with data; beat them at their own game.

## 8. Regulatory Pathway

- V1 positions as **general wellness** (HR, sleep, steps, SpO2 wellness) — no clearance
  needed — while hardware (§hw 9) and QMS keep the door open.
- Stand up **ISO 13485 design controls + ISO 14971 risk** from day one so AFib/ECG and
  SpO2-clinical submissions (510(k)/De Novo, EU MDR) are paperwork, not redesign.
- Pre-sub (Q-sub) with FDA before any disease claim; label every metric with its
  validated accuracy band in-app.

## 9. Competitive Teardown (win conditions)

| Rival | Their strength | Their gap we exploit |
| :--- | :--- | :--- |
| Apple Watch | Ecosystem, screen, ECG | 18-h battery; no EMG; dashboard-thinking |
| Whoop | Recovery narrative | Subscription + no screen + no input story |
| Oura | Sleep + form factor | Ring can't do EMG/gestures; no display at all |
| Garmin | Athlete depth, battery | UX stuck in 2014; no AI coach; no social graph |
| Pixel Watch | Fitbit + Google | Fitbit stagnation; no wrist-input platform |
| **Halo-Band** | **Input + sensing fusion, 7-day battery, Lumi coach, glasses synergy, privacy math** | Must-win: PPG skin-tone parity, running-HR lock, comfort |

## 10. Flywheel & Roadmap

- **Flywheel:** glasses success → band installed base → fitness data → Lumi coaching
  quality → glasses value ↑ → attach ↑. Price the band as a wedge (≤ Watch SE);
  monetize glasses + services. Trade-in credit band→glasses.
- **V1 (this spec):** fitness parity-or-better + EMG input + Lumi + Health Connect
  write + 7-day battery.
- **V2:** ECG path live (AFib screening, cleared), illness-onset flags validated,
  federated personalization opt-in, band-only cellular SKU for runners.
- **V3:** closed-loop coaching with verified outcomes at population scale; clinician
  channel (reimbursed remote monitoring); research API powering published studies
  *from* Meta data — the credibility flywheel.

*The band earns the wrist with input, keeps it with battery, and wins it forever by
making you healthier. Then the glasses are already sold.* ⌚👓🪽
