# LifeSOS User Guide

LifeSOS is a bio-responsive life companion. A little creature named **Lumi**
mirrors your whole life — health, schedule, money, and relationships — and
warns you when one area is quietly costing you another. On first launch she
is **dormant and gray**: nothing is made up, and every channel lights up only
as real data arrives.

There are three tabs: **Home** (Lumi), **Data** (your four channels), and
**Connections** (setup — start here).

---

## 1. Put your name

1. Open the **Connections** tab and scroll to **Your Profile**.
2. Type your name in **Your Name** and pick a name for your companion in
   **Companion Name** (default: Lumi).
3. Tap **Save Profile**.

The profile card also shows Lumi's level, XP, total hugs (pets), and current
evolution stage. While dormant you will see a reminder banner until your
first channel connects.

---

## 2. Add keys and set up connections

Work top to bottom in the **Connections** tab. Anything can be skipped —
unconnected channels simply stay gray.

### Meta Model API key (for cloud AI)

1. In **AI Engine & Privacy**, paste your key into **Meta Model API Key**.
2. Tap **Save Key**. The status chip changes to Keystore Encrypted, and your
   key is AES-encrypted in the phone's hardware Keystore — it is never baked
   into the app or pasted into AI requests.
3. **Remove Key** wipes it.

Without a key, everything still works through the built-in **Offline Rules
Engine** (deterministic local logic — no cloud, no cost).

### Engine choice

- **Meta Muse Spark 1.3 ⭐ STAR** — cloud reasoning over your telemetry.
- **On-Device Mode** — 100% offline privacy mode; nothing leaves the phone.

### Chat memory slider

Right under the key field, **Chat memory** (1–7 messages, default 7) sets how
many prior turns are re-sent with each chat message. More memory = better
context but more tokens per message. The API is stateless, so anything older
than this window is genuinely forgotten by the cloud.

### Health — Google Health Connect

Tap the Health card and grant the Health Connect permissions (steps, sleep,
heart rate, hydration). If Health Connect has no steps for today, LifeSOS
falls back to the phone's hardware step counter.

### Schedule — Calendar & Tasks

Grant Android Calendar permission to read today's events, late-night load,
and declined RSVPs. Optionally paste a Google Tasks token for task sync.

### Social — Facebook & Close Circle

- **Sync Device Starred Contacts & Birthdays** — the validated path. Star
  close contacts in your phone app and LifeSOS watches over them.
- **Facebook Graph v21.0** (token + App ID + OAuth Login) ships
  code-complete, but live use needs a Meta-reviewed Facebook app, so treat it
  as unvalidated for now.

### Money — Bank & Budget (three ways)

- **Plaid** — client ID + secret + environment (Sandbox works with test banks;
  this direct-from-phone integration is a hackathon proof of concept, not
  production banking architecture).
- **SimpleFIN** — paste your bridge setup URL/token.
- **Bank CSV** — import a real bank `.csv` export; delivery merchants are
  detected by name for the Money Leak pattern.

### Voice and theme

- **Voice Settings** — pitch and tempo sliders plus a test listen; Lumi reads
  diagnoses aloud with these settings.
- **Theme Settings** — Meta Horizon (blue) or Luminous Serenity.

### Resync All

The **Resync** button at the top re-reads every connected channel at once.
LifeSOS also re-syncs automatically in the background about every six hours.

---

## 3. Features and how to use each

### Home (front page)

- **Lumi** — her color, mood, and backdrop mirror your state: sunny when
  happy, cloudy when not, starry night when asleep. Tap her to pet her
  (hugs are counted).
- **4 channel chips** — Health / Tasks / Finance / Social scores around her.
  Gray with "—" until that channel has data; tap one to jump to the Data tab.
- **Dialog box** — her latest message, the action pill, and quick replies.
  The **chevron** collapses/expands it.
- **Flask icon** — the five demo states (below).
- **Open Companion Hub** — the full detail view.

### Companion Hub (full Home)

- **Habitat pod** — Life Score, five vital bars, and five caretaker pills
  (+Water, +Breathe, +Walk, +Focus, +Connect). Pills are grayed out while
  dormant and light up with your first signal. The moon icon puts Lumi to
  sleep; waking re-derives her mood from real data.
- **Dialogue stream** — full chat history (survives restarts), quick-prompt
  chips, and the message box. After sending, a spinner plus a status line
  ("Contacting Meta Muse Spark…") shows until her answer lands.
- **Consult LifeSOS for Advice** — packages vitals, quests, and care history
  to the cloud (or offline engine) and returns a diagnosis card. Disabled as
  "Awaiting First Signal" until data exists.
- **Action pill** — appears directly under Consult (and on the front page):
  WhatsApp outreach, calendar blocks, or water/walk logs, depending on the
  advice. WhatsApp pills are grayed out unless WhatsApp is installed.
- **Life Quests, Care Logs, Thought Streams** — tap quests to complete them,
  browse your nurturing history, and read Lumi's reflections.

### Data tab

- **Header + bio-radar rings** — one ring per channel; gray and empty until
  data flows.
- **Sleep wave** — your week of sleep, or a gray baseline with a hint to
  connect Health.
- **Check-in sliders** — self-report clarity, equilibrium, and peace; these
  are your input, always available.
- **Channel cards** — per-channel status, metrics, and history log.
- **Memory Moments** — add, pin, and revisit memories Lumi keeps for you.

### Demo scenarios (flask icon)

Five states for exploring without connecting anything. The last four pump
synthetic data and say so on screen:

1. **Live Real Data** — the real app, honestly empty until you connect.
2. **Healthy Baseline** — everything thriving.
3. **The Success Trap** — high output, crashing sleep and friendships.
4. **Money Leak** — delivery spending alongside late work and short sleep.
5. **Connection Debt** — thriving body and budget, neglected people.

Classic demo: Success Trap → Consult → WhatsApp pill → send the pre-written
check-in → log follow-through → Lumi recovers to Radiant.

### Honest behaviors worth knowing

- Gray + "—" always means **no data**, never zero or failure.
- "Verified" recovery in this prototype means **you logged the action**;
  independent sensor verification is future work.
- Offline advice is deterministic local logic, clearly labeled — never
  presented as a cloud or on-device model response.

### Quick troubleshooting

| Symptom | Meaning |
|---|---|
| Everything gray, Consult disabled | Normal fresh state — connect a channel or pick a demo |
| WhatsApp pill gray | WhatsApp app not installed |
| Chat spinner spins a while | Cloud request in flight; offline reply follows on failure |
| Key field says Key Needed | Paste a Meta key, or use On-Device Mode |
| Social empty | Star contacts on the phone, then re-sync contacts |
