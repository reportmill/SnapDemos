package snapdemos.facetris;
import snap.geom.Rect;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.view.ParentView;
import snap.view.View;
import snap.view.ViewTimer;
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
    private ViewTimer _timer;

    // The Timer
    private ViewTimer _newFaceTimer;

    // The Random
    private Random  _random = new Random();

    /**
     * Creates PlayView.
     */
    public PlayView()
    {
        setFill(new Color("#F0F8FF"));
        setBorder(Color.BLACK, BORDER_SIZE);
        setBorder(getBorder().copyFor(Border.PaintAbove_Prop, true));
        _timer = new ViewTimer(25, t -> animFrame());
        _newFaceTimer = new ViewTimer(3000, t -> addFace());
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
        _timer.start();
        _newFaceTimer.start(0);
    }

    /**
     * Stops the play.
     */
    public void stop()
    {
        _timer.stop();
        _newFaceTimer.stop();
    }

    /**
     * Stops the play.
     */
    public void stopNewFaces()
    {
        _newFaceTimer.stop();
    }

    /**
     * Adds a face.
     */
    protected void addFace()
    {
        // Get next face and view
        Face face = FaceIndex.get().getNextFaceFromQueue();
        _faces.addFieldFace(face);

        View view = face.getView();
        addChild(view);

        // Set location
        double pw = getWidth();
        double vw = view.getWidth();
        double vh = view.getHeight();
        int xRange = (int)(pw - vw);
        int x = _random.nextInt(xRange);
        view.setXY(x, -vh);
        getOwner().resetLater();
    }

    /**
     * Called to guess a face.
     */
    public boolean handleGuessFace(String aName)
    {
        // Get main face and name
        Face face = _faces.getMainFace();
        if (face==null) return false;
        String name = face.getName().toLowerCase();
        String first = face.getFirstName().toLowerCase();

        // If mace matches
        String gname = aName.toLowerCase();
        boolean match = gname.length()>0 && (name.startsWith(gname) || first.startsWith(gname));
        if (match)
            handleFaceWin(face);
        else handleFaceLose(face);
        return match;
    }

    /**
     * Add win face.
     */
    void handleFaceWin(Face aFace)
    {
        _player.addWonFace(aFace);

        View view = aFace.getView();
        view.getAnim(500).setOpacity(0).setOnFinish(() -> handleFaceWinAnimDone(aFace)).play();
        getOwner().resetLater();
    }

    /**
     * Called when addWinFace anim is finished.
     */
    void handleFaceWinAnimDone(Face aFace)
    {
        _faces.removeFieldFace(aFace);

        View view = aFace.getView();
        removeChild(view);
        view.setOpacity(1);
    }

    /**
     * Called to add a lose face.
     */
    void handleFaceLose(Face aFace)
    {
        _player.addLostFace(aFace);
        getOwner().resetLater();
    }

    /**
     * Add lost face.
     */
    void handleFaceCollide(Face aFace)
    {
        if (aFace.inPlay())
            handleFaceLose(aFace);

        _faces.removeFallFace(aFace);

        if (_player.getLostFaces().size()>=3)
            stopNewFaces();
    }

    /**
     * Called for every frame.
     */
    private void animFrame()
    {
        // Move faces
        moveFaces();

        // If no more faces, stop
        if (_faces.getFallFaces().size()==0 && _player.getLostFaces().size()>=3)
            stop();
    }

    /**
     * Moves any falling faces.
     */
    void moveFaces()
    {
        // Get falling faces
        Face fallFaces[] = _faces.getFallFaces().toArray(new Face[0]);

        // Iterate over faces
        for (Face face : fallFaces) {

            // If move will collide,
            double dx = face.getView().isLost() ? 6 : 2;
            if (willCollide(face, dx))
                handleFaceCollide(face);

            // Otherwise, move face
            else {
                View view = face.getView();
                view.setY(view.getY() + dx);
            }
        }
    }

    /**
     * Returns whether face move would hit anything.
     */
    public boolean willCollide(Face aFace, double dx)
    {
        View view = aFace.getView();
        Rect rect = view.getBounds();
        rect.y += dx;

        // If will hit border, return true
        if (rect.getMaxY() > getBounds().getHeight() - BORDER_SIZE)
            return true;

        for (Face face : _player.getLostFaces()) {
            if (face==aFace) break;
            if (face.getView().getBounds().intersectsRect(rect))
                return true;
        }
        return false;
    }
}
