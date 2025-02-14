package snapdemos.facetris;
import snap.gfx.Color;
import snap.gfx.Effect;
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
    protected FacetrisView _facetrisView;

    // The TextField
    private TextField  _nameText;

    // StartPane
    private ParentView _startPane;

    // Whether to cheat
    public static boolean  _cheat;

    // Constant for title shadow
    private Effect SHADOW = new ShadowEffect();

    /**
     * Constructor.
     */
    public FacetrisApp()
    {
        super();
    }

    /**
     * Called to start game.
     */
    protected void playGame()
    {
        _facetrisView.play();
        requestFocus("NameText");
    }

    /**
     * Called to end game.
     */
    protected void gameOver()
    {
        showStartPane();
    }

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
        _facetrisView = getView("PlayView", FacetrisView.class);

        // Get NameText
        _nameText = getView("NameText", TextField.class);
        _nameText.addEventFilter(e -> ViewUtils.runLater(() -> textFieldKeyTyped(e)), KeyType);
        setFirstFocus(_nameText);

        // Configure TitleLabel
        Label title = getView("TitleLabel", Label.class);
        title.setTextColor(Color.WHITE);
        title.setEffect(SHADOW);

        // Get StartPane and remove from UI
        _startPane = getView("StartPane", ParentView.class);
        ViewUtils.removeChild(getUI(ParentView.class), _startPane);
    }

    /**
     * Called when UI is showing.
     */
    @Override
    protected void initShowing()
    {
        runLater(this::showStartPane);
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Reset UI
        setViewValue("FamFacesLabel", "Familiar Faces: " + _facetrisView.getWonFaces().size());

        // Reset image views
        resetComingSoonView();
        resetFamiliarFacesView();
    }

    /**
     * Reset ComingSoonView.
     */
    private void resetComingSoonView()
    {
        // Update coming faces
        ColView comingSoonView = getView("ComingSoonView", ColView.class);
        FaceEntry[] comingFaces = FaceIndex.getShared().getNextQueue().toArray(new FaceEntry[0]);
        if (comingFaces.length == 0) {
            comingSoonView.removeChildren();
            return;
        }

        // Remove uses faces
        List<View> children = new ArrayList<>(comingSoonView.getChildren());
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
    private void resetFamiliarFacesView()
    {
        // Update coming faces
        ColView familiarView = getView("FamiliarFacesView", ColView.class);
        FaceEntry[] familiarFaces = _facetrisView.getWonFaces().toArray(new FaceEntry[0]);
        if (familiarFaces.length == 0) {
            familiarView.removeChildren();
            return;
        }

        // Add Familiar faces
        while (familiarView.getChildCount() < familiarFaces.length) {
            FaceEntry face = familiarFaces[familiarView.getChildCount()];
            View faceView = face.getMiniView();
            familiarView.addChild(faceView, 0);
            faceView.setPrefHeight(-1);
            double faceH = faceView.getBestHeight(-1);
            faceView.setPrefHeight(0);
            faceView.getAnimCleared(500).setPrefHeight(faceH).setOnFinish(() -> faceView.setEffect(SHADOW)).play();
        }
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        switch (anEvent.getName()) {

            // Handle PlayButton
            case "PlayButton": playGame(); break;

            // Handle NameText
            case "NameText": handleGuess(anEvent); break;

            // Handle CheatCheckBox
            case "CheatCheckBox":
                _cheat = anEvent.getBoolValue();
                FaceIndex.getShared().getNextQueue().forEach(i -> i.getView().setShowName(_cheat));
                playGame();
                break;
        }
    }

    /**
     * Called to check a guess.
     */
    private void handleGuess(ViewEvent anEvent)
    {
        if (_startPane.isShowing()) {
            hideStartPane();
            return;
        }

        _facetrisView.handleGuessFace(anEvent.getStringValue());
        _nameText.setText("");
    }

    /**
     * Called after TextField has KeyType.
     */
    protected void textFieldKeyTyped(ViewEvent anEvent)
    {
        // Get prefix text and current selection
        String typedChars = _nameText.getText();
        int selStart = _nameText.getSelStart();
        if (typedChars == null || typedChars.length() == 0)
            return;

        // Look for possible completion
        String item = getCompletionForTypedChars(typedChars.toLowerCase(), selStart);

        // If completion available, set completion text
        if (item != null)
            _nameText.setCompletionText(item);
    }

    /**
     * Returns a possible completion for given typed chars.
     */
    private String getCompletionForTypedChars(String typedChars, int selStart)
    {
        FaceEntry faceEntry = _facetrisView.getMainFace();
        if (faceEntry != null && faceEntry.getName().toLowerCase().startsWith(typedChars))
            return faceEntry.getName();

        if (faceEntry != null && faceEntry.getFirstName().toLowerCase().startsWith(typedChars))
            return faceEntry.getFirstName();

        return FaceIndex.getShared().getNameForPrefix(selStart > 0 ? typedChars : "");
    }

    /**
     * Shows the start pane.
     */
    private void showStartPane()
    {
        // If first time, do some init
        boolean firstTime = _startPane.getOpacity() == 1;
        if (firstTime) {
            _startPane.setFill(ViewUtils.getBackFill());
            _startPane.setManaged(false);
            _startPane.setSizeToPrefSize();
            Button playButton = (Button) _startPane.getChildForName("PlayButton");
            playButton.setDefaultButton(true);
            playButton.addEventHandler(e -> hideStartPane(), Action);
        }

        // Otherwise do some updating
        else {
            _startPane.setOpacity(1);
            Label topLabel = (Label) _startPane.getChildForName("TopLabel");
            topLabel.setText("Game Over");
            int wonFacesCount = _facetrisView.getWonFaces().size();
            Label label2 = (Label) _startPane.getChildForName("Label2");
            label2.setText("You recognized " + wonFacesCount + " faces.");
            Label label3 = (Label) _startPane.getChildForName("Label3");
            label3.setText("");
        }

        // More config
        _startPane.setEffect(new ShadowEffect(20, Color.BLACK, 0, 0));

        // Add StartPane to main view and animate in
        ParentView topView = getUI(ParentView.class);
        if (topView instanceof ScaleBox)
            topView = (ParentView) ((ScaleBox) topView).getContent();
        ViewUtils.addChild(topView, _startPane);
        _startPane.setXY(200, -_startPane.getHeight());
        _startPane.getAnimCleared(1000).setY(200).play();
    }

    /**
     * Hides the start pane.
     */
    private void hideStartPane()
    {
        _startPane.setEffect(null);
        Runnable removeStartPaneRun = () -> ViewUtils.removeChild(_startPane.getParent(), _startPane);
        _startPane.getAnimCleared(500).setOpacity(0).setOnFinish(removeStartPaneRun).play();
        playGame();
    }

    /**
     * Standard Main implementation.
     */
    public static void main(String[] args)
    {
        ViewUtils.runLater(FacetrisApp::showFacetrisApp);
    }

    /**
     * Shows the Facetris app.
     */
    private static void showFacetrisApp()
    {
        FacetrisApp facetrisApp = new FacetrisApp();
        facetrisApp.getWindow().setMaximized(SnapUtils.isWebVM);
        facetrisApp.setWindowVisible(true);
    }
}
