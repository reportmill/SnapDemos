package snapdemos.puzzle;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.GradientPaint;
import snap.gfx.Image;
import snap.gfx.ShadowEffect;
import snap.util.SnapEnv;
import snap.view.*;

/**
 * This class holds the PuzzleView.
 */
public class PuzzleApp extends ViewOwner {

    // The Puzzle View
    private PuzzleView  _puzzleView;

    // The Score
    private int _score;

    // The InfoView
    private View _infoView;

    /**
     * Constructor.
     */
    public PuzzleApp()
    {
        super();
    }

    /**
     * Returns the score.
     */
    public int getScore()  { return _score; }

    /**
     * Sets the score.
     */
    public void setScore(int aValue)
    {
        _score = aValue;
        resetLater();
    }

    /**
     * Override to put in ScaleBox for web.
     */
    @Override
    protected View createUI()
    {
        View mainView = super.createUI();
        if (SnapEnv.isWebVM)
            mainView = new ScaleBox(mainView, true, true);
        return mainView;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Create/config PuzzleView
        _puzzleView = new PuzzleView(this);
        _puzzleView.setGrowHeight(true);
        _puzzleView.setMargin(4, 50, 20, 50);

        // Add to PuzzleViewBox
        BoxView puzzleViewBox = getView("PuzzleViewBox", BoxView.class);
        puzzleViewBox.setContent(_puzzleView);

        // Set Gradient
        GradientPaint gradientPaint = new GradientPaint.Builder().angle(80)
            //.stop(0, new Color("#D8D8E0"))
            .stop(0, new Color("#E0E8F0"))
            .stop(1, new Color("#D8D8E0")).build();
        getUI().setFill(gradientPaint);

        // Watch
        getUI().addEventFilter(e -> { if (_infoView != null) hideInfoPanel(); }, ViewEvent.Type.MousePress);

        // If TeaVM, maximize window
        if (SnapEnv.isTeaVM) {
            getWindow().setMaximized(true);
            _puzzleView.setMargin(4, 100, 20, 100);
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

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update ScoreText
        setViewValue("ScoreText", getScore());
    }

    /**
     * Respond to UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle InfoButton
        if (anEvent.equals("InfoButton"))
            showInfoPanel();

        // Handle RestartButton, NewPuzzleButton
        if (anEvent.equals("RestartButton") || anEvent.equals("NewPuzzleButton"))
            _puzzleView.resetPuzzle();
    }

    /**
     * Shows the InfoPanel.
     */
    private void showInfoPanel()
    {
        if (_infoView != null) {
            hideInfoPanel();
            return;
        }

        Image infoImage = Image.getImageForClassResource(getClass(), "InfoGraphic.png");
        _infoView = new ImageView(infoImage);
        _infoView.setFill(Color.WHITE);
        _infoView.setBorder(Color.BLACK, 1);
        _infoView.setBorderRadius(5);
        _infoView.setEffect(new ShadowEffect());
        _infoView.setLean(Pos.TOP_CENTER);
        _infoView.setManaged(false);
        _infoView.setClipToBounds(true);
        _infoView.setMargin(70, 0, 0, 0);
        _infoView.setSize(302, 252);
        ViewUtils.addChild((ParentView) getUI(), _infoView);

        // Animate down
        _infoView.setTransY(-320);
        ViewAnim viewAnim = _infoView.getAnim(1000);
        viewAnim.setTransY(0);
        viewAnim.play();
    }

    /**
     * Hides the InfoPanel.
     */
    private void hideInfoPanel()
    {
        if (_infoView == null) return;
        ViewAnim viewAnim = _infoView.getAnimCleared(1000);
        viewAnim.setTransY(-350).setLinear();
        viewAnim.play();
        viewAnim.setOnFinish(() -> {
            ViewUtils.removeChild((ParentView) getUI(), _infoView);
            _infoView = null;
        });
    }

    /**
     * Standard main implementation.
     */
    public static void showPuzzleApp()
    {
        PuzzleApp puzzleApp = new PuzzleApp();
        puzzleApp.getWindow().setMaximized(true);
        puzzleApp.setWindowVisible(true);
    }

    /**
     * Standard main implementation.
     */
    public static void main(String[] args)
    {
        ViewUtils.runLater(PuzzleApp:: showPuzzleApp);
    }
}
