package snapdemos.puzzle;

import snap.view.ViewTheme;

/**
 * A puzzle app.
 */
public class PuzzleApp {

    /**
     * Standard main implementation.
     */
    public static void main(String[] args)
    {
        ViewTheme.setThemeForName("Light");
        PuzzlePane puzzlePane = new PuzzlePane();
        puzzlePane.setWindowVisible(true);
    }
}
