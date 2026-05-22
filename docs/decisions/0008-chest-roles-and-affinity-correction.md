# 0008: Chest Roles Gate Learned Storage Affinity

Status: accepted

Created: 2026-05-21

This record captures the decision to keep learned chest affinity, but make
each claimed chest's participation explicit through a small player-authored
role.

## Decision

- Every claimed chest has exactly one role: `Storage`, `Buffer`, or `Ignore`.
- The active-chest strip exposes one role button. Each click cycles
  `Storage -> Buffer -> Ignore -> Storage`; there is no automatic mode.
- A newly opened/unseen chest appears as `Ignore`. The existing first-deposit
  auto-claim path creates it as `Storage`, because depositing is the signal
  that the player wants that chest to be durable storage.
- `Storage` chests are visible, searchable, learn affinity, and accept quick
  or bulk deposit.
- `Buffer` chests are visible/searchable and can be pulled from, but never
  learn affinity and are never quick-deposit targets.
- `Ignore` chests are hidden from SLOT storage projection, affinity learning,
  and routing.
- Machines, vessels, and other non-storage hosts are excluded from affinity
  learning by the storage-affinity tags; if something still appears as a chest,
  the player can set it to `Ignore`.
- Reorganizing by moving the last stack of an identity out of one `Storage`
  chest and depositing it into another `Storage` chest clears the old
  `(chest, item)` affinity.
- Item context menus offer `Don't auto-deposit here` when the active chest has
  affinity for that item; the action clears only that `(chest, item)` bond.

## Context

Learned storage solved the old claim/link/area surface, but playtesting exposed
a subtler failure: deposits into machine feeders, hopper buffers, forge fuel
slots, crucibles, and other process-adjacent inventories trained the storage
router as if those slots were permanent homes. Affinity decay was previously
disabled because it forgot legitimate storage homes as well as bad ones, so it
was not a real fix.

Automatic feeder detection is attractive but brittle. The same block can be a
legitimate storage chest in one build and a machine buffer in another. The game
already gives the player local context while the chest is open, and the burden
of one role click is lower than recovering from an automatic misclassification.

The second problem is reorganization. If a player moves items from Chest A to
Chest B, the old affinity for Chest A must not silently pull those items back
on the next quick deposit. The low-friction signal is the move itself: if Chest
A no longer contains the identity after the take, and the next observed storage
deposit of that identity lands in another storage chest, the old bond was
probably superseded.

## Rationale

The role model keeps player control close to the thing being controlled. It is
manual enough to avoid false positives, but small enough that players do not
have to manage per-item rules in normal play.

`Buffer` is deliberately not a weaker form of `Storage`: it is a readable
source, not a destination. That matches feeder crates and staging chests, where
SLOT may help the player see or take contents without treating the block as a
home during quick deposit.

Targeted item forget handles the rare cases where only one item/chest bond is
wrong. The command lives on the item context menu while the chest is open, so
the label describes the visible situation instead of requiring an abstract
affinity editor.

## Consequences

Benefits:

- players can exclude feeder/buffer chests without standing far away from them
- accidental machine/vessel affinity is prevented at the eligibility layer and
  recoverable at the role layer
- quick deposit only targets places the player has allowed as durable storage
- storage reorganization usually fixes its own old affinity
- the correction path is per-item when needed, but absent from the normal flow

Costs:

- claimed chest state now carries a role and persistence has one more workflow
  event
- existing code that asks "is this claimed storage?" must choose between
  visible-to-workspace and quick-deposit-target semantics
- `Ignore` keeps a claim record so the active strip can cycle it back; it is
  hidden behaviorally rather than deleted

## Non-Reversal Guidance

Do not restore decay or automatic feeder detection as the primary answer to bad
affinity. Reconsider roles only if playtesting shows that the one-button cycle
is still too much friction in ordinary storage play. Any replacement must keep
the same invariant: SLOT never quick-deposits into a chest that the player is
using as process input unless the chest is currently open and the action is
explicit.
