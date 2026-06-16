package dev.imagio.slot.ui.workspace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceSearchInputPolicyTest {
    @Test
    void slashOpensModalAndClearsQueryWhenTextInputIsNotFocused() {
        WorkspaceSearchInputPolicy.Decision decision =
                WorkspaceSearchInputPolicy.charTyped(false, "axe", '/', false);

        assertTrue(decision.handled());
        assertTrue(decision.active());
        assertEquals("", decision.query());
        assertEquals(WorkspaceSearchInputPolicy.Action.OPEN, decision.action());
    }

    @Test
    void printableCharactersAppendOnlyWhileSearchIsActive() {
        WorkspaceSearchInputPolicy.Decision inactiveEmpty =
                WorkspaceSearchInputPolicy.charTyped(false, "", 'a', false);
        WorkspaceSearchInputPolicy.Decision inactiveWithQuery =
                WorkspaceSearchInputPolicy.charTyped(false, "ax", 'w', false);
        WorkspaceSearchInputPolicy.Decision active =
                WorkspaceSearchInputPolicy.charTyped(true, "ax", 'e', false);

        assertFalse(inactiveEmpty.handled());
        assertFalse(inactiveWithQuery.handled());
        assertFalse(inactiveWithQuery.active());
        assertEquals("ax", inactiveWithQuery.query());
        assertEquals("axe", active.query());
    }

    @Test
    void digitsAreConsumedButNotAppendedOnlyWhileSearchIsActive() {
        WorkspaceSearchInputPolicy.Decision decision =
                WorkspaceSearchInputPolicy.charTyped(true, "axe", '3', false);
        WorkspaceSearchInputPolicy.Decision inactive =
                WorkspaceSearchInputPolicy.charTyped(false, "axe", '3', false);

        assertTrue(decision.handled());
        assertTrue(decision.active());
        assertEquals("axe", decision.query());
        assertEquals(WorkspaceSearchInputPolicy.Action.IGNORE_DIGIT, decision.action());
        assertFalse(inactive.handled());
        assertFalse(inactive.active());
        assertEquals("axe", inactive.query());
        assertEquals(WorkspaceSearchInputPolicy.Action.NONE, inactive.action());
    }

    @Test
    void visibleQueryDoesNotStealHostTextInputFocus() {
        WorkspaceSearchInputPolicy.Decision decision =
                WorkspaceSearchInputPolicy.charTyped(false, "axe", 'r', true);

        assertFalse(decision.handled());
        assertFalse(decision.active());
        assertEquals("axe", decision.query());
    }

    @Test
    void enterConfirmsAndBackslashClears() {
        WorkspaceSearchInputPolicy.Decision confirm = WorkspaceSearchInputPolicy.keyPressed(
                true,
                "axe",
                WorkspaceSearchInputPolicy.ControlKey.ENTER);
        WorkspaceSearchInputPolicy.Decision clear = WorkspaceSearchInputPolicy.keyPressed(
                true,
                "axe",
                WorkspaceSearchInputPolicy.ControlKey.CLEAR);

        assertFalse(confirm.active());
        assertEquals("axe", confirm.query());
        assertFalse(clear.active());
        assertEquals("", clear.query());
        assertEquals(WorkspaceSearchInputPolicy.Action.CLEAR, clear.action());
    }

    @Test
    void backslashClearsRememberedQueryEvenWhenModalIsInactive() {
        WorkspaceSearchInputPolicy.Decision clear = WorkspaceSearchInputPolicy.keyPressed(
                false,
                "axe",
                WorkspaceSearchInputPolicy.ControlKey.CLEAR);

        assertTrue(clear.handled());
        assertFalse(clear.active());
        assertEquals("", clear.query());
    }
}
