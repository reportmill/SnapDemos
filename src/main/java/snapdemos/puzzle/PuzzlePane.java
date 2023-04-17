package snapdemos.puzzle;
import snap.util.SnapUtils;
import snap.view.View;
import snap.view.ViewOwner;

/**
 * This class holds the PuzzleView.
 */
public class PuzzlePane extends ViewOwner {

    // The Puzzle View
    private PuzzleView  _puzzleView;

    /**
     * Constructor.
     */
    public PuzzlePane()
    {
        super();
    }

    /**
     * Creates UI.
     */
    @Override
    protected View createUI()
    {
        _puzzleView = new PuzzleView();
        return _puzzleView;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Configure PuzzleView
        _puzzleView.setMargin(50, 50, 50, 50);

        // If TeaVM, maximize window
        if (SnapUtils.isTeaVM) {
            getWindow().setMaximized(true);
            _puzzleView.setMargin(100, 100, 100, 100);
        }
    }
}
