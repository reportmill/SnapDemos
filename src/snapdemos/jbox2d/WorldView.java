package snapdemos.jbox2d;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import snap.geom.Point;
import snap.util.ListUtils;
import snap.view.*;
import java.util.List;

/**
 * This view subclass facilitates using JBox2D with SnapKit.
 */
public class WorldView extends ChildView {

    // The JBox world
    private JBoxWorld _jboxWorld;

    // The Runner
    private Runnable _runner;

    // MouseJoint used for dragging
    private MouseJoint _dragJoint;

    // Static dummy body used by mouse joint for dragging
    private Body _dragGroundBody;

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
     * Returns the gravity in meters per second.
     */
    public double getGravity()  { return _jboxWorld.getGravity(); }

    /**
     * Sets the gravity in meters per second.
     */
    public void setGravity(double aValue)  { _jboxWorld.setGravity(aValue); }

    /**
     * Sets the height in meters.
     */
    public void setHeightInMeters(double height)
    {
        _jboxWorld.setPixelsToMeters(getHeight() / height);
    }

    /**
     * Adds ground to world view.
     */
    public void addGround()
    {
        RectView bottomWallView = new RectView(0, getHeight() + 1, getWidth(), 1);
        bottomWallView.getPhysics(true);
        _jboxWorld.createJboxBodyForView(bottomWallView);
    }

    /**
     * Adds ground and walls to world view.
     */
    public void addGroundAndWalls()
    {
        addGround();

        // Add left wall
        RectView leftWallView = new RectView(-1, -900, 1, getHeight() + 900);
        leftWallView.getPhysics(true);
        _jboxWorld.createJboxBodyForView(leftWallView);

        // Add right wall
        RectView rightWallView = new RectView(getWidth(), -900, 1, getHeight() + 900);
        rightWallView.getPhysics(true);
        _jboxWorld.createJboxBodyForView(rightWallView);
    }

    /**
     * Returns whether physics is running.
     */
    public boolean isRunning()  { return _runner != null; }

    /**
     * Sets whether physics is running.
     */
    public void setRunning(boolean aValue)
    {
        // If already set, just return
        if(aValue == isRunning()) return;

        // Set timer to call timerFired 25 times a second
        if(_runner == null)
            ViewEnv.getEnv().runIntervals(_runner = _jboxWorld::stepWorld, JBoxWorld.INTERVAL_MILLIS);

        else {
            ViewEnv.getEnv().stopIntervals(_runner);
            _runner = null;
        }
    }

    /**
     * Adds a child with physics.
     */
    public void addChildWithPhysics(View aView)  { addChildWithPhysics(aView, getChildCount()); }

    /**
     * Adds a child with physics.
     */
    public void addChildWithPhysics(View aView, int anIndex)
    {
        addChild(aView, anIndex);
        if (!JBoxWorld.isJoint(aView))
            addBodyForView(aView);
        else addJointForView(aView);
    }

    /**
     * Override to remove JBox natives.
     */
    @Override
    public View removeChild(int anIndex)
    {
        // Do normal version
        View removedView = super.removeChild(anIndex);

        // Remove JBox native
        ViewPhysics<?> viewPhysics = removedView.getPhysics();
        Object jboxNative = viewPhysics != null ? viewPhysics.getNative() : null;
        if (jboxNative != null) {
            if (jboxNative instanceof Body jboxBody)
                _jboxWorld.getWorld().destroyBody(jboxBody);
            else if (jboxNative instanceof Joint jboxJoint)
                _jboxWorld.getWorld().destroyJoint(jboxJoint);
            viewPhysics.setNative(null);
        }

        // Return
        return removedView;
    }

    /**
     * Adds physics to world view children.
     */
    public void addJBoxNativesForChildren()
    {
        ViewList children = getChildren();

        // Add body views
        List<View> bodyChildren = ListUtils.filter(children, child -> !JBoxWorld.isJoint(child));
        bodyChildren.forEach(this::addBodyForView);

        // Add joint views
        List<View> jointChildren = ListUtils.filter(children, child -> JBoxWorld.isJoint(child));
        jointChildren.forEach(this::addJointForView);
    }

    /**
     * Adds body to given child view.
     */
    protected void addBodyForView(View aView)
    {
        // Create body
        ViewPhysics<Body> viewPhysics = aView.getPhysics(true);
        viewPhysics.setDynamic(true);
        Body body = _jboxWorld.createJboxBodyForView(aView);

        // Add view <--> body links
        viewPhysics.setNative(body);
        body.setUserData(aView);

        // Enable dragging
        if (viewPhysics.isDraggable())
            enableDraggingForView(aView);
    }

    /**
     * Adds joint to given child view.
     */
    protected void addJointForView(View aView)
    {
        // Create joint and add to view
        RevoluteJoint joint = _jboxWorld.createJboxJointForView(aView);
        aView.getPhysics(true).setNative(joint);

        // Remove view for joint
        aView.getParent(ChildView.class).removeChild(aView);
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
        Body dragBody = (Body) dragView.getPhysics().getNative();
        Point dragPoint = anEvent.getPoint(dragView.getParent());
        anEvent.consume();

        // Handle MousePress: Create & install drag MouseJoint
        if (anEvent.isMousePress()) {
            MouseJointDef jdef = new MouseJointDef();
            jdef.bodyA = getDragGroundBody();
            jdef.bodyB = dragBody;
            jdef.collideConnected = true;
            jdef.maxForce = 1000f * dragBody.getMass();
            jdef.target.set(_jboxWorld.convertViewXYToJbox(dragPoint.x, dragPoint.y));
            _dragJoint = (MouseJoint) _jboxWorld.getWorld().createJoint(jdef);
            dragBody.setAwake(true);
        }

        // Handle MouseDrag: Update drag MouseJoint
        else if (anEvent.isMouseDrag()) {
            Vec2 dragPointInJbox = _jboxWorld.convertViewXYToJbox(dragPoint.x, dragPoint.y);
            _dragJoint.setTarget(dragPointInJbox);
        }

        // Handle MouseRelease: Remove drag MouseJoint
        else if (anEvent.isMouseRelease()) {
            _jboxWorld.getWorld().destroyJoint(_dragJoint);
            _dragJoint = null;
        }
    }

    /** Returns the static dummy body used by mouse joint for dragging. */
    private Body getDragGroundBody()
    {
        if (_dragGroundBody != null) return _dragGroundBody;
        return _dragGroundBody = _jboxWorld.getWorld().createBody(new BodyDef());
    }
}
