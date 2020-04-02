package snapdemos.facetris;
import snap.view.ParentView;
import snap.view.View;
import snap.view.ViewTimer;
import java.util.Random;

/**
 *
 */
public class PlayView extends ParentView {

    // The FaceView
    private FaceInfo _face;

    // Whether we need new face
    private boolean  _needFace = true;

    // The Timer
    private ViewTimer _timer;

    // The Random
    private Random  _random = new Random();

    /**
     * Creates PlayView.
     */
    public PlayView()
    {
        _timer = new ViewTimer(50, t -> animFrame());
    }

    /**
     *
     */
    public void play()
    {
        _timer.start();
    }

    /**
     * Called for every frame.
     */
    public void animFrame()
    {
        FaceInfo face = getFace();
        View view = face.getView();
        view.setY(view.getY()+2);

        if (view.getMaxY()>getBounds().getMaxY())
            _timer.stop();
    }

    /**
     * Sets a new face.
     */
    public FaceInfo getFace()
    {
        if (_face!=null) return _face;
        FaceInfo face = FaceIndex.get().getNextFace();
        View view = face.getView();
        addChild(view);
        int x = _random.nextInt((int)(getWidth()-view.getWidth()));
        view.setX(x);
        return _face = face;
    }
}
