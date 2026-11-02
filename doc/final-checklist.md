# MetaMuse — Final Pre-Submission Runbook ✅

Everything Muse Code could do from here is done (code, tests 42/42 green, docs,
icon, live rule engine). This runbook is the **human-only remainder**: device work,
proof capture, and submission. Do the steps in order; each ends with an expected result.
Check the box only when you see it.

---

## Phase 1 — Clean-room build (30 min)

**1.1 Fresh clone to a new directory**
```bash
git clone https://github.com/ashrafi/MetaMuse.git MetaMuse-final-check
cd MetaMuse-final-check
```
Expected: clone succeeds (proves the repo URL in the README is real and public).

**1.2 Verify JDK + SDK**
- `java -version` → JDK 17 or 21.
- `local.properties` must contain `sdk.dir=` pointing at a valid Android SDK, or set
  `ANDROID_HOME`. (This file is gitignored on purpose — create it locally, never commit it.)
- `adb --version` works and `adb devices` shows your test phone.

**1.3 Debug build from zero**
```bash
./gradlew :app:assembleDebug
ls -la app/build/outputs/apk/debug/app-debug.apk
```
Expected: `BUILD SUCCESSFUL`, fresh timestamped APK (~25 MB). This also proves the
committed `debug.keystore` signs correctly on a machine that never saw this repo.

**1.4 Full unit suite**
```bash
./gradlew testDebugUnitTest
grep -h "<testsuite " app/build/test-results/testDebugUnitTest/*.xml
```
Expected: `ExampleUnitTest 8/8`, `ExampleRobolectricTest 15/15`, `LifeRuleEngineTest 9/9`,
`LifeMemoryMomentsTest 5/5`, `SyncFallbackMathTest 5/5`, zero failures/errors — **42/42 green**, matching the last verified run.

---

## Phase 2 — Install + first-run impression (15 min)

**2.1 Install exactly as the README says**
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.aistudio.serenity.lmns/com.example.MainActivity
```
Expected: app launches to Home with Lumi **Radiant ✨ on the Meta Horizon (Meta Blue)
theme** — no onboarding, no key prompt blocking the screen.

**2.2 Launcher check.** Find the app in the launcher.
Expected: new Meta Blue→Violet Lumi starburst icon, app name **MetaMuse**.

**2.3 Kill + relaunch.** Force-stop, reopen.
Expected: theme, companion name, and vitals persist; still Radiant.

---

## Phase 3 — The 5-state demo rehearsal (45 min)

Rehearse [demo-script.md](demo-script.md) verbatim. For **each** of the 5 states
(science-icon menu → Live Data, Healthy Baseline, Success Trap, Money Leak,
Connection Debt):

- [ ] Correct Lumi mood (Radiant / Radiant / Sick 🤒 / Sick 🤒→Tired narrative / Sad 🥺).
- [ ] Diagnosis card renders with headline + prescription + WhatsApp nudge (no blank cards).
- [ ] Tapping the WhatsApp nudge opens WhatsApp with a pre-filled message
      (or the system share sheet on devices without WhatsApp).
- [ ] 🔊 voice readout speaks; change pitch/tempo in Connections → Voice Output and
      confirm the voice audibly changes + persists after relaunch.
- [ ] **Log Follow-Through** returns Lumi to Radiant ✨ (self-reported; sensor-verified recovery is roadmap).

**3.1 Zero-key airplane pass (the most important 10 minutes).**
Enable airplane mode, revoke/clear any entered keys, and repeat the 5 states.
Expected: every beat works offline via the on-device engine — advice, nudges, voice,
recovery. No crashes, no infinite spinners, no empty cards. If anything hangs here,
it will hang for a judge.

**3.2 Live-data pass.** With real permissions granted (Health Connect, Calendar,
Contacts) and ideally a bank CSV imported, select **Live Real Data** and tap sync.
Expected: vitals/radar reflect your real numbers; if your real data matches a
cross-channel pattern you may see a genuine `LIVE RULE TRIGGERED` explanation —
screenshot it if so, it's the strongest possible demo evidence.

**3.3 Cloud proof pass.** Enter a real Meta Model API key, trigger Success Trap,
tap **Consult Muse for Advice**.
Expected: card shows `modelUsed: Meta Muse Spark 1.3 (Meta Model API)`. Screenshot
this card — it is the flagship-claim proof. Then **Remove Key** and confirm the app
falls back to offline cleanly.

---

## Phase 4 — Capture submission assets (45 min)

**4.1 Screenshots** (physical device, light mode, Meta Horizon theme) into
`doc/screenshots/` per the shot list: home-radiant, success-trap-sick,
whatsapp-nudge (crop personal chats), follow-through-radiant, bio-radar, connections.
Expected: 6 PNGs at device resolution, status-bar clutter cropped.

**4.2 Demo video (60–90 s).** Record the [demo-script.md](demo-script.md) flow with
voiceover, upload unlisted to YouTube/Loom.
Expected: shareable link, audible narration, shows WhatsApp handoff + voice readout.

**4.3 Link assets in the README.** Embed the 7 screenshots in the Three-Tab Interface
section and the video link near the top (below the badges).
Expected: README renders fully offline in GitHub preview — no broken image links
(use relative `doc/screenshots/...` paths, verify after push).

---

## Phase 5 — Repo + rules compliance (20 min)

- [ ] `git status` clean; everything committed and **pushed**; repo **public**.
- [ ] `LICENSE` (MIT) present; author credit line final.
- [ ] Re-read the hackathon's official rules once: eligibility, deadline + timezone,
      required submission fields (repo link, video link, description), license
      constraints, and any AI-disclosure requirement — MetaMuse already discloses
      AI assistance in the README; mirror that in the portal if asked.
- [ ] Confirm no secrets anywhere: `git log -p -- .env | head` empty, no tokens in
      README/screenshots/video (revoke + re-shoot if a key is visible in any pixel).

---

## Phase 6 — Submission + day-of kit

- [ ] Submit through the official portal before the deadline; save the confirmation.
- [ ] Day-of kit on the demo phone + a backup: release-mode APK installed, airplane-mode
      rehearsed, charger, hotspot-independent (demo must survive dead venue Wi-Fi —
      that is what Phase 3.1 proved).
- [ ] One-page talk track ready: problem (metric silos → invisible burnout) → Lumi mirror
      → Success Trap live → WhatsApp tap → logged recovery → "only Meta sees
      connection debt" closer (see [meta-product-vision.md](meta-product-vision.md)).

---

*When every box above is checked, the project is done. Good luck — go win it.* 🏆
