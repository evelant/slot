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
    void printableCharactersOnlyAppendWhileSearchIsActive() {
        WorkspaceSearchInputPolicy.Decision inactive =
                WorkspaceSearchInputPolicy.charTyped(false, "", 'a', false);
        WorkspaceSearchInputPolicy.Decision active =
                WorkspaceSearchInputPolicy.charTyped(true, "ax", 'e', false);

        assertFalse(inactive.handled());
        assertEquals("axe", active.query());
    }

    @Test
    void digitsAreConsumedButNotAppendedWhileSearchIsActive() {
        WorkspaceSearchInputPolicy.Decision decision =
                WorkspaceSearchInputPolicy.charTyped(true, "axe", '3', false);

        assertTrue(decision.handled());
        assertTrue(decision.active());
        assertEquals("axe", decision.query());
        assertEquals(WorkspaceSearchInputPolicy.Action.IGNORE_DIGIT, decision.action());
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
