package snapdemos.jbox2d;
import org.jbox2d.dynamics.Body;
import snap.view.ChildView;
import snap.view.RectView;

/**
 * This view subclass works with JBox.
 */
public class WorldView extends ChildView {

    // The JBox world
    private JBoxWorld _jboxWorld;

    // Static Body used for dragging
    private Body _leftWallBody;

    /**
     * Constructor.
     */
    public WorldView()
    {
        super();
        _jboxWorld = new JBoxWorld(this);
    }

    /**
     * Returns the JBox world.
     */
    public JBoxWorld getJBoxWorld()  { return _jboxWorld; }

    /**
     * Sets the height in meters.
     */
    public void setHeightInMeters(double height)
    {
        _jboxWorld.setPixelsToMeters(getHeight() / height);
    }

    /**
     * Adds walls to world view.
     */
    public void addWalls()
    {
        double viewW = getWidth();
        double viewH = getHeight();

        // Create left wall
        RectView leftWallView = new RectView(-1, -900, 1, viewH + 900);
        leftWallView.getPhysics(true);
        _jboxWorld._leftWallBody = _jboxWorld.createJboxBodyForView(leftWallView);

        // Create bottom wall
        RectView bottomWallView = new RectView(0, viewH+1, viewW, 1);
        bottomWallView.getPhysics(true);
        _jboxWorld.createJboxBodyForView(bottomWallView);

        // Create right wall
        RectView rightWallView = new RectView(viewW, -900, 1, viewH + 900);
        rightWallView.getPhysics(true);
        _jboxWorld.createJboxBodyForView(rightWallView);
    }

}
