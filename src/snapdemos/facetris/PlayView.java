package snapdemos.facetris;
import org.jbox2d.dynamics.Body;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.view.ParentView;
import snap.view.View;
import snap.view.ViewTimer;
import snap.view.ViewUtils;
import java.util.Random;

/**
 * A View subclass to handle the real gameplay.
 */
public class PlayView extends ParentView {

    // The player
    private Player  _player = new Player();

    // The Faces
    private PlayField  _faces = new PlayField();

    // Constants
    private int BORDER_SIZE = 2;

    // The Timer
    private ViewTimer _newFaceTimer;

    // The Random
    private Random  _random = new Random();

    // The PhysicsRunner
    private PhysicsRunner _physRunner;

    /**
     * Creates PlayView.
     */
    public PlayView()
    {
        setFill(new Color("#F0F8FF"));
        setBorder(Color.BLACK, BORDER_SIZE);
        setBorder(getBorder().copyFor(Border.PaintAbove_Prop, true));
        _newFaceTimer = new ViewTimer(this::addFace, 3500);
        setClipToBounds(true);
    }

    /**
     * Returns the player.
     */
    public Player getPlayer()  { return _player; }

    /**
     * Returns the PlayField.
     */
    public PlayField getField()  { return _faces; }

    /**
     * Starts the play.
     */
    public void play()
    {
        // Remove faces/views
        _player.reset();
        _faces.reset();
        removeChildren();

        // Start timers
        _newFaceTimer.start(0);

        if (_physRunner != null)
            _physRunner.setRunning(false);

        ParentView worldView = this;
        _physRunner = new PhysicsRunner(worldView);
        _physRunner.setViewToWorldMeters(getHeight()/5);
        _physRunner.addWalls();
        _physRunner.addPhysForViews();
        _physRunner.setRunning(true);
    }

    /**
     * Stops the play.
     */
    public void stop()
    {
        _newFaceTimer.stop();

        ((FacetrisApp)getOwner()).gameOver();
    }

    /**
     * Stops the play.
     */
    public void stopNewFaces()
    {
        if (!_newFaceTimer.isRunning()) return;

        _newFaceTimer.stop();

        ViewUtils.runDelayed(() -> stop(), 2500);
    }

    /**
     * Adds a face.
     */
    protected void addFace()
    {
        // Get next face and view
        FaceEntry face = FaceIndex.getShared().getNextFaceFromQueue();
        _faces.addFieldFace(face);

        View view = face.getView();
        addChild(view);

        view.setRotate(10 - _random.nextInt(20));

        // Set location
        double pw = getWidth();
        double vw = view.getWidth();
        double vh = view.getHeight();
        int xRange = (int)(pw - vw);
        int x = 10 + _random.nextInt(xRange - 20);
        view.setXY(x, -vh);
        getOwner().resetLater();

        if (_physRunner != null)
            _physRunner.addPhysForView(view);
    }

    /**
     * Called to guess a face.
     */
    public void handleGuessFace(String aName)
    {
        // Get main face and name
        FaceEntry face = _faces.getMainFace();
        if (face == null)
            return;
        String name = face.getName().toLowerCase();
        String first = face.getFirstName().toLowerCase();

        // If mace matches
        String guessName = aName.toLowerCase();
        boolean match = guessName.length() > 0 && (name.startsWith(guessName) || first.startsWith(guessName));
        if (match)
            handleFaceWin(face);
        else handleFaceLose(face);
    }

    /**
     * Add win face.
     */
    void handleFaceWin(FaceEntry aFace)
    {
        _player.addWonFace(aFace);

        View view = aFace.getView();
        view.getAnim(500).setOpacity(0).setOnFinish(() -> handleFaceWinAnimDone(aFace)).play();
        getOwner().resetLater();
    }

    /**
     * Called when addWinFace anim is finished.
     */
    void handleFaceWinAnimDone(FaceEntry aFace)
    {
        _faces.removeFieldFace(aFace);

        View view = aFace.getView();
        removeChild(view);
        view.setOpacity(1);

        if (_physRunner != null)
            _physRunner.removePhysForView(view);
    }

    /**
     * Called to add a lose face.
     */
    void handleFaceLose(FaceEntry aFace)
    {
        _player.addLostFace(aFace);
        getOwner().resetLater();

        Body body = (Body) aFace.getView().getPhysics().getNative();
        body.setGravityScale(2.5f);

        if (_player.getLostFaces().size() >= 3)
            stopNewFaces();
    }

    /**
     * Add lost face.
     */
    void handleFaceCollide(FaceEntry aFace)
    {
        if (aFace.inPlay())
            handleFaceLose(aFace);

        _faces.removeFallFace(aFace);
    }
}
