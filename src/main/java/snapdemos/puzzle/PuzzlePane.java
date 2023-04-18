package snapdemos.puzzle;
import snap.util.SnapUtils;
import snap.view.ColView;
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
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get main ColView
        ColView mainColView = (ColView) getUI();

        // Create/config/add PuzzleView
        _puzzleView = new PuzzleView();
        _puzzleView.setGrowHeight(true);
        _puzzleView.setMargin(10, 50, 20, 50);
        mainColView.addChild(_puzzleView, mainColView.getChildCount() - 1);

        // If TeaVM, maximize window
        if (SnapUtils.isTeaVM) {
            getWindow().setMaximized(true);
            _puzzleView.setMargin(10, 100, 20, 100);
        }
    }

    /**
     * Override to repaint when images loaded.
     */
    @Override
    protected void initShowing()
    {
        if (!PuzzlePieceView.DOWN_ARROW.isLoaded())
            PuzzlePieceView.DOWN_ARROW.addLoadListener(() -> getUI().repaint());
        if (!PuzzlePieceView.RIGHT_ARROW.isLoaded())
            PuzzlePieceView.RIGHT_ARROW.addLoadListener(() -> getUI().repaint());
    }
}
