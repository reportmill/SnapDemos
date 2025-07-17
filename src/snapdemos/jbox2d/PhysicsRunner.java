package snapdemos.jbox2d;
import java.util.*;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.*;
import snap.geom.*;
import snap.util.MathUtils;
import snap.view.*;
import snap.view.EventListener;

/**
 * A class to run Box2D physics for a view.
 */
public class PhysicsRunner {
    
    // The world view
    private ParentView _worldView;

    // The Box2D World
    protected World _world;
    
    // The ratio of screen points to Box2D world meters.
    private double _scale = 720/10d;
    
    // The Runner
    private Runnable _runner;
    
    // The builder
    private PhysicsBuilder _builder;
    
    // Listener to handle drags
    private EventListener _viewDraggingEventLsnr;
    
    // Static Body used for dragging
    private Body _leftWallBody;
    
    // MouseJoint used for dragging
    private MouseJoint _dragJoint;

    // Joints to be created
    private List<View> _jointViews = new ArrayList<>();

    // The speed
    private int INTERVAL_MILLIS = 25;
    private float INTERVL_SECS = INTERVAL_MILLIS/1000f;

    /**
     * Constructor for given world view.
     */
    public PhysicsRunner(ParentView worldView)
    {
        // Set View
        _worldView = worldView;

        // Create world
        _world = new World(new Vec2(0, -9.8f));

        // Create Builder
        _builder = new PhysicsBuilder(this);
    }

    /**
     * Adds walls to world view.
     */
    public void addWallsToWorldView()
    {
        double viewW = _worldView.getWidth();
        double viewH = _worldView.getHeight();

        // Create left wall
        RectView leftWallView = new RectView(-1, -900, 1, viewH + 900);
        leftWallView.getPhysics(true);
        _leftWallBody = _builder.createJboxBodyForView(leftWallView);

        // Create bottom wall
        RectView bottomWallView = new RectView(0, viewH+1, viewW, 1);
        bottomWallView.getPhysics(true);
        _builder.createJboxBodyForView(bottomWallView);

        // Create right wall
        RectView rightWallView = new RectView(viewW, -900, 1, viewH + 900);
        rightWallView.getPhysics(true);
        _builder.createJboxBodyForView(rightWallView);
    }

    /**
     * Adds physics to world view children.
     */
    public void addPhysicsForWorldViewChildren()
    {
        _worldView.getChildren().forEach(this::addPhysicsForView);
        addJoints();
    }

    /**
     * Adds physics to given view.
     */
    public void addPhysicsForView(View aView)
    {
        ViewPhysics<?> viewPhysics = aView.getPhysics(true);

        // Handle Joint: Just add to list of joint views
        if (viewPhysics.isJoint() || "joint".equals(aView.getName()))
            _jointViews.add(aView);

        // Handle Body: Create and set native jbox body in view physics
        else {
            viewPhysics.setDynamic(true);
            _builder.createJboxBodyForView(aView);
            enableViewDragging(aView);
        }
    }

    /**
     * Removes physics for view.
     */
    public void removePhysicsForView(View aView)
    {
        Body body = (Body) aView.getPhysics().getNative();
        _world.destroyBody(body);
    }

    /**
     * Sets a contact listener.
     */
    public void setContactListener(ContactListener contactLsnr)
    {
        _world.setContactListener(contactLsnr);
    }

    /**
     * Adds joints.
     */
    public void addJoints()
    {
        _jointViews.forEach(view -> _builder.createJboxJointForViewAndSet(view));
        _jointViews.clear();
    }

    /**
     * Returns the scale of the world in screen points to Box2D world meters.
     */
    public double getViewToWorldMeters()  { return _scale; }

    /**
     * Sets the scale of the world in screen points to Box2D world meters.
     * If you want a 720 point tall view to be 10m, set scale to be 720/10d (the default).
     */
    public void setViewToWorldMeters(double aScale)  { _scale = aScale; }

    /**
     * Returns whether physics is running.
     */
    public boolean isRunning()  { return _runner!=null; }

    /**
     * Sets whether physics is running.
     */
    public void setRunning(boolean aValue)
    {
        // If already set, just return
        if(aValue == isRunning()) return;

        // Set timer to call timerFired 25 times a second
        if(_runner == null)
            ViewEnv.getEnv().runIntervals(_runner = this::handleTimerFired, INTERVAL_MILLIS);

        else {
            ViewEnv.getEnv().stopIntervals(_runner);
            _runner = null;
        }
    }

    /**
     * Called when world timer fires.
     */
    private void handleTimerFired()
    {
        // Update jbox natives from world view child views (maybe one was dragged or updated externally)
        _worldView.getChildren().forEach(this::updateJboxBodyFromView);

        // Update world
        _world.step(INTERVL_SECS,20,20);

        // Update world view children from jbox natives
        _worldView.getChildren().forEach(this::updateViewFromJboxNative);
    }

    /**
     * Updates a view from a body.
     */
    private void updateViewFromJboxNative(View aView)
    {
        // Get ViewPhysics and body
        ViewPhysics<?> phys = aView.getPhysics(); if (phys == null) return;
        Object jboxNative = phys.getNative();

        // Handle Body
        if (jboxNative instanceof Body body) {
            if (!phys.isDynamic())
                return;

            // Get/set position
            Vec2 pos = body.getPosition();
            Point posV = convertJboxXYToView(pos.x, pos.y);
            aView.setXY(posV.x - aView.getWidth() / 2, posV.y - aView.getHeight() / 2);

            // Get set rotation
            float angle = body.getAngle();
            aView.setRotate(-Math.toDegrees(angle));
        }

        // Handle Joint
        else if (jboxNative instanceof RevoluteJoint joint) {

            // Get/set position
            Vec2 pos = new Vec2(0,0); joint.getAnchorA(pos);
            Point posV = convertJboxXYToView(pos.x, pos.y);
            aView.setXY(posV.x - aView.getWidth() / 2, posV.y - aView.getHeight() / 2);

            // Get set rotation
            //float angle = joint.getAngle(); aView.setRotate(-Math.toDegrees(angle));
        }
    }

    /**
     * Updates a body from a view.
     */
    private void updateJboxBodyFromView(View aView)
    {
        // Get ViewPhysics and body
        ViewPhysics<Body> phys = aView.getPhysics();
        if (phys == null || phys.isDynamic() || phys.isJoint())
            return;
        Body body = phys.getNative();

        // Get/set position
        Vec2 pos0 = body.getPosition();
        Vec2 pos1 = convertViewXYToJbox(aView.getMidX(), aView.getMidY());
        double vx = (pos1.x - pos0.x)*25;
        double vy = (pos1.y - pos0.y)*25;
        body.setLinearVelocity(new Vec2((float)vx, (float)vy));

        // Get/set rotation
        double rot0 = body.getAngle();
        double rot1 = Math.toRadians(-aView.getRotate());
        double dr = rot1 - rot0;
        if(dr>Math.PI || dr<-Math.PI) dr = MathUtils.mod(dr + Math.PI, Math.PI*2) - Math.PI;
        body.setAngularVelocity((float)dr*25);
    }

    /**
     * Enables user mouse dragging of given view.
     */
    public void enableViewDragging(View aView)
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
            jdef.target.set(convertViewXYToJbox(dragPoint.x, dragPoint.y));
            _dragJoint = (MouseJoint)_world.createJoint(jdef);
            dragBody.setAwake(true);
        }

        // Handle MouseDrag: Update drag MouseJoint
        else if (anEvent.isMouseDrag()) {
            Vec2 target = convertViewXYToJbox(dragPoint.x, dragPoint.y);
            _dragJoint.setTarget(target);
        }

        // Handle MouseRelease: Remove drag MouseJoint
        else if (anEvent.isMouseRelease()) {
            _world.destroyJoint(_dragJoint);
            _dragJoint = null;
        }
    }

    /**
     * Convert View coord to Box2D.
     */
    public float convertViewCoordToJbox(double aValue)  { return (float) (aValue / _scale); }

    /**
     * Convert View coord to Box2D.
     */
    public Vec2 convertViewXYToJbox(double aX, double aY)  { return getVec(getViewToBoxTransform().transformXY(aX, aY)); }

    /**
     * Convert Box2D coord to View.
     */
    public double convertJboxCoordToView(double aValue)  { return aValue * _scale; }

    /**
     * Convert Box2D coord to View.
     */
    public Point convertJboxXYToView(double aX, double aY)  { return getBoxToViewTransform().transformXY(aX, aY); }

    /**
     * Returns transform from View coords to Box coords.
     */
    public Transform getViewToBoxTransform()
    {
        // Create transform from WorldView bounds to World bounds
        Rect r0 = _worldView.getBoundsLocal();
        Rect r1 = new Rect(0, 0, r0.width / _scale, -r0.height / _scale);
        double bw = r0.width;
        double bh = r0.height;
        double sx = bw != 0 ? r1.width / bw : 0;
        double sy = bh != 0 ? r1.height / bh : 0;
        Transform trans = Transform.getScale(sx, sy);
        trans.translate(r1.x - r0.x, r1.y - r0.y);

        // Return
        return trans;
    }

    /**
     * Returns transform from Box coords to View coords.
     */
    public Transform getBoxToViewTransform()  { return getViewToBoxTransform().getInverse(); }

    /**
     * Converts from View to box coords.
     */
    protected Vec2 convertViewXYToJboxLocal(double aX, double aY, View aView)
    {
        float x = convertViewCoordToJbox(aX - aView.getWidth() / 2);
        float y = convertViewCoordToJbox(aView.getHeight() / 2 - aY);
        return new Vec2(x, y);
    }

    /**
     * Return Vec2 for snap Point.
     */
    private Vec2 getVec(Point aPnt)  { return new Vec2((float) aPnt.x, (float) aPnt.y); }
}