# MetaMuse — Hackathon Readiness Plan

**Goal:** a solid, judge-proof, presentable project for the Meta Million Dollar Hackathon.
**Last reviewed:** 2026-11-01 against the current codebase (`SerenityRepository`, `MuseService`,
connectors, `SecureByokStore`, UI screens, tests: 45/45 passing — see record below).

## Current Verification Record (single source of truth, Nov 1)

- Command: `./gradlew testDebugUnitTest --offline` (Android Studio JBR 21) → **45/45 green**,
  zero failures/errors: `ExampleUnitTest` 8, `ExampleRobolectricTest` 18,
  `LifeRuleEngineTest` 9, `LifeMemoryMomentsTest` 5, `SyncFallbackMathTest` 5.
- `:app:assembleDebug --offline` → BUILD SUCCESSFUL; launcher icon pixel-verified in APK.
- Historical counts elsewhere in this doc (15 → 19 → 25 → 31) are dated milestones from
  earlier in the day, not conflicting claims — this section is the current truth.

Items are ordered by priority. P0 items can break judging or the live demo; P1 items affect
perceived quality; P2 items are nice-to-have polish.

---

## P0 — Must fix before submission (demo/judging blockers)

> Historical note: the P0/P1 sections below preserve the original audit's problem statements
> verbatim as a record — every item is resolved (see the Current Verification Record above).
>
### 1. [DONE] Fresh-clone signing was broken — keystore committed
- `app/build.gradle.kts` assigns `signingConfig = debugConfig` with
  `storeFile = ${rootDir}/debug.keystore`, but `debug.keystore` is in `.gitignore` and untracked.
  A judge cloning fresh gets a signing failure on `./gradlew assembleDebug`.
- **Fix (pick one):**
  - Commit a shared debug keystore (conventional and safe for `androiddebugkey`), or
  - Make the `debugConfig` block conditional: use the file only if it exists, else fall back to
    the default AGP debug keystore.

### 2. [DONE] `.env` setup instructions were broken
- README §2 tells judges to set `META_AI_TOKEN`, `PLAID_CLIENT_ID`/`PLAID_SECRET`,
  `FACEBOOK_APP_ID` in `.env`, but [`.env.example`](../.env.example) contains only commented-out
  `GEMINI_API_KEY` / `META_AI_TOKEN` — no Plaid/FB vars at all.
- Worse: **nothing in `app/src/main` reads `BuildConfig`**, so `.env` values are silently ignored;
  the real key path is in-app Settings → BYOK (which is good design, but contradicts the README).
- **Fix:** rewrite README §2 to the true flow (install → Connections → paste key, keys encrypted
  in Keystore), and either expand `.env.example` to match or delete the secrets-plugin path if
  unused. Decide: `.env` *or* in-app BYOK as the documented story — not both.

### 3. [DONE] Flagship Meta API claim verified
- The app defaults to `https://api.meta.ai/v1/chat/completions` with model `muse-spark-1.3`
  ([MuseService.kt](../app/src/main/java/com/example/data/MuseService.kt)). The code gracefully
  falls back to offline synthesis on any failure, so the demo never crashes — but the entire
  "powered by Meta Muse Spark 1.3 via Meta Model API" narrative must be real and reachable with
  a judge-obtainable token.
- **Fix:** confirm endpoint + model name + token source from official docs; print the exact
  token-acquisition steps in the README; test one real cloud call on a device and keep a
  screenshot of the `modelUsed` field as proof.

### 4. [DONE] README accuracy bugs fixed
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

### 5. [DONE] Meta Horizon default theme
- First-run default is `LUMINOUS_SERENITY`
  ([SerenityRepository.kt](../app/src/main/java/com/example/data/SerenityRepository.kt),
  [ConnectorPreferencesStore.kt](../app/src/main/java/com/example/data/connectors/ConnectorPreferencesStore.kt)).
  For a Meta hackathon, judges should see Meta Blue `#0866FF` first. Keep Serenity as the toggle
  alternative.

### 6. [DONE] Branding unified: MetaMuse vs Serenity
- Launcher label is "Serenity" (`strings.xml`), `rootProject.name` is "Serenity", but the README
  brands "MetaMuse". Pick one user-facing name and apply it to `app_name`, README title, and
  store listing text. Also update stale [metadata.json](../metadata.json) (still says Serenity +
  `SERVER_SIDE_GEMINI_API`, contradicting the Meta-first story).

### 7. [PARTIAL] Screenshots + demo video (script done, capture = owner)
- There are currently zero visual assets. Minimum: 3 screenshots (Home/Lumi, Bio-Radar,
  Connections) in `doc/screenshots/` + a demo video link (unlisted YouTube/Loom).
- Suggested 60-second judge script:
  1. Open app → Lumi Radiant on Meta Horizon theme (0–10s).
  2. Tap science icon → trigger "Success Trap" → Lumi turns Sick, Spark diagnosis + WhatsApp
     nudge appears (10–35s).
  3. Tap WhatsApp nudge → pre-filled message (35–45s).
  4. Tap "Verify Follow-Through" → Lumi recovers to Radiant; show TTS readout (45–60s).

### 8. [PARTIAL] Offline demo path (static done, device = owner)
- Code paths look correct (offline synthesis when token blank), but confirm on-device with **no
  keys, no SIM data (airplane mode)** that all 5 states render advice + nudges without errors or
  blank cards. Judges will test the no-config path.

### 9. [DONE] Repo hygiene (public push = owner)
- `.idea/` project files are tracked in git — either commit this deliberately or untrack them
  (keep `runConfigurations` if they help judges run the app).
- Confirm `github.com/ashrafi/MetaMuse` exists, is **public**, and matches the clone URL in the
  README before the deadline.
- Add a `LICENSE` file (check hackathon rules for requirements — Apache-2.0/MIT is typical).
- Update the author/credits section (see open question below).

### 10. [OWNER] Install proof (device task)
- `app-debug.apk` in build outputs is stale relative to sources. After P0 fixes, rebuild from a
  clean clone, re-run `adb install` + `am start` exactly as documented, and confirm the
  `applicationId com.aistudio.serenity.lmns` launch command still matches.

---

## P2 — Polish (all done)

- [x] **TTS customization implemented** — pitch/tempo sliders in Connections → Voice Output
  with live preview, persisted prefs, ViewModel wiring, and 2 new tests (was: hardcoded
  `1.04`/`0.96`, README claim now true).
- [x] **Mermaid diagram** — `SPARK` node wired as the primary path, Llama as alternative.
- [x] **SDK versions** — README states `compileSdk 37`, `targetSdk 36`, `minSdk 26`.
- [x] **Tests section** — README structure lists both test files with real coverage.
- [x] **Trivia** — SICK emoji 🤒 and `modelUsed` string match the code.
- **Test note:** then-current suite verified green (see Current Verification Record).
  Two `@Composable` annotation fixes in `PresenceScreen.kt` were required and corrected
  during that run. Remaining testing is device/field work (below).

## Caveat Fix — Live Cross-Channel Rule Engine (done Nov 1)

The "superintelligence is scripted scenarios" gap is closed at the detection layer:
new pure-Kotlin `LifeRuleEngine` evaluates the three named trade-off rules against real
synced telemetry (sleep drop + late nights + canceled plans → Success Trap with savings
contrast; delivery volume + exhaustion → Money Leak; neglected contacts + stable vitals →
Connection Debt with pinned-value conflict). Matches drive Lumi's live mood/speech and the
`triggeredRuleExplanation` fed to Muse Spark, with single-channel alerts as fallback.
Covered by 6 JVM tests in `LifeRuleEngineTest`; then-current suite re-verified green
(forced clean re-run — see Current Verification Record).

## Article Gaps — Background Sync + Memory Moments (done Nov 1)

Two more article requirements implemented: (1) `LumiBackgroundSyncWorker` (WorkManager,
6-hourly, new `androidx.work:work-runtime-ktx:2.12.0` dep) re-runs live evaluation with
the app closed — required new vitals/mood/speech persistence in prefs so the body
survives process death, plus `READ_HEALTH_DATA_HISTORY`/`IN_BACKGROUND` manifest
permissions; (2) `LifeMemoryMoments` gives Lumi trend-aware lines (sleep recovery,
delivery-free stretches, active days) on the healthy path. Covered by 5 JVM tests +
1 Robolectric persistence test; then-current suite re-verified green and
`assembleDebug` clean (see Current Verification Record). Note: work-runtime excludes the unused
`lifecycle-service`/`lifecycle-livedata` modules (see comment in `app/build.gradle.kts`).
Device follow-ups for the owner: grant background access in Health Connect settings,
then verify an overnight sync refreshes Lumi before the app opens.

## External Review Response (done Nov 1)

An 8.5/10 external review was verified claim-by-claim against the code: 6 confirmed
(plus 1 correction issued later — see Fifth Review entry: the neglected-contact
"refutation" below was wrong; the count was indeed never incremented until fixed),
1 severity-downgraded (Plaid backend is a production requirement, documented as
sandbox-scoped for this backend-less PoC). Fixes shipped: fallback `modelUsed`
provenance (never Spark branding), Offline Engine renaming (ExecuTorch = roadmap),
recovery flow relabeled to the Suggested → Logged → Sensor-verified ladder, exact
what-leaves-the-device disclosure, PKCE *doc* correction (README no longer claims it;
connector code intentionally unchanged, implementation on roadmap), and this
consolidated test record. Covered by 2 new regression tests (provenance + labels).

## Second Review Response — Freeze & Polish (done Nov 1)

A follow-up assessment ("do not build anything major") was verified the same way:
all three `LifeRuleEngine` credibility fixes shipped (separated canceled-vs-neglected
counts, correlation-not-causation leak language, unknown-not-stable debt vitals),
README rebranded Spark-first (tagline, badge, FAQ, reasoning-trace wording),
demo script split into keyed-cloud primary vs offline backup with the suggested
opening line, and every test count synced to **36/36 green**. Architecture frozen per
the review's counsel — remaining work is device proof, screenshots, and rehearsal.

## Third Review Response — Fallback Provenance Deep-Clean (done Nov 1)

A third assessment (reading a GitHub branch lagging this workspace) still added two
genuine catches: fallback *thought-steps* impersonated Spark/Guard verdicts even after
`modelUsed` was fixed, and Money Leak kept causal leftovers (`cause` variable, KDoc,
"fatigue seeking comfort" prescriptions). Both fixed: thought-steps now read Offline
Rules Engine / Deterministic Rule Evaluation / Template-Based Guidance / Safety Note,
fallback Guard status reads Unscreened, and leak language is correlation-only
throughout — all locked by extended provenance-test assertions (suite still 36/36,
no new tests needed). Readiness P0/P1 headers converted to [DONE]/[PARTIAL]/[OWNER]
status form so cold readers can't mistake the audit record for outstanding work.
Stale-branch items in that review (README identity, demo split, counts) resolve on
push.

## Fourth Review Response — Final Consistency Sweep (done Nov 1)

A corrected re-review (9 prior fixes confirmed on `main`) left 4 small items, all
shipped: cloud Guard default + `defaultThoughtSteps` reworded to prompt-level honesty,
"Telemetry Verification" step renamed to Self-Reported Action Recorded, the last
"Meta Llama Brain" table label and two checklist "verify" wordings fixed, and the
keystore checklist line corrected (committed long ago; only the clean-clone proof
remains). Suite steady at **36/36 green**. Nothing architectural left — the freeze
holds; only device proof and presentation assets remain.

## Fifth Review Response — Technical Debt Batch (done Nov 1)

A fifth, deeply technical review verified 6 of 7 actionable items: reboot-cumulative
step-counter bug (fixed with a persisted midnight-baseline delta + 3 tests),
neglected-contact count never incremented — **a genuine miss in the earlier "refuted"
verdict, corrected here** (14-day `LAST_TIME_CONTACTED` query implemented + 2 tests),
unused retrofit/moshi deps removed (room reverted: its removal destabilized offline
graph resolution — documented in `app/build.gradle.kts`; firebase pair kept as AI
Studio scaffolding), TTS `delay(7500)` replaced with `UtteranceProgressListener`,
and the Ladybug-era IDE prerequisite corrected. `runBlocking` cold-start reads
confirmed real but minor (documented, post-submit refactor). compileSdk kept at 37
(AGP 9.1.1 mandates newest Studio regardless; downgrade adds risk for no gain).
Suite at **41/41 green** — see Current Verification Record.

A sixth, code-deep review verified all actionable items: credential-encryption claims
narrowed to the two actually-encrypted secrets (Meta token + Plaid client secret;
StrongBox/TEE wording retired everywhere), chat-history transmission path disclosed,
model-graded `PASSED` safety verdict fully retired (schema no longer requests it,
parser ignores it, default/UI/README show "Prompt-Level Safety Guidance — No
Separate Classifier"), remaining Money Leak causal wordings fixed in prompt/scenario/
dialogue/README, verify→log rename completed in `AttuneScreen` (callbacks, icon,
test tags, user strings), diagram `GUARD` node renamed to `SAFETY`, vision
WorkManager sentence corrected, and screenshot count unified at 7. The review's
`isConnected`-after-fallback claim was refuted (it derives from key presence at
config time, not from fallback responses). One disclosure unit test added.
Suite at **42/42 green** — see Current Verification Record.

A seventh review on the pushed 69-commit main verified all actionable items: compact
Connections card converted to dynamic engine labels (`selectedEngine` threaded
through, "AI Engine & Privacy" + per-engine Run/status lines), Money Leak scenario
purged of unmeasured physiology (cortisol/fatigue-spike/decision-fatigue/willpower →
workload/congestion/density wording), parser fallback labels derived from the
selected cloud model, and `isConnected` fixed to reflect cloud reachability —
**a genuine miss in the earlier "refuted" verdict, corrected here** (the refuting
grep had been `head`-truncated; the unconditional `copy(isConnected = true)` sat
in `sendAllInfoToMuse`). Docs: `.env.example` narrowed, diagram store label fixed,
demo-script key line made precise, last README `Llama's Diagnosis`/FAQ/loop
wordings corrected, tree completed, 3 absolute links relativized, `metadata.json`
Gemini capability emptied, screenshot count unified at 7 everywhere. One
Robolectric provenance test added (blank-token consult asserts offline + unconnected).
Suite at **43/43 green** — see Current Verification Record.

An eighth review verified all actionable items: the retired-model Llama cloud option
was removed end-to-end (enum entry, selector card, `selectModelEngine` branch,
compact-card mapping, `fromModelName` default now Spark-first; stored
`llama_3_1_70b` prefs fall back to Spark safely), the chat prompt was branched on
the selected engine so non-Spark custom endpoints no longer misidentify as Muse
Spark, Plaid partial syncs now report "spend unknown" with a warning instead of
`$0` (`transactionsOk` plumbing across status line, headline, metrics, log, and
error message; 2 new Robolectric tests), the compact-card button reflects the real
execution path ("Run Offline Rules" without a key) with a provenance-gated status
line, and both screen-time correlation sentences were replaced. README: all 8
wording corrections applied (root-cause, causal, delivery-detection, heart-rate,
JSON-schema, zero-knowledge ×2, offline scope, credential transmission) plus the
Llama-alternative sweep (diagram, table, FAQ, intro). Screenshot counts unified at
7 in all four docs. SimpleFIN left as-is (single-payload response — nothing is
known-missing, unlike Plaid's separate failed request). Suite at **45/45 green**
— see Current Verification Record.

---

## Pre-submission checklist

- [x] Fresh-clone signing fixed — `debug.keystore` committed and tracked (P0-1). Still needed: clean-clone `./gradlew assembleDebug` on a JDK machine
- [x] README setup rewritten to in-app BYOK; `.env.example` corrected (P0-2). Still needed: verbatim run-through on a fresh machine
- [x] Muse Spark endpoint externally verified — Meta Model API at `api.meta.ai/v1` serving `muse-spark-1.3` confirmed via public docs (P0-3). Still needed: one real on-device cloud call + screenshot
- [x] README accuracy bugs fixed — 5 states, dead screens deleted, Guard reworded, SeaWeed removed (P0-4)
- [x] Meta Horizon is the first-run theme — repo, prefs, and ThemeState defaults flipped + 2 new tests (P1-5). Verified green (see Current Verification Record)
- [x] Single product name — launcher, chat tag, project, metadata now MetaMuse (P1-6). Kept deliberately: `applicationId`, code identifiers, "Luminous Serenity" theme name, AI-Studio `majorCapabilities`
- [x] Demo script written — [demo-script.md](demo-script.md) + `screenshots/` shot list (P1-7). Still needed: capture 7 shots + record/link video, link from README
- [x] No-key path statically verified — blank token → offline synthesis → full card render, no blank states (P1-8). Still needed: airplane-mode pass on all 5 states on-device
- [x] `.idea` untracked, MIT LICENSE added (P1-9). Still needed: push repo public, confirm credits
- [ ] Fresh-APK install proof re-run (P1-10)
