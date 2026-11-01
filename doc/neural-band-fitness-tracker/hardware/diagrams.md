# Hardware Diagrams 📐

Hand-authored vector schematics (edit the `.svg` sources directly — no export step).
They render inline on GitHub and in most Markdown viewers.

## System Block Diagram

![System block diagram](system-block-diagram.svg)

Full signal architecture: sensors → AFE → sensor hub → SoC+NPU → radios/haptics,
plus the power subsystem. See [`spec.md`](spec.md) §2–§6 for the numbers behind
every block.

## sEMG Signal Chain

![sEMG signal chain](semg-signal-chain.svg)

Per-channel path from dry electrode to classified token. The key guarantee, drawn as
the boundary it is: raw waveforms never leave this chain.

## Power Tree

![Power tree](power-tree.svg)

Cell → PMIC → four gated rails → loads, annotated with the daily budgets that sum to
the 7-day battery target.

## Main Interconnect (reference wiring)

![Main interconnect](main-interconnect.svg)

Pin-level buses: SPI0 to the sEMG AFE, I2C1 sensor trunk, QSPI flash, I2C2 to secure
element / NFC / haptics, SPI2 to UWB, RF to the antenna match — plus star-routed power
rails. Typical values; requires EE review.

## sEMG Front-End Detail (reference wiring)

![sEMG front-end](semg-frontend.svg)

One fully-wired channel (protection → PGA → filter → ADC → digital, with DRL drive and
impedance injection) plus the 8-pair wrist array map and flex routing rules.
