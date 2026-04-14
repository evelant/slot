package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.collection.CollectionViewStateController;
import dev.imagio.slot.client.screen.debug.SlotDebugInventoryScreen;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.workflow.InspectionService;
import dev.imagio.slot.workflow.SearchWorkflowService;
import dev.imagio.slot.workflow.SettingsService;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Supplier;

public final class SlotCarriedInventoryScreen extends SlotDebugInventoryScreen {
    public SlotCarriedInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            Runnable openVanillaAction,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction
    ) {
        super(
                parentScreen,
                collectionStore,
                Component.translatable("slot.screen.inventory.title"),
                openVanillaAction,
                true,
                emiPresent,
                settingsController,
                collectionViewStateController,
                currentScreenToggleLabel,
                currentScreenToggleAction
        );
    }

    public SlotCarriedInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            Runnable openVanillaAction,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction,
            SearchWorkflowService searchWorkflow,
            InspectionService inspectionService
    ) {
        super(
                parentScreen,
                collectionStore,
                Component.translatable("slot.screen.inventory.title"),
                openVanillaAction,
                true,
                emiPresent,
                settingsController,
                collectionViewStateController,
                currentScreenToggleLabel,
                currentScreenToggleAction,
                null,
                searchWorkflow,
                inspectionService
        );
    }

    public SlotCarriedInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            Component title,
            Runnable openVanillaAction,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction,
            InventoryScreenContext screenContext
    ) {
        this(
                parentScreen,
                collectionStore,
                title,
                openVanillaAction,
                emiPresent,
                settingsController,
                collectionViewStateController,
                currentScreenToggleLabel,
                currentScreenToggleAction,
                screenContext,
                null,
                null
        );
    }

    public SlotCarriedInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            Component title,
            Runnable openVanillaAction,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction,
            InventoryScreenContext screenContext,
            SearchWorkflowService searchWorkflow,
            InspectionService inspectionService
    ) {
        super(
                parentScreen,
                collectionStore,
                title,
                openVanillaAction,
                true,
                emiPresent,
                settingsController,
                collectionViewStateController,
                currentScreenToggleLabel,
                currentScreenToggleAction,
                screenContext,
                searchWorkflow,
                inspectionService
        );
    }

    public SlotCarriedInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            Runnable openVanillaAction,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction,
            InventoryHostDescriptor host
    ) {
        this(
                parentScreen,
                collectionStore,
                openVanillaAction,
                emiPresent,
                settingsController,
                collectionViewStateController,
                currentScreenToggleLabel,
                currentScreenToggleAction,
                host,
                null,
                null
        );
    }

    public SlotCarriedInventoryScreen(
            Screen parentScreen,
            CollectionStore collectionStore,
            Runnable openVanillaAction,
            boolean emiPresent,
            SettingsService settingsController,
            CollectionViewStateController collectionViewStateController,
            Supplier<Component> currentScreenToggleLabel,
            Runnable currentScreenToggleAction,
            InventoryHostDescriptor host,
            SearchWorkflowService searchWorkflow,
            InspectionService inspectionService
    ) {
        this(
                parentScreen,
                collectionStore,
                Objects.requireNonNull(host, "host").title(),
                openVanillaAction,
                emiPresent,
                settingsController,
                collectionViewStateController,
                        currentScreenToggleLabel,
                        currentScreenToggleAction,
                        InventoryScreenContext.carriedOnly(
                        host.title(),
                        host.menu(),
                        host.layout()
                ),
                searchWorkflow,
                inspectionService
        );
    }
}
