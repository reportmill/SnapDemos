package snapdemos.facetris;
import snap.gfx.Color;
import snap.view.ParentView;
import snap.view.View;
import snap.view.ViewTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A View subclass to handle the real gameplay.
 */
public class PlayView extends ParentView {

    // The FaceView
    private List<FaceInfo>  _faces = new ArrayList<>();

    // The FaceView
    private List<FaceInfo>  _lostFaces = new ArrayList<>();

    // Constants
    private int BORDER_SIZE = 2;

    // Whether we need new face
    private boolean  _needFace = true;

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
        _timer = new ViewTimer(25, t -> animFrame());
        _newFaceTimer = new ViewTimer(3000, t -> addFace());
    }

    /**
     * Returns the active faces.
     */
    public List<FaceInfo> getFaces()
    {
        return _faces;
    }

    /**
     * Adds a face.
     */
    protected void addFace()
    {
        // Get next face and view
        FaceInfo face = FaceIndex.get().getNextFace();
        addFace(face);
    }

    /**
     * Adds a face.
     */
    protected void addFace(FaceInfo aFace)
    {
        // Get next face and view
        _faces.add(aFace);
        View view = aFace.getView();
        addChild(view);

        // Set location
        int x = _random.nextInt((int)(getWidth()-view.getWidth()));
        view.setX(x);
    }

    /**
     * Removes a face.
     */
    protected void removeFace(FaceInfo aFace)
    {
        _faces.remove(aFace);
        _lostFaces.add(aFace);
    }

    /**
     * Starts the play.
     */
    public void play()
    {
        addFace();
        _timer.start();
        _newFaceTimer.start();
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
     * Called for every frame.
     */
    private void animFrame()
    {
        FaceInfo faces[] = getFaces().toArray(new FaceInfo[0]);

        // Iterate over faces
        for (FaceInfo face : faces) {
            View view = face.getView();

            if (view.getMaxY()+2 > getBounds().getHeight() - BORDER_SIZE)
                removeFace(face);

            else {
                view.setY(view.getY() + 2);
            }
        }

        // If no more faces, stop
        if (_lostFaces.size()>=3)
            stop();
    }
}
