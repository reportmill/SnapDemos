package snapdemos.jbox2d;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import snap.geom.Point;
import snap.util.ListUtils;
import snap.view.*;
import java.util.List;

/**
 * This view subclass works with JBox.
 */
public class WorldView extends ChildView {

    // The JBox world
    private JBoxWorld _jboxWorld;

    // Static Body used for dragging
    private Body _leftWallBody;

    // MouseJoint used for dragging
    private MouseJoint _dragJoint;

    // Listener to handle drags
    private EventListener _viewDraggingEventLsnr;

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
        _leftWallBody = _jboxWorld.createJboxBodyForView(leftWallView);

        // Create bottom wall
        RectView bottomWallView = new RectView(0, viewH+1, viewW, 1);
        bottomWallView.getPhysics(true);
        _jboxWorld.createJboxBodyForView(bottomWallView);

        // Create right wall
        RectView rightWallView = new RectView(viewW, -900, 1, viewH + 900);
        rightWallView.getPhysics(true);
        _jboxWorld.createJboxBodyForView(rightWallView);
    }

    /**
     * Adds physics to world view children.
     */
    public void addJBoxNativesForChildren()
    {
        ViewList children = getChildren();

        // Add body views
        List<View> bodyChildren = ListUtils.filter(children, child -> !JBoxWorld.isJoint(child));
        bodyChildren.forEach(_jboxWorld::addBodyForView);

        // Add joint views
        List<View> jointChildren = ListUtils.filter(children, child -> JBoxWorld.isJoint(child));
        jointChildren.forEach(_jboxWorld::addJointForView);
    }

    /**
     * Enables user mouse dragging of given view.
     */
    public void enableDraggingForView(View aView)
    {
        if (_viewDraggingEventLsnr == null) _viewDraggingEventLsnr = this::handleViewMouseEventForDragging;
        aView.addEventFilter(_viewDraggingEventLsnr, View.MousePress, View.MouseDrag, View.MouseRelease);
    }

    /**
     * Called when View gets drag event.
     */
    private void handleViewMouseEventForDragging(ViewEvent anEvent)
    {
        // Get View, ViewPhysics, Body and Event point in page view
        View dragView = anEvent.getView();
        ViewPhysics<Body> phys = dragView.getPhysics();
        Body dragBody = phys.getNative();
        Point dragPoint = anEvent.getPoint(dragView.getParent());
        anEvent.consume();

        // Handle MousePress: Create & install drag MouseJoint
        if (anEvent.isMousePress()) {
            MouseJointDef jdef = new MouseJointDef();
            jdef.bodyA = _leftWallBody;
            jdef.bodyB = dragBody;
            jdef.collideConnected = true;
            jdef.maxForce = 1000f * dragBody.getMass();
            jdef.target.set(_jboxWorld.convertViewXYToJbox(dragPoint.x, dragPoint.y));
            _dragJoint = (MouseJoint) _jboxWorld.getWorld().createJoint(jdef);
            dragBody.setAwake(true);
        }

        // Handle MouseDrag: Update drag MouseJoint
        else if (anEvent.isMouseDrag()) {
            Vec2 target = _jboxWorld.convertViewXYToJbox(dragPoint.x, dragPoint.y);
            _dragJoint.setTarget(target);
        }

        // Handle MouseRelease: Remove drag MouseJoint
        else if (anEvent.isMouseRelease()) {
            _jboxWorld.getWorld().destroyJoint(_dragJoint);
            _dragJoint = null;
        }
    }
}
