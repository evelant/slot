package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitRackUiBuilderTest {
    @Test
    void clusterShowsActiveKitAndCyclesPages() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel view = viewModel(kit(true, 2, missingSlot("minecraft:stone")));

        SlotUiElement cluster = new KitRackUiBuilder(context).cluster(view, false);
        cluster.children().get(1).dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertTrue(cluster.hasAttachment(WorkspaceUiAttachments.KIT_CLUSTER));
        assertEquals("Mining 1/2", cluster.children().get(0).text());
        assertEquals(1, context.pageDirection);
    }

    @Test
    void rackSaveButtonAndCardActivationDispatchCommonActions() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel.KitCard inactive = kit(false, 1, missingSlot("minecraft:stone"));

        SlotUiElement rack = new KitRackUiBuilder(context).rack(viewModel(inactive));
        rack.children().get(0).children().get(1)
                .dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));
        SlotUiElement card = rack.children().get(1).children().get(0);
        card.dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertTrue(rack.hasAttachment(WorkspaceUiAttachments.KIT_RACK));
        assertEquals(1, context.saveCount);
        assertEquals(inactive.kitId(), context.activatedKitId);
        assertSame(inactive, card.attachment(WorkspaceUiAttachments.KIT_CARD, SlotWorkspaceViewModel.KitCard.class));
    }

    @Test
    void gatherButtonOnlyRequestsMissingIdentitiesInNearbyChests() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel.IdentityRef stone = identity("minecraft:stone");
        SlotWorkspaceViewModel.IdentityRef dirt = identity("minecraft:dirt");
        context.proximate.add(stone);
        SlotWorkspaceViewModel.KitCard card = kit(false, 1,
                slot(stone, false),
                slot(dirt, false));

        SlotUiElement rack = new KitRackUiBuilder(context).rack(viewModel(card));
        SlotUiElement cardElement = rack.children().get(1).children().get(0);
        SlotUiElement actions = cardElement.children().get(2);
        actions.children().get(1).dispatch(new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false));

        assertEquals(List.of(stone), context.taken);
        assertEquals("gathering 1 kit item", context.status);
    }

    @Test
    void rightClickFilledKitSlotClearsSlot() {
        RecordingContext context = new RecordingContext();
        SlotWorkspaceViewModel.KitCard card = kit(false, 1, missingSlot("minecraft:stone"));

        SlotUiElement rack = new KitRackUiBuilder(context).rack(viewModel(card));
        SlotUiElement cardElement = rack.children().get(1).children().get(0);
        SlotUiElement pageRow = cardElement.children().get(1);
        SlotUiElement slotCell = pageRow.children().get(1).children().get(0);
        slotCell.dispatch(new SlotUiEvent(SlotUiEventKind.MOUSE_DOWN, 1, 0, 0, false));

        assertEquals(card.kitId(), context.clearedKitId);
        assertEquals(0, context.clearedPageIndex);
        assertEquals(0, context.clearedSlotIndex);
    }

    private static SlotWorkspaceViewModel viewModel(SlotWorkspaceViewModel.KitCard... kits) {
        return new SlotWorkspaceViewModel(
                0,
                "ready",
                "",
                0,
                0,
                1,
                1,
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                SlotWorkspaceViewModel.emptyHotbar(),
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of(kits));
    }

    private static SlotWorkspaceViewModel.KitCard kit(
            boolean active,
            int pageCount,
            SlotWorkspaceViewModel.KitSlotState... slots
    ) {
        ArrayList<SlotWorkspaceViewModel.KitPageView> pages = new ArrayList<>();
        for (int page = 0; page < pageCount; page++) {
            List<SlotWorkspaceViewModel.KitSlotState> pageSlots = page == 0
                    ? List.of(slots)
                    : List.of();
            pages.add(new SlotWorkspaceViewModel.KitPageView(
                    page,
                    pageSlots.size(),
                    (int) pageSlots.stream().filter(SlotWorkspaceViewModel.KitSlotState::ready).count(),
                    pageSlots));
        }
        List<SlotWorkspaceViewModel.KitSlotState> activeSlots = pages.get(0).slots();
        return new SlotWorkspaceViewModel.KitCard(
                "kit-1",
                "Mining",
                pageCount,
                0,
                active,
                activeSlots.size(),
                (int) activeSlots.stream().filter(SlotWorkspaceViewModel.KitSlotState::ready).count(),
                9,
                36,
                0,
                0,
                activeSlots,
                pages,
                List.of());
    }

    private static SlotWorkspaceViewModel.KitSlotState missingSlot(String itemId) {
        return slot(identity(itemId), false);
    }

    private static SlotWorkspaceViewModel.KitSlotState slot(
            SlotWorkspaceViewModel.IdentityRef identity,
            boolean ready
    ) {
        return new SlotWorkspaceViewModel.KitSlotState(
                0,
                true,
                ready,
                identity,
                new ItemStack(identity.itemId(), 1, 64),
                identity.itemId());
    }

    private static SlotWorkspaceViewModel.IdentityRef identity(String itemId) {
        return SlotWorkspaceViewModel.IdentityRef.from(ItemIdentity.of(itemId));
    }

    private static final class RecordingContext implements KitRackUiBuilder.Context {
        final List<SlotWorkspaceViewModel.IdentityRef> proximate = new ArrayList<>();
        final List<SlotWorkspaceViewModel.IdentityRef> taken = new ArrayList<>();
        int pageDirection;
        int saveCount;
        String activatedKitId;
        String clearedKitId;
        int clearedPageIndex = -1;
        int clearedSlotIndex = -1;
        String status;

        @Override
        public void toggleKitRack() {
        }

        @Override
        public void closeKitRack() {
        }

        @Override
        public void saveCurrentBeltAsKit() {
            saveCount++;
        }

        @Override
        public void activateKit(String kitId) {
            activatedKitId = kitId;
        }

        @Override
        public void deactivateKit() {
        }

        @Override
        public void switchActiveKitPage(int direction) {
            pageDirection = direction;
        }

        @Override
        public void addKitPage(String kitId) {
        }

        @Override
        public void removeKitPage(String kitId, int pageIndex) {
        }

        @Override
        public void clearKitSlot(String kitId, int pageIndex, int slotIndex) {
            clearedKitId = kitId;
            clearedPageIndex = pageIndex;
            clearedSlotIndex = slotIndex;
        }

        @Override
        public void clearKitBring(String kitId, SlotWorkspaceViewModel.IdentityRef identity) {
        }

        @Override
        public void takeStackByIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
            taken.add(identity);
        }

        @Override
        public int proximateCount(SlotWorkspaceViewModel.IdentityRef identity) {
            return proximate.contains(identity) ? 1 : 0;
        }

        @Override
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
