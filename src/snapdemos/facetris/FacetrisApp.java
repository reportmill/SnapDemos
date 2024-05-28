package snapdemos.facetris;
import snap.gfx.Color;
import snap.gfx.Effect;
import snap.gfx.Image;
import snap.gfx.ShadowEffect;
import snap.util.SnapUtils;
import snap.view.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main UI controller for Facetris.
 */
public class FacetrisApp extends ViewOwner {

    // The PlayView
    private PlayView  _playView;

    // The TextField
    private TextField  _nameText;

    // StartPane
    private StartPane _startPane = new StartPane(this);

    // Whether to cheat
    public static boolean  _cheat;

    // Constant for title shadow
    private Effect SHADOW = new ShadowEffect();

    /**
     * Returns the player.
     */
    public Player getPlayer()  { return _playView.getPlayer(); }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        if (SnapUtils.isWebVM)
            return new ScaleBox(super.createUI(), true, true);
        return super.createUI();
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get PlayView
        _playView = getView("PlayView", PlayView.class);

        // Get NameText
        _nameText = getView("NameText", TextField.class);
        _nameText.addEventFilter(e -> ViewUtils.runLater(() -> textFieldKeyTyped(e)), KeyType);
        setFirstFocus(_nameText);

        // Configure TitleLabel
        Label title = getView("TitleLabel", Label.class);
        title.setTextFill(Color.WHITE);
        title.setEffect(SHADOW);
    }

    @Override
    protected void resetUI()
    {
        // Reset UI
        setViewValue("FamFacesLabel", "Familiar Faces: " + getPlayer().getWonFaces().size());

        // Reset image views
        resetComingSoonView();
        resetFamiliarFacesView();
    }

    /**
     * Reset ComingSoonView.
     */
    void resetComingSoonView()
    {
        // Update coming faces
        ColView comingSoonView = getView("ComingSoonView", ColView.class);
        FaceEntry[] comingFaces = FaceIndex.getShared().getNextQueue().toArray(new FaceEntry[0]);
        if (comingFaces.length == 0) {
            comingSoonView.removeChildren();
            return;
        }

        // Remove uses faces
        List<View> children = new ArrayList<>(Arrays.asList(comingSoonView.getChildren()));
        while (children.size() > 0 && comingFaces[0].getMiniView() != children.get(0)) {
            View view = children.remove(0);
            view.setEffect(null);
            view.getAnim(500).setPrefHeight(0).setOnFinish(() -> comingSoonView.removeChild(view)).play();
        }

        // Add new faces
        for (int i = children.size(); i < comingFaces.length; i++) {
            FaceEntry face = comingFaces[i];
            comingSoonView.addChild(face.getMiniView());
        }
    }

    /**
     * Reset FamiliarFacesView.
     */
    void resetFamiliarFacesView()
    {
        // Update coming faces
        ColView familiarView = getView("FamiliarFacesView", ColView.class);
        FaceEntry[] familiarFaces = getPlayer().getWonFaces().toArray(new FaceEntry[0]);
        if (familiarFaces.length == 0) {
            familiarView.removeChildren();
            return;
        }

        // Add Familiar faces
        while (familiarView.getChildCount() < familiarFaces.length) {
            FaceEntry face = familiarFaces[familiarView.getChildCount()];
            View view = face.getMiniView();
            familiarView.addChild(view, 0);
            double height = face.getImage().getHeight() / 2;
            view.setPrefHeight(0);
            view.getAnimCleared(500).setPrefHeight(height).setOnFinish(() -> view.setEffect(SHADOW)).play();
        }
    }

    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle PlayButton
        if (anEvent.equals("PlayButton"))
            playGame();

        // Handle NameText
        if (anEvent.equals("NameText"))
            handleGuess(anEvent);

        // Handle CheatCheckBox
        if (anEvent.equals("CheatCheckBox")) {
            _cheat = anEvent.getBoolValue();
            FaceIndex.getShared().getNextQueue().forEach(i -> i.getView().setShowName(_cheat));
            playGame();
        }
    }

    /**
     * Called when UI is showing.
     */
    @Override
    protected void initShowing()
    {
        FaceEntry face = FaceIndex.getShared().getNextQueue().peek();
        Image img = face.getImage();
        if (img.isLoaded())
            runLater(() -> showStartPane());
        else img.addLoadListener(() -> runLater(() -> showStartPane()));
    }

    /**
     * Called to start game.
     */
    void playGame()
    {
        _playView.play();
        requestFocus("NameText");
    }

    void showStartPane()
    {
        _startPane.show();
    }

    void gameOver()
    {
        showStartPane();
    }

    /**
     * Called to check a guess.
     */
    void handleGuess(ViewEvent anEvent)
    {
        if (_startPane.isShowing()) {
            _startPane.handlePlayButton();
            return;
        }

        _playView.handleGuessFace(anEvent.getStringValue());
        _nameText.setText("");
    }

    /**
     * Called after TextField has KeyType.
     */
    protected void textFieldKeyTyped(ViewEvent anEvent)
    {
        // Get prefix text and current selection
        String text = _nameText.getText();
        int selStart = _nameText.getSelStart();
        if (text==null || text.length()==0)
            return;

        // Look for possible completion
        String item = null;
        FaceEntry face = _playView.getField().getMainFace();
        if (face!=null && face.getName().toLowerCase().startsWith(text.toLowerCase()))
            item = face.getName();
        else if (face!=null && face.getFirstName().toLowerCase().startsWith(text.toLowerCase()))
            item = face.getFirstName();
        else item = FaceIndex.getShared().getNameForPrefix(selStart>0? text : "");

        // If completion available, set completion text
        if (item!=null)
            _nameText.setCompletionText(item);
    }

    /**
     * Standard Main implementation.
     */
    public static void main(String[] args)
    {
        ViewUtils.runLater(FacetrisApp::mainLater);
    }

    /**
     * Standard Main implementation.
     */
    private static void mainLater()
    {
        FacetrisApp facetrisApp = new FacetrisApp();
        facetrisApp.getWindow().setMaximized(SnapUtils.isWebVM);
        facetrisApp.setWindowVisible(true);
    }
}
