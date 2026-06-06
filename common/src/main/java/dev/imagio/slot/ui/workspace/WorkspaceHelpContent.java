package dev.imagio.slot.ui.workspace;

import java.util.ArrayList;
import java.util.List;

public final class WorkspaceHelpContent {
    public static final int POPOVER_WIDTH_PX = 348;
    public static final int POPOVER_HEIGHT_PX = 244;
    public static final int HELP_LABEL_HEIGHT_PX = 9;
    public static final int HELP_DESCRIPTION_HEIGHT_PX = 8;
    public static final int HELP_ROW_PADDING_VERTICAL_PX = 2;
    public static final int HELP_ROW_GAP_PX = 1;

    private static final List<Line> GESTURES = List.of(
            new Line("Left-click",
                    "Pick up the hovered stack into SLOT's cursor.",
                    "Click another card to take more or swap the cursor."),
            new Line("Drop on section",
                    "With an item on the SLOT cursor, left-click a section.",
                    "That moves the item's home to that section."),
            new Line("Right-click held",
                    "While carrying with SLOT's cursor, right-click anywhere.",
                    "This returns/cancels the held item instead of opening a menu."),
            new Line("Right-click card",
                    "Opens item actions: junk, desired count, home moves,",
                    "workflow input rules, hotbar, storage, and trash actions."),
            new Line("Shift+right-click",
                    "Transfers against nearby storage for that item's home.",
                    "It pulls target gaps when needed, otherwise pushes carried stacks."),
            new Line("Shift+scroll",
                    "Moves a small amount without opening a menu.",
                    "Scroll up takes from nearby storage; down stores carried items."),
            new Line("Shift+left-click",
                    "In a machine/crafting/sidebar screen, inserts one carried item.",
                    "If the item is not carried, the action fails closed."),
            new Line("Ctrl+scroll",
                    "Raises or lowers the persistent desired count.",
                    "Desired means keep this stocked whenever possible."),
            new Line("Alt+scroll",
                    "Raises or lowers the temporary wanted count.",
                    "Wanted means I need this now; it clears when satisfied.")
    );

    private static final List<Line> KEYS = List.of(
            new Line("Backtick",
                    "Move hovered item to main inventory.",
                    "Use this to pull something out of hotbar/backpack lanes."),
            new Line("Shift+Backtick",
                    "Move hovered item toward backpack/provider storage.",
                    "The action routes through storage providers when available."),
            new Line("Tab",
                    "Put hovered item on a free or stale hotbar slot.",
                    "When closed, redo the latest quick hotbar swap."),
            new Line("Shift+Tab",
                    "When closed, undo the latest quick hotbar swap.",
                    "Restores the item displaced by Tab."),
            new Line("1-9",
                    "Assign hovered item to that exact hotbar slot.",
                    "If something is there, SLOT stages it out first."),
            new Line("W",
                    "Set hovered wanted count to a useful target.",
                    "In recipe view this uses the ingredient's required count."),
            new Line("X / Shift+X",
                    "X toggles nearby tracked storage ghosts.",
                    "Shift+X toggles all tracked storage ghosts.")
    );

    private static final List<Line> GHOSTS = List.of(
            new Line("Bright card",
                    "You are carrying this identity right now.",
                    "Bottom count is your carried total."),
            new Line("Dim card",
                    "A ghost is known or needed but not carried.",
                    "It can represent storage, intent, recipe, recent, or junk state."),
            new Line("Wanted/desired",
                    "If carried count is below a target, the card remains visible.",
                    "The bottom x/y badge shows carried versus target."),
            new Line("Craft helper",
                    "Tracked EMI recipes can create ghost cards for missing inputs.",
                    "They show what must be found, gathered, or crafted."),
            new Line("EMI view",
                    "When an EMI recipe is visible, SLOT can filter the wall.",
                    "The wall becomes a recipe-ingredient checklist for that recipe."),
            new Line("Recipe choices",
                    "Owned alternatives appear when SLOT can see them.",
                    "If no alternative is visible, SLOT shows a missing placeholder."),
            new Line("Nearby storage",
                    "X reveals ghosts for tracked storage close enough to use.",
                    "Nearby ghosts can usually be gathered or deposited against."),
            new Line("Tracked storage",
                    "Shift+X reveals ghosts from all tracked storage.",
                    "Remote ghosts are browse/wayfinding hints unless you are nearby."),
            new Line("Section +N",
                    "A section header can hide ordinary nearby storage ghosts.",
                    "Click the +N header chip to expand or collapse them."),
            new Line("Workflow input",
                    "Accepted workflow inputs can reveal nearby substitutes.",
                    "They are relevance hints, not permanent wanted targets."),
            new Line("Recent/junk",
                    "Recent and junk tags can keep non-carried cards visible.",
                    "That makes them easy to find, unmark, or re-home later.")
    );

    private static final List<Line> MARKERS = List.of(
            new Line("Bottom number",
                    "Plain number is carried count.",
                    "x/y means carried count versus target count."),
            new Line("Bottom blue",
                    "Desired target, including recipe-required targets.",
                    "Use Ctrl+scroll or the card menu to adjust desired."),
            new Line("Bottom purple",
                    "Wanted target: a short-term need-now request.",
                    "Use Alt+scroll or W to adjust wanted."),
            new Line("Bottom amber",
                    "Active workflow target/needed count.",
                    "The active workflow tab is asking for this item."),
            new Line("Top-right green",
                    "Nearby stored count in tracked/claimed storage.",
                    "This is storage close enough for gather/deposit actions."),
            new Line("Green corner",
                    "A deposit route is known, but there is no visible count.",
                    "Hover Deposit to preview routed cards."),
            new Line("Top-left blue",
                    "Stored elsewhere/tracked count.",
                    "Shown during search, x-ray, or tracked-storage reveal."),
            new Line("Green ring",
                    "Put-away/deposit route is known or being previewed.",
                    "This item can be routed to storage."),
            new Line("Blue ring",
                    "Put-away checked this item but found no learned route.",
                    "It will stay carried until a route is taught or chosen."),
            new Line("Amber/red ring",
                    "Target gap is available in storage, partly available, or missing.",
                    "Amber leans stored; red means craft/find it."),
            new Line("Orange corner",
                    "Junk marker.",
                    "Right-click the card to mark or unmark junk."),
            new Line("Side strip",
                    "Storage arrow points toward the best known source.",
                    "A need N strip means no known storage covers the target."),
            new Line("Section dot",
                    "Section count with a dot means carried cards are inside.",
                    "Filtering may show visible/total counts.")
    );

    private static final List<Line> STORAGE_ROLES = List.of(
            new Line("Storage",
                    "Visible and searchable; learns item homes.",
                    "Accepts quick store and bulk deposit."),
            new Line("Feeder/Buffer",
                    "Visible and searchable, but does not learn homes.",
                    "Never receives quick store or bulk deposit."),
            new Line("Ignore",
                    "Hidden from SLOT storage projection and routing.",
                    "Use for chests SLOT should leave alone.")
    );

    private static final List<Line> TERMS = List.of(
            new Line("Home",
                    "Where an item should live on the wall.",
                    "Move it by picking the item up and dropping it on a section."),
            new Line("Desired",
                    "Persistent target to keep stocked.",
                    "Desired survives the current task and drives Gather."),
            new Line("Wanted",
                    "Temporary need for this moment.",
                    "Wanted clears once your carried count satisfies it."),
            new Line("Workflow",
                    "Task tab with members, accepted inputs, and belt pages.",
                    "Active workflows can create needed targets and gather gaps."),
            new Line("Gather",
                    "Pull desired/wanted/workflow gaps from nearby storage.",
                    "Only known, reachable storage can satisfy the pull."),
            new Line("Deposit",
                    "Put carried clutter into learned storage routes.",
                    "Unrouted items stay carried instead of guessing."),
            new Line("Junk",
                    "Low-priority item marker.",
                    "Junk ghosts stay visible so they can be unmarked."),
            new Line("Crafting helper",
                    "EMI recipe staging and variant tracking.",
                    "It shows missing inputs and lets you choose alternatives.")
    );

    private WorkspaceHelpContent() {
    }

    public static List<Line> gestures() {
        return GESTURES;
    }

    public static List<Line> keys() {
        return KEYS;
    }

    public static List<Line> ghosts() {
        return GHOSTS;
    }

    public static List<Line> markers() {
        return MARKERS;
    }

    public static List<Line> storageRoles() {
        return STORAGE_ROLES;
    }

    public static List<Line> terms() {
        return TERMS;
    }

    private static List<String> descriptions(String... values) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>(values.length);
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                result.add(value);
            }
        }
        return List.copyOf(result);
    }

    public record Line(String key, List<String> descriptions) {
        public Line(String key, String... descriptions) {
            this(key, WorkspaceHelpContent.descriptions(descriptions));
        }

        public Line {
            key = key == null ? "" : key;
            descriptions = descriptions == null ? List.of() : List.copyOf(descriptions);
        }

        public int heightPx() {
            return HELP_ROW_PADDING_VERTICAL_PX * 2
                    + HELP_LABEL_HEIGHT_PX
                    + (descriptions.isEmpty() ? 0 : HELP_ROW_GAP_PX)
                    + descriptions.size() * HELP_DESCRIPTION_HEIGHT_PX;
        }
    }
}
