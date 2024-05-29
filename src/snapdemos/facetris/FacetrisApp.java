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
    private StartPane _startPane = new StartPane(this);

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
        title.setTextFill(Color.WHITE);
        title.setEffect(SHADOW);
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
            View view = face.getMiniView();
            familiarView.addChild(view, 0);
            double height = face.getImage().getHeight() / 2;
            view.setPrefHeight(0);
            view.getAnimCleared(500).setPrefHeight(height).setOnFinish(() -> view.setEffect(SHADOW)).play();
        }
    }

    /**
     * Respond UI.
     */
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
     * Called to start game.
     */
    protected void playGame()
    {
        _facetrisView.play();
        requestFocus("NameText");
    }

    private void showStartPane()
    {
        _startPane.show();
    }

    protected void gameOver()
    {
        showStartPane();
    }

    /**
     * Called to check a guess.
     */
    private void handleGuess(ViewEvent anEvent)
    {
        if (_startPane.isShowing()) {
            _startPane.handlePlayButton();
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
