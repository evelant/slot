# 0009: AE2 Networks Are Tracked By Mounted Storage Media

Status: accepted

Created: 2026-07-08

This record replaces the earlier live-only AE2 terminal model with persistent
ME network storage records.

## Decision

- AE2 ME item contents are tracked virtual world storage, not only transient
  terminal display storage.
- The durable storage identity is the mounted AE2 storage-cell media set:
  `ae2:network:<hash(sorted(mediaIds))>`.
- SLOT stamps each observed mounted AE2 storage-cell `ItemStack` with a
  SLOT-owned UUID when AE2 does not expose a stable media serial.
- The network identity includes only observed mounted cells with positive
  stored `AEItemKey` contents. Empty cells, fluid/non-item-only cells, and
  cells passing through IO ports are media observations, not network identity.
- Physical terminals, open item/crafting terminal screens, and wireless
  item/crafting terminal screens are access routes to the storage media set,
  not the storage identity itself.
- Remembered AE2 contents remain searchable and can produce wayfinding when a
  physical terminal route is known. Live insert/extract still requires an
  active route and uses AE2 power/security/player action-source checks.
- When a newly observed AE2 media set overlaps an older remembered AE2 record
  with a different storage id, the older record is retired. SLOT chooses a
  temporary undercount over a double-count when drives move, split, or merge.
- Per-media observations also retire remembered records that still claim a
  cell observed empty or non-item-only. This covers IO-port cell-emptying and
  cell-to-cell transfers with no shared media id.
- If a remembered physical route is later proven to point at a different ME
  network or no matching endpoint, the remembered record is demoted to
  unreachable: counts remain searchable, but mutation and wayfinding are
  disabled until a live route refreshes it.
- v1 enumerates stored `AEItemKey` item entries only. Craftables,
  autocrafting jobs, fluids, and other key types are separate future work and
  must not inflate stored item counts.
- AE2 storage buses are explicit aliases of their adjacent external inventory.
  SLOT subtracts known alias contents from ME counts so a bused chest/crate is
  not counted once as a chest and again through the ME network.

## Context

The first Forge AE2 integration treated nearby physical terminals as
live-only display storage. That solved near-terminal search and the crafting
terminal quick-move bug, but it made ME contents disappear from SLOT as soon
as no terminal was currently observed.

That behavior was wrong for real play. AE2 is persistent storage: the player
expects cells in drives and chests to participate in search, gather, put away,
wayfinding, and recipe pressure like any other storage. The hard part is that
the AE2 grid is not itself the durable object. A network can split, merge, or
move; the storage cells are the durable media.

Storage buses add another ambiguity. They make an external chest visible
through AE2 while the same chest may also be visible to SLOT directly. Counting
both surfaces as independent storage would overstate resources and could
mislead recipe/gather decisions.

## Rationale

Media-set identity follows what actually stores the items while still giving
SLOT one aggregate record for the player-facing ME network. Retiring
overlapping old records keeps split/merge/move behavior conservative: missing
some remembered items for a moment is recoverable, but double-counting
resources can drive incorrect automation and gather guidance.

The media set alone is not enough. A cell can be emptied into another cell or
through an IO port, producing a new network with no media-id overlap. SLOT
therefore stores a small AE2 media ledger keyed by the SLOT-owned media UUID.
Drive/ME-chest cells with item contents participate in the network id; empty
and non-item observations tombstone old item-count records for that media.

Terminal routes stay separate from storage identity because they are access
points. A physical terminal gives both mutation and wayfinding. An open or
wireless terminal can give mutation while the menu is open; it should refresh
the same media-set record, but it must not overwrite the last known physical
terminal route with a player position or placeholder coordinate.

Storage-bus aliases are modeled in the storage index instead of hidden inside
AE2 enumeration. That lets the direct chest record remain authoritative for
the external inventory while the ME record owns only the ME-only remainder.

## Consequences

Benefits:

- ME items remain searchable after observation, even when no terminal screen is
  currently open.
- Wayfinding points to a known physical terminal instead of an arbitrary grid
  machine or menu state.
- Storage-cell moves, network splits, and network merges do not leave multiple
  remembered copies of the same media.
- Cell-emptying machines and disjoint cell transfers do not leave stale item
  counts behind for the emptied cell.
- Empty spare cells do not churn the persistent network id.
- Bused chests are not double-counted when SLOT can identify the aliased
  storage.
- Craftables and stored items stay distinct for the later autocrafting plan.

Costs:

- SLOT mutates AE2 storage-cell NBT to add its media UUID when needed.
- AE2 remembered storage state carries provider metadata, media ids, aliases,
  per-media observations, and route information beyond ordinary chest counts.
- Wireless/open-terminal routes require special handling: live mutation can use
  the open menu, but persistent wayfinding requires a separately observed
  physical terminal route.
- If a storage-bus target is unknown and unreadable, SLOT leaves those counts in
  the ME record because there is no separate storage record to subtract.

## Non-Reversal Guidance

Do not return AE2 to terminal-position-only identity. Reconsider the media-set
model only if AE2 exposes stable public media identities or grid/storage APIs
that let SLOT distinguish moved cells, split networks, and merged networks more
reliably without writing SLOT-owned media UUIDs. Any replacement must preserve
the invariant that stored item counts exclude craftables and do not double-count
known storage-bus aliases.
