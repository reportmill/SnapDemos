package snapdemos.jewels;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * The class that manages top level UI for app.
 */
public class JewelsApp extends ViewOwner {

    // The PlayView
    private PlayView _playView;

    /**
     * Constructor.
     */
    public JewelsApp()
    {
        super();

        getWindow().setMaximized(SnapUtils.isWebVM);
        setWindowVisible(true);
        runLater(_playView::startGame);
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Do normal version
        RowView mainRowView = (RowView) super.createUI();

        // Swap out placeholder with PlayView
        _playView = new PlayView();
        ViewUtils.replaceView(mainRowView.getChild(1), _playView);

        // Wrap in ScaleBox and return
        return new ScaleBox(mainRowView, true, true);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle PauseButton, RestartButton
        if(anEvent.equals("PauseButton"))
            _playView.pauseGame();
        if(anEvent.equals("RestartButton"))
            _playView.startGame();
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)
    {
        ViewUtils.runLater(() -> new JewelsApp());
    }
}