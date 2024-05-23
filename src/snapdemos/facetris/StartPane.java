package snapdemos.facetris;
import snap.gfx.Color;
import snap.gfx.ShadowEffect;
import snap.view.*;

/**
 * A panel to start game.
 */
public class StartPane extends ViewOwner {

    // Facetris
    private Facetris  _facetris;

    /**
     * Constructor.
     */
    public StartPane(Facetris aFactris)
    {
        super();
        _facetris = aFactris;
    }

    /**
     * Show pane.
     */
    public void show()
    {
        boolean firstTime = !isUISet();
        View ui = getUI();
        ui.setFill(ViewUtils.getBackFill());
        ui.setManaged(false);
        ui.setEffect(new ShadowEffect(20, Color.BLACK, 0, 0));
        ui.setSize(ui.getPrefSize());
        if (!firstTime) {
            ui.setOpacity(1);
            setViewValue("TopLabel", "Game Over");
            int count = _facetris.getPlayer().getWonFaces().size();
            setViewValue("Label2", "You recongized " + count + " faces.");
            setViewValue("Label3", "");
        }

        ParentView view = _facetris.getUI(ParentView.class);
        ViewUtils.addChild(view, ui);
        ui.setX(200);
        ui.setY(-ui.getHeight());
        ui.getAnimCleared(1000).setY(200).play();
    }

    @Override
    protected void initUI()
    {
        Button playButton = getView("PlayButton", Button.class);
        playButton.setDefaultButton(true);
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        if (anEvent.equals("PlayButton"))
            handlePlayButton();
    }

    public void handlePlayButton()
    {
        getUI().setEffect(null);
        getUI().getAnimCleared(500).setOpacity(0).setOnFinish(() -> playButtonAnimDone()).play();
        _facetris.playGame();
    }

    void playButtonAnimDone()
    {
        View ui = getUI();
        ViewUtils.removeChild(ui.getParent(), ui);
    }
}
