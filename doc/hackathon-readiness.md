# MetaMuse — Hackathon Readiness Plan

**Goal:** a solid, judge-proof, presentable project for the Meta Million Dollar Hackathon.
**Last reviewed:** 2026-11-01 against the current codebase (`SerenityRepository`, `MuseService`,
connectors, `SecureByokStore`, UI screens, tests: 15/15 passing).

Items are ordered by priority. P0 items can break judging or the live demo; P1 items affect
perceived quality; P2 items are nice-to-have polish.

---

## P0 — Must fix before submission (demo/judging blockers)

### 1. Fresh-clone builds are broken — `debug.keystore` is gitignored
- `app/build.gradle.kts` assigns `signingConfig = debugConfig` with
  `storeFile = ${rootDir}/debug.keystore`, but `debug.keystore` is in `.gitignore` and untracked.
  A judge cloning fresh gets a signing failure on `./gradlew assembleDebug`.
- **Fix (pick one):**
  - Commit a shared debug keystore (conventional and safe for `androiddebugkey`), or
  - Make the `debugConfig` block conditional: use the file only if it exists, else fall back to
    the default AGP debug keystore.

### 2. `.env` setup instructions don't work
- README §2 tells judges to set `META_AI_TOKEN`, `PLAID_CLIENT_ID`/`PLAID_SECRET`,
  `FACEBOOK_APP_ID` in `.env`, but [`.env.example`](../.env.example) contains only commented-out
  `GEMINI_API_KEY` / `META_AI_TOKEN` — no Plaid/FB vars at all.
- Worse: **nothing in `app/src/main` reads `BuildConfig`**, so `.env` values are silently ignored;
  the real key path is in-app Settings → BYOK (which is good design, but contradicts the README).
- **Fix:** rewrite README §2 to the true flow (install → Connections → paste key, keys encrypted
  in Keystore), and either expand `.env.example` to match or delete the secrets-plugin path if
  unused. Decide: `.env` *or* in-app BYOK as the documented story — not both.

### 3. Verify the flagship Meta API claim against official hackathon docs
- The app defaults to `https://api.meta.ai/v1/chat/completions` with model `muse-spark-1.3`
  ([MuseService.kt](../app/src/main/java/com/example/data/MuseService.kt)). The code gracefully
  falls back to offline synthesis on any failure, so the demo never crashes — but the entire
  "powered by Meta Muse Spark 1.3 via Meta Model API" narrative must be real and reachable with
  a judge-obtainable token.
- **Fix:** confirm endpoint + model name + token source from official docs; print the exact
  token-acquisition steps in the README; test one real cloud call on a device and keep a
  screenshot of the `modelUsed` field as proof.

### 4. Fix the four README accuracy bugs (judges read the README first)
1. "4 Demo Scenarios" title vs 5 actual states (Live Data, Healthy Baseline, 1–3) in
   [CompanionModels.kt](../app/src/main/java/com/example/model/CompanionModels.kt).
2. `DialogueScreen.kt` / `VaultScreen.kt` are listed as screens but are **dead code** —
   [MainActivity.kt](../app/src/main/java/com/example/MainActivity.kt) routes those nav targets to
   Attune/Vitality. Delete the files or wire them up; don't list dead code.
3. **Llama Guard 3 is prompt-level, not an API gate.** No Guard endpoint is called;
   `llamaGuardStatus` is a string the model echoes. Either add a real Guard classify call or
   reword to "Llama Guard 3 safety-style prompting" and drop the `PASSED` badge claim.
4. **SeaWeed Finance** is named in the README table but has no connector code (one prompt-string
   mention only). Real finance paths: Plaid + SimpleFIN + CSV. Implement or remove the name.

---

## P1 — Presentation quality (what judges notice in 60 seconds)

### 5. Default to the Meta Horizon theme on first launch
- First-run default is `LUMINOUS_SERENITY`
  ([SerenityRepository.kt](../app/src/main/java/com/example/data/SerenityRepository.kt),
  [ConnectorPreferencesStore.kt](../app/src/main/java/com/example/data/connectors/ConnectorPreferencesStore.kt)).
  For a Meta hackathon, judges should see Meta Blue `#0866FF` first. Keep Serenity as the toggle
  alternative.

### 6. Unify branding: MetaMuse vs Serenity
- Launcher label is "Serenity" (`strings.xml`), `rootProject.name` is "Serenity", but the README
  brands "MetaMuse". Pick one user-facing name and apply it to `app_name`, README title, and
  store listing text. Also update stale [metadata.json](../metadata.json) (still says Serenity +
  `SERVER_SIDE_GEMINI_API`, contradicting the Meta-first story).

### 7. Add screenshots + a 60–90s demo video to the README
- There are currently zero visual assets. Minimum: 3 screenshots (Home/Lumi, Bio-Radar,
  Connections) in `doc/screenshots/` + a demo video link (unlisted YouTube/Loom).
- Suggested 60-second judge script:
  1. Open app → Lumi Radiant on Meta Horizon theme (0–10s).
  2. Tap science icon → trigger "Success Trap" → Lumi turns Sick, Spark diagnosis + WhatsApp
     nudge appears (10–35s).
  3. Tap WhatsApp nudge → pre-filled message (35–45s).
  4. Tap "Verify Follow-Through" → Lumi recovers to Radiant; show TTS readout (45–60s).

### 8. Prove the offline / zero-key demo path on a real device
- Code paths look correct (offline synthesis when token blank), but confirm on-device with **no
  keys, no SIM data (airplane mode)** that all 5 states render advice + nudges without errors or
  blank cards. Judges will test the no-config path.

### 9. Repo hygiene
- `.idea/` project files are tracked in git — either commit this deliberately or untrack them
  (keep `runConfigurations` if they help judges run the app).
- Confirm `github.com/ashrafi/MetaMuse` exists, is **public**, and matches the clone URL in the
  README before the deadline.
- Add a `LICENSE` file (check hackathon rules for requirements — Apache-2.0/MIT is typical).
- Update the author/credits section (see open question below).

### 10. Refresh the install proof
- `app-debug.apk` in build outputs is stale relative to sources. After P0 fixes, rebuild from a
  clean clone, re-run `adb install` + `am start` exactly as documented, and confirm the
  `applicationId com.aistudio.serenity.lmns` launch command still matches.

---

## P2 — Polish if time allows

- **TTS wording:** README says "customizable pitch and tempo" but values are hardcoded
  (`1.04`/`0.96` in `SerenityViewModel`). Add sliders or reword to "tuned".
- **Mermaid diagram:** the `SPARK` node has no edges; the reasoning path goes through `LLAMA`.
  Rewire the diagram to match the flagship narrative.
- **SDK versions:** README omits `compileSdk 37` and `minSdk 26`; state the real matrix.
- **Tests section:** README undersells — `ExampleUnitTest` also covers model-engine selection
  and BYOK revocation, and `ExampleRobolectricTest` (11 tests) isn't mentioned at all.
- **Trivia:** SICK emoji is 🤒 in code vs 🤢 in README; example JSON shows
  `"modelUsed": "Meta Llama 3.1"` while code resolves `"Meta Muse Spark 1.3 (Meta Model API)"`.
- **No-Java note:** this environment has no JDK, so the build/test evidence above comes from the
  repo's last recorded run (15/15 green on Nov 1). Re-verify on a machine with JDK 17/21 +
  Android SDK before submitting.

---

## Pre-submission checklist

- [x] Fresh-clone signing fixed — `debug.keystore` un-ignored for commit (P0-1). Still needed: commit it + clean-clone `./gradlew assembleDebug` on a JDK machine
- [x] README setup rewritten to in-app BYOK; `.env.example` corrected (P0-2). Still needed: verbatim run-through on a fresh machine
- [x] Muse Spark endpoint externally verified — Meta Model API at `api.meta.ai/v1` serving `muse-spark-1.3` confirmed via public docs (P0-3). Still needed: one real on-device cloud call + screenshot
- [x] README accuracy bugs fixed — 5 states, dead screens deleted, Guard reworded, SeaWeed removed (P0-4)
- [ ] Meta Horizon is the first-run theme (P1-5)
- [ ] Single product name everywhere (P1-6)
- [ ] Screenshots + demo video linked (P1-7)
- [ ] Airplane-mode demo passes on all 5 states (P1-8)
- [ ] Repo public, license added, credits final (P1-9)
- [ ] Fresh-APK install proof re-run (P1-10)
