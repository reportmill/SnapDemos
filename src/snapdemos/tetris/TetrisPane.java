package snapdemos.tetris;
import snap.util.SnapEnv;
import snap.view.*;

/**
 * The main UI controller for the game.
 */
public class TetrisPane extends ViewOwner {
    
    // The PlayView
    private PlayView _playView;
    
    // The next block box
    private BoxView _nextBlockBox;

    /**
     * Constructor.
     */
    public TetrisPane()
    {
        super();
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Create PlayView and add to pane
        _playView = new PlayView();
        BoxView playViewBox = getView("PlayViewBox", BoxView.class);
        playViewBox.setContent(_playView);

        // Get/configure NextBlockBox
        _nextBlockBox = getView("NextBlockBox", BoxView.class);
        _nextBlockBox.setScale(.6);

        // Add PlayView listener to Score, NextBlock changes
        _playView.addPropChangeListener(pc -> resetLater(), PlayView.Score_Prop);
        _playView.addPropChangeListener(pc -> handlePlayViewNextBlockChange(), PlayView.NextBlock_Prop);
    }

    /**
     * Initialize showing.
     */
    @Override
    protected void initShowing()  { runLater(_playView::startGame); }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        setViewValue("ScoreLabel", _playView.getScore());
    }

    /**
     * Respond to UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle LeftButton, RightButton, DropButton, RotateButton
        if(anEvent.equals("LeftButton")) _playView.moveLeft();
        if(anEvent.equals("RightButton")) _playView.moveRight();
        if(anEvent.equals("DropButton")) _playView.dropBlock();
        if(anEvent.equals("RotateButton")) _playView.rotateBlock();

        // Handle PauseButton, RestartButton
        if(anEvent.equals("PauseButton")) _playView.pauseGame();
        if(anEvent.equals("RestartButton")) _playView.startGame();
    }

    /**
     * Called when PlayView.NextBlock changes.
     */
    private void handlePlayViewNextBlockChange()
    {
        Block nextBlock = _playView.getNextBlock(false);
        Block nextBlockCopy = nextBlock.getCopy();
        _nextBlockBox.setContent(nextBlockCopy);
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)  { ViewUtils.runLater(TetrisPane::appThreadMain); }

    /**
     * Standard main method.
     */
    private static void appThreadMain()
    {
        TetrisPane tetrisPane = new TetrisPane();
        tetrisPane.getWindow().setMaximized(SnapEnv.isWebVM);
        tetrisPane.setWindowVisible(true);
    }
}