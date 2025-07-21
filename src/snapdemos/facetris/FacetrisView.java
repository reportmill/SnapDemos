package snapdemos.facetris;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.contacts.Contact;
import snap.gfx.Color;
import snap.util.ListUtils;
import snap.view.ParentView;
import snap.view.View;
import snap.view.ViewTimer;
import snap.view.ViewUtils;
import snapdemos.jbox2d.JBoxWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A View subclass to handle the real gameplay.
 */
public class FacetrisView extends ParentView {

    // The Faces on the field
    private List<FaceEntry> _fieldFaces = new ArrayList<>();

    // The Won faces
    private List<FaceEntry> _wonFaces = new ArrayList<>();

    // The Lost Faces
    private List<FaceEntry> _lostFaces = new ArrayList<>();

    // The Timer
    private ViewTimer _newFaceTimer;

    // The JBox2D World
    private JBoxWorld _jboxWorld;

    // Constants
    private static final int BORDER_SIZE = 2;

    // The Random
    private static final Random  _random = new Random();

    /**
     * Creates PlayView.
     */
    public FacetrisView()
    {
        super();
        setFill(new Color("#F0F8FF"));
        setBorder(Color.BLACK, BORDER_SIZE);
        setClipToBounds(true);

        // Create timer
        _newFaceTimer = new ViewTimer(this::addFace, 3500);
    }

    /**
     * Returns the first face still in play.
     */
    public FaceEntry getMainFace()
    {
        return ListUtils.findMatch(_fieldFaces, face -> face.inPlay());
    }

    /**
     * Adds a face.
     */
    protected void addFieldFace(FaceEntry aFace)
    {
        _fieldFaces.add(aFace);
    }

    /**
     * Removes a face.
     */
    protected void removeFieldFace(FaceEntry aFace)
    {
        _fieldFaces.remove(aFace);
    }

    /**
     * Returns the faces we've won.
     */
    public List<FaceEntry> getWonFaces()  { return _wonFaces; }

    /**
     * Add familiar face.
     */
    public void addWonFace(FaceEntry aFace)
    {
        _wonFaces.add(aFace);
        aFace.setStatus(FaceEntry.Status.Won);
    }

    /**
     * Returns the faces we've lost.
     */
    public List<FaceEntry> getLostFaces()  { return _lostFaces; }

    /**
     * Add lost face.
     */
    public void addLostFace(FaceEntry aFace)
    {
        _lostFaces.add(aFace);
        aFace.setStatus(FaceEntry.Status.Lost);
    }

    /**
     * Starts the play.
     */
    public void play()
    {
        // Remove faces/views
        _fieldFaces.clear();
        _lostFaces.clear();
        _wonFaces.clear();
        removeChildren();
        if (_jboxWorld != null)
            _jboxWorld.setRunning(false);

        // Start timers
        _newFaceTimer.start(0);

        // Create and configure JBoxWorld for this view
        _jboxWorld = new JBoxWorld(this);
        _jboxWorld.setPixelsToMeters(getHeight() / 5);
        _jboxWorld.addWallsToWorldView();
        _jboxWorld.addPhysicsForWorldViewChildren();
        _jboxWorld.setContactListener(new ViewContactListener());
        _jboxWorld.setRunning(true);
    }

    /**
     * Stops the play.
     */
    public void stop()
    {
        _newFaceTimer.stop();

        FacetrisApp facetrisApp = getOwner(FacetrisApp.class);
        facetrisApp.gameOver();
    }

    /**
     * Stops the play.
     */
    public void stopNewFaces()
    {
        if (!_newFaceTimer.isRunning()) return;

        _newFaceTimer.stop();

        ViewUtils.runDelayed(this::stop, 2500);
    }

    /**
     * Adds a face.
     */
    protected void addFace()
    {
        // Get next face and view
        FaceEntry face = FaceIndex.getShared().getNextFaceFromQueue();
        addFieldFace(face);

        View faceView = face.getView();
        addChild(faceView);

        faceView.setRotate(10 - _random.nextInt(20));

        // Set location
        double viewW = getWidth();
        double faceW = faceView.getWidth();
        double faceH = faceView.getHeight();
        int xRange = (int) (viewW - faceW);
        int faceX = 10 + _random.nextInt(xRange - 20);
        faceView.setXY(faceX, -faceH);
        getOwner().resetLater();

        if (_jboxWorld != null)
            _jboxWorld.addPhysicsForView(faceView);
    }

    /**
     * Called to guess a face.
     */
    public void handleGuessFace(String aName)
    {
        // Get main face and name
        FaceEntry mainFace = getMainFace();
        if (mainFace == null)
            return;
        String name = mainFace.getName().toLowerCase();
        String first = mainFace.getFirstName().toLowerCase();

        // If mace matches
        String guessName = aName.toLowerCase();
        boolean match = !guessName.isEmpty() && (name.startsWith(guessName) || first.startsWith(guessName));
        if (match)
            handleFaceWin(mainFace);
        else handleFaceLose(mainFace);
    }

    /**
     * Add win face.
     */
    private void handleFaceWin(FaceEntry aFace)
    {
        addWonFace(aFace);

        View view = aFace.getView();
        view.getAnim(500).setOpacity(0).setOnFinish(() -> handleFaceWinAnimDone(aFace)).play();
        getOwner().resetLater();
    }

    /**
     * Called when addWinFace anim is finished.
     */
    private void handleFaceWinAnimDone(FaceEntry aFace)
    {
        removeFieldFace(aFace);

        View view = aFace.getView();
        removeChild(view);
        view.setOpacity(1);

        if (_jboxWorld != null)
            _jboxWorld.removePhysicsForView(view);
    }

    /**
     * Called to add a lose face.
     */
    private void handleFaceLose(FaceEntry aFace)
    {
        addLostFace(aFace);
        getOwner().resetLater();

        Body body = (Body) aFace.getView().getPhysics().getNative();
        body.setGravityScale(2.5f);

        if (getLostFaces().size() >= 3)
            stopNewFaces();
    }

    /**
     * Add lost face.
     */
    protected void handleFaceCollide(FaceEntry aFace)
    {
        if (aFace.inPlay())
            handleFaceLose(aFace);
        removeFieldFace(aFace);
    }

    /**
     * Contact listener to handle collisions.
     */
    private class ViewContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact)
        {
            View viewA = (View) contact.getFixtureA().getBody().getUserData();
            View viewB = (View) contact.getFixtureB().getBody().getUserData();
            if (viewA instanceof FaceView faceView)
                handleFaceCollide(faceView.getFace());
            if (viewB instanceof FaceView faceView)
                handleFaceCollide(faceView.getFace());
        }

        @Override
        public void endContact(Contact contact)  { }
        @Override
        public void preSolve(Contact contact, Manifold oldManifold)  { }
        @Override
        public void postSolve(Contact contact, ContactImpulse impulse)  { }
    }
}
