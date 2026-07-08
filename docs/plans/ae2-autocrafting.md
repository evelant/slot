# AE2 Autocrafting Plan

Last updated: 2026-07-08

Status: queued planning. Do not implement as part of the AE2 storage bridge
unless this plan is explicitly activated.

## Summary

SLOT's AE2 integration currently treats stored ME items as persistent
media-set storage and open/physical terminals as live access routes. That is
correct: stored stacks and craftable patterns are different promises.

This plan adds a separate AE2 autocrafting route for craft-run deficits. A
tracked recipe may show that a missing output or ingredient is craftable by the
nearby ME network, then offer an explicit "Request from ME" action. The first
implementation should not submit AE2 jobs automatically when the player clicks
`Add Recipe`.

## Product Decision

`Add Recipe` means "track, gather, and stage this work." It must not also mean
"start a long-running AE2 job that consumes power, CPUs, and ingredients."

Near-term behavior:

- Actual stored ME items remain searchable, takeable, gatherable, and
  depositable through the existing storage bridge.
- AE2 craftables are displayed as a distinct route, not merged into normal
  storage counts.
- A craft-run output or ingredient deficit may expose an explicit request
  control when the same nearby/open AE2 terminal context can craft it.
- Requesting from AE2 asks AE2 to produce the chosen item/count. Once the item
  lands in ME storage, SLOT sees it through the existing item-storage bridge.

Deferred behavior:

- Auto-requesting craftable deficits when a recipe is added.
- Pattern terminals, fluids, cross-network request routing, and automatic
  requests from remembered-but-not-live AE2 networks.
- SLOT-owned progress/cancel UI for AE2 jobs.

## Current Baseline

The landed Forge 1.20.1 AE2 bridge is item-only:

- Mounted storage-cell media defines persistent `ae2:network:<hash>` records.
- Nearby physical item/crafting terminal parts and open item/crafting terminal
  menus refresh the same media-set storage record when mounted media is
  discoverable.
- Open item/crafting terminal menus also expose provider-backed `ae2:terminal`
  storage for terminal-screen mutations.
- Counts come from `MEStorage.getAvailableStacks()` and only `AEItemKey`
  entries are exposed.
- Known AE2 storage-bus aliases are subtracted from ME counts before SLOT uses
  storage availability math.
- Mutations use `StorageHelper.poweredInsert(...)` and
  `StorageHelper.poweredExtraction(...)` with
  `IActionSource.ofPlayer(player, actionHost)`.

Relevant local code:

- [`Ae2StorageBridge.java`](../../forge-1.20/src/main/java/dev/imagio/slot/forge/compat/ae2/Ae2StorageBridge.java)
- [`ForgeAe2WorldStorageDelegate.java`](../../forge-1.20/src/main/java/dev/imagio/slot/forge/compat/ae2/ForgeAe2WorldStorageDelegate.java)
- [`Ae2TerminalInventoryIntegrationProvider.java`](../../forge-1.20/src/main/java/dev/imagio/slot/forge/compat/ae2/Ae2TerminalInventoryIntegrationProvider.java)
- [`WorkspaceCraftRunCommandService.java`](../../common/src/main/java/dev/imagio/slot/inventory/workspace/WorkspaceCraftRunCommandService.java)

## AE2 API Findings

Use the public `appeng.api.networking.crafting.*` surface from AE2 15.4.10.

Primary APIs:

- `IGrid.getCraftingService()`
- `ICraftingService.isCraftable(AEKey)`
- `ICraftingService.getCraftingFor(AEKey)`
- `ICraftingService.getCraftables(AEKeyFilter)`
- `ICraftingService.beginCraftingCalculation(level, requester, key, amount,
  CalculationStrategy.REPORT_MISSING_ITEMS)`
- `ICraftingService.submitJob(plan, requester, cpu, prioritizePower, source)`
- `ICraftingPlan.usedItems()`, `emittedItems()`, `missingItems()`,
  `patternTimes()`, `bytes()`, `simulation()`, and `multiplePaths()`
- `ICraftingSubmitResult.successful()`, `errorCode()`, and `errorDetail()`

Important constraints:

- `beginCraftingCalculation(...)` returns a `Future<ICraftingPlan>` and AE2
  explicitly warns not to block waiting for it. SLOT must poll or callback from
  server tick/workflow runtime code.
- `submitJob(...)` rejects incomplete simulation plans. A plan with missing
  items should become user-visible diagnostics, not a submit attempt.
- AE2's own terminal confirmation menu submits player-started jobs with a null
  `ICraftingRequester`. That is the right first implementation model for SLOT:
  the job belongs to AE2, not to a fake SLOT machine.
- Requester-backed links are a separate research slice. AE2 documents that a
  requester must keep `ICraftingLink`s, persist them, and expose them through
  `ICraftingRequester.getRequestedJobs()`. Since `ICraftingRequester` is also an
  AE2 grid-node service, SLOT should not implement it casually.
- `ICraftingService.isRequesting(...)` / `getRequestedAmount(...)` are useful
  network facts for intermediate emitted items, but the API says a job's final
  output does not count as requested. Do not use them as the sole source of
  truth for final-output progress.

## Architecture

Keep three concepts separate:

1. **Storage availability**: actual item counts that can be extracted now.
2. **Craftability availability**: AE2 patterns that may produce an item through
   a future job.
3. **Craft-run intent**: SLOT's tracked recipe/output/ingredient deficits.

Common code should own the craft-run decision model. Forge AE2 compat should
own only the AE2 API calls.

Proposed seams:

- Add a common `ExternalCraftingAccess` / `NetworkCraftingAccess` style
  contract that can answer craftability and accept server-authoritative request
  commands.
- Register a Forge-only AE2 implementation when `ae2` is loaded.
- Reuse the existing AE2 endpoint resolution path for nearby physical terminals
  and open item/crafting terminal hosts. The endpoint should expose the active
  grid, action host, player action source, and storage service/crafting service.
- Keep craftable facts out of `WorldStorageAccess` and
  `InventorySourceSnapshot`. Those remain actual storage.
- Add craftability facts to workspace projection only as optional route/status
  on craft-run entries or deficit cards.

Server command shape:

- Client sends only stable craft-run references, for example entry id plus
  output/ingredient-group choice. It must not send authoritative stack/count,
  host id, or menu references.
- Server resolves the current `CraftRunState`, selected alternatives, actual
  deficit, active AE2 endpoint, and target `AEItemKey`.
- Server simulates/plans through AE2. If the plan is complete and the command is
  explicitly a submit command, server submits through AE2 with
  `IActionSource.ofPlayer(player, actionHost)`.
- If no active powered terminal/grid exists, fail closed with a diagnostic such
  as `ae2_crafting_endpoint_unavailable`.

## Storage-Bus Aliasing Hazard

AE2 storage buses make the same physical inventory available through two
routes. A chest or crate with a storage bus attached still exposes its own block
inventory to SLOT when the player is nearby, while the ME terminal also reports
the contents of that same inventory through AE2's grid storage service.

Implications:

- Real item duplication is unlikely: extracting through either route should
  mutate the same underlying physical inventory.
- Count duplication is very plausible: SLOT can currently see the direct chest
  entry and the ME terminal display entry as different storage ids, then add
  both to nearby/stored/craft-run visible counts.
- Craft-run deficits can be hidden. If a recipe needs 64 plates and a
  storage-bussed crate contains 32, adding the crate's direct 32 plus the ME
  network's same 32 can incorrectly make the deficit look satisfied.
- Gather/stage can over-plan. SLOT may try to pull the same identity once from
  the direct chest route and again from the ME route; the second mutation should
  fail or partially move after the first route drains the physical stack, but
  the UI/planner will have promised too much.
- Deposit route hints can be misleading. A bussed crate may be both a learned
  chest route and part of the ME network route. The player should not see this
  as two independent destinations with twice the capacity or twice the stock.

Required invariant:

```text
Availability math must count each physical item authority at most once.
Routes may be alternatives; they are not always independent additive stock.
```

The storage bridge now records storage-bus alias target blocks and subtracts
loaded or remembered claimed-chest counts from ME counts. Autocrafting must use
those corrected stored-count facts and must not re-add raw ME aggregate counts
as independent stock. Every extract/insert still simulates against the live
route immediately before mutation and must tolerate a smaller result if another
alias already moved the item.

## First Slice

1. **Craftability discovery**
   - Extend the AE2 bridge with a read-only craftability query.
   - For each craft-run output and selected ingredient deficit, map the
     representative stack to `AEItemKey`.
   - Query `ICraftingService.isCraftable(...)`.
   - Project a distinct "craftable in ME" route/status without changing
     storage counts.

2. **Explicit request command**
   - Add a server-authoritative command for "request this selected craft-run
     deficit from nearby/open ME."
   - Compute the amount from the current craft-run deficit after carried/main
     inventory and actual storage counts are considered.
   - Start AE2 planning asynchronously with
     `CalculationStrategy.REPORT_MISSING_ITEMS`.
   - Return a pending status while planning is in flight.

3. **Plan result handling**
   - If `plan.simulation()` or `missingItems()` is non-empty, surface missing
     item diagnostics and do not submit.
   - If no suitable CPU is available, surface AE2's submit error code/detail.
   - If the plan is complete and the player confirmed the request, call
     `submitJob(plan, null, selectedCpuOrNull, true, actionSource)`.
   - On success, show "requested from ME" and let normal AE2 storage projection
     reveal the crafted item when it arrives.

4. **UI**
   - Add route chrome to tracked recipe outputs/ingredients where the deficit is
     craftable by the active ME network.
   - Add an explicit request control. Do not bind this to `Add Recipe`.
   - Keep staging behavior unchanged: stage pulls actual stored items into
     player main inventory; it does not auto-start crafting.

## Later Slices

- Optional plan-preview UI that summarizes used, craftable, stored, and missing
  items similarly to AE2's confirmation screen.
- CPU selection if playtesting shows null CPU choice is too surprising.
- Durable progress/cancel status. This needs a real design because AE2's
  requester-backed `ICraftingLink` model is intended for AE2 grid services, not
  arbitrary external UI code.
- Optional auto-request mode gated behind explicit player configuration and
  playtest evidence.
- Multi-loader strategy if NeoForge AE2 support becomes important again.

## Tests

Common tests:

- Storage-bussed direct chest contents and ME terminal contents do not satisfy a
  craft-run deficit twice.
- Craftability route facts do not increase stored counts or satisfy extraction
  availability.
- `Add Recipe` never submits an external crafting request.
- Explicit request resolves target/count from server-side craft-run state and
  authority, not from client payload.
- Missing/inactive provider fails closed with a useful diagnostic.
- Stage command still moves only actual available stacks.

Forge tests/smoke checks:

- AE2 absent startup.
- AE2 present compile against `appeng:appliedenergistics2-forge:15.4.10`.
- Storage bus attached to a nearby chest/crate does not double the visible
  availability used by craft-run deficits or request counts.
- Powered terminal marks craftable deficits as requestable.
- Unpowered/inactive terminal fails closed.
- Planning with missing ingredients reports missing items and does not submit.
- Complete plan submits through AE2 and crafted output later appears as normal
  stored ME contents.

Verification command set:

```bash
./gradlew :common:test :neoforge:test :common:compileJava :neoforge:compileJava :forge-1.20:compileSharedProbeJava :forge-1.20:compileJava
```

## Open Questions

- Should the first request target recipe outputs, ingredient deficits, or both?
  Recommendation: both, but each as an explicit button tied to a visible
  deficit.
- Should a successful explicit request keep an ephemeral SLOT status row until
  the output appears, or should AE2's own terminal job UI be the only progress
  surface?
- Is a plan preview necessary before submit, or is "request exact visible
  deficit" clear enough for the first playtest?
- Should repeated request attempts account for already-submitted standalone AE2
  jobs? Without an `ICraftingLink`, SLOT can only infer from storage deltas and
  limited AE2 requested-amount facts.
- Can we reliably identify storage-bus target positions through public AE2 API,
  or should v1 use a conservative non-additive fallback whenever nearby ME
  aggregate storage and direct proximate containers coexist?
- Should auto-request ever happen on `Add Recipe`? Recommendation: no unless a
  later explicit workflow setting and playtest evidence justify it.
