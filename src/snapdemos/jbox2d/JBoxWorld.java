package snapdemos.jbox2d;
import java.util.*;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.*;
import snap.geom.*;
import snap.util.ListUtils;
import snap.util.MathUtils;
import snap.view.*;
import snap.view.EventListener;

/**
 * A class to run Box2D physics for a view.
 */
public class JBoxWorld {
    
    // The world view
    private ParentView _worldView;

    // The Box2D World
    protected World _world;
    
    // The ratio of screen points to Box2D world meters.
    private double _pixelsToMeters = 720 / 10d;
    
    // The Runner
    private Runnable _runner;
    
    // Listener to handle drags
    private EventListener _viewDraggingEventLsnr;
    
    // Static Body used for dragging
    protected Body _leftWallBody;
    
    // MouseJoint used for dragging
    private MouseJoint _dragJoint;

    // The speed
    private int INTERVAL_MILLIS = 25;
    private float INTERVL_SECS = INTERVAL_MILLIS / 1000f;

    // Constant for default gravity in world
    private static final float DEFAULT_GRAVITY = -9.8f;

    /**
     * Constructor for given world view.
     */
    public JBoxWorld(WorldView worldView)
    {
        // Set View
        _worldView = worldView;

        // Create jbox world
        _world = new World(new Vec2(0, DEFAULT_GRAVITY));
    }

    /**
     * Returns the scale of the world in screen points to Box2D world meters.
     */
    public double getPixelsToMeters()  { return _pixelsToMeters; }

    /**
     * Sets the ratio of view screen points to Box2D world meters.
     * If you want a 720 point tall view to be 10m, set scale to be 720 / 10d (the default).
     */
    public void setPixelsToMeters(double aValue)
    {
        if (aValue == getPixelsToMeters()) return;
        _pixelsToMeters = aValue;
    }

    /**
     * Adds body to given view.
     */
    public void addBodyForView(View aView)
    {
        // Create body
        ViewPhysics<Body> viewPhysics = aView.getPhysics(true);
        viewPhysics.setDynamic(true);
        Body body = createJboxBodyForView(aView);

        // Add view <--> body links
        viewPhysics.setNative(body);
        body.setUserData(aView);

        // Enable dragging
        enableViewDragging(aView);
    }

    /**
     * Adds body to given view.
     */
    public void addJointForView(View aView)
    {
        // Create joint and add to view
        RevoluteJoint joint = createJboxJointForView(aView);
        aView.getPhysics(true).setNative(joint);

        // Remove view for joint
        aView.getParent(ChildView.class).removeChild(aView);
    }

    /**
     * Removes body for view.
     */
    public void removeJBoxNativeForView(View aView)
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
        _world.step(INTERVL_SECS, 8, 3);

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
        if(dr > Math.PI || dr < -Math.PI)
            dr = MathUtils.mod(dr + Math.PI, Math.PI * 2) - Math.PI;
        body.setAngularVelocity((float) dr * 25);
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
     * Returns a body for a view.
     */
    public Body createJboxBodyForView(View aView)
    {
        // Create BodyDef
        ViewPhysics<Body> phys = aView.getPhysics();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = phys.isDynamic() ? BodyType.DYNAMIC : BodyType.KINEMATIC;
        bodyDef.position.set(convertViewXYToJbox(aView.getMidX(), aView.getMidY()));
        bodyDef.angle = (float) Math.toRadians(-aView.getRotate());
        //bodyDef.linearDamping = 10;
        //bodyDef.angularDamping = 10;

        // Create Body
        Body body = _world.createBody(bodyDef);

        // Create PolygonShape
        Shape viewShape = aView.getBoundsShape();
        List<org.jbox2d.collision.shapes.Shape> jboxShapes = createJboxShapesForShape(viewShape);

        // Create FixtureDef
        for (org.jbox2d.collision.shapes.Shape jboxShape : jboxShapes) {
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = jboxShape;
            fixtureDef.density = (float) phys.getDensity();
            fixtureDef.friction = .3f;
            fixtureDef.restitution = .6f;
            fixtureDef.filter.groupIndex = phys.getGroupIndex();
            body.createFixture(fixtureDef);
        }

        // Return
        return body;
    }

    /**
     * Creates a Box2D shape for given snap shape.
     */
    public List<org.jbox2d.collision.shapes.Shape> createJboxShapesForShape(Shape aShape)
    {
        // Handle Rect (simple case)
        if (aShape instanceof Rect rect) {
            PolygonShape polygonShape = new PolygonShape();
            float pw = convertViewCoordToJbox(rect.width / 2);
            float ph = convertViewCoordToJbox(rect.height / 2);
            polygonShape.setAsBox(pw, ph);
            return List.of(polygonShape);
        }

        // Handle Ellipse
        if (aShape instanceof Ellipse ellipse && aShape.getWidth() == aShape.getHeight()) {
            CircleShape circleShape = new CircleShape();
            circleShape.setRadius(convertViewCoordToJbox(ellipse.getWidth() / 2));
            return List.of(circleShape);
        }

        // Handle Arc
        if (aShape instanceof Arc arc && aShape.getWidth() == aShape.getHeight()) {
            if (arc.getSweepAngle() == 360) {
                CircleShape cshape = new CircleShape();
                cshape.setRadius(convertViewCoordToJbox(arc.getWidth()/2));
                return List.of(cshape);
            }
        }

        // Handle Polygon if Simple, Convex and less than 8 points
        if (aShape instanceof Polygon poly) {
            org.jbox2d.collision.shapes.Shape pshape = createJboxShapeForPolygon(poly);
            if(pshape != null)
                return List.of(pshape);
        }

        // Get shape centered around shape midpoint
        Rect shapeBounds = aShape.getBounds();
        Shape shape = aShape.copyFor(new Transform(-shapeBounds.width / 2, -shapeBounds.height / 2));

        // Get convex Polygons for shape
        List<Polygon> convexPolys = Polygon.getConvexPolygonsWithMaxSideCount(shape, 8);
        return ListUtils.mapNonNull(convexPolys, this::createJboxShapeForPolygon);
    }

    /**
     * Creates a Box2D shape for given snap shape.
     */
    public org.jbox2d.collision.shapes.Shape createJboxShapeForPolygon(Polygon aPoly)
    {
        // If invalid, just return null
        if (aPoly.isSelfIntersecting() || !aPoly.isConvex() || aPoly.getPointCount() > 8)
            return null;

        // Create Box2D PolygonShape
        int pointCount = aPoly.getPointCount();
        Vec2[] vecs = new Vec2[pointCount];
        for (int i = 0; i < pointCount; i++)
            vecs[i] = convertViewXYToJbox(aPoly.getPointX(i), aPoly.getPointY(i));
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.set(vecs, vecs.length);

        // Return
        return polygonShape;
    }

    /**
     * Creates a Joint for given joint view.
     */
    public RevoluteJoint createJboxJointForView(View aView)
    {
        // Get shapes interesting joint view
        ParentView editor = aView.getParent();
        Rect viewBoundsInParent = aView.getBoundsParent();
        List<View> hits = new ArrayList<>();
        for (View v : editor.getChildren()) {
            if(v != aView && v.getBoundsLocal().intersectsShape(v.parentToLocal(viewBoundsInParent)))
                hits.add(v);
        }

        // if less than two, bail
        if (hits.size() < 2) {
            System.out.println("PhysicsRunner.createJoint: 2 Bodies not found for joint: " + aView.getName());
            return null;
        }

        // Get joint views
        View viewA = hits.get(0);
        View viewB = hits.get(1);

        // Create joint def and set body A/B
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = (Body) viewA.getPhysics().getNative();
        jointDef.bodyB = (Body) viewB.getPhysics().getNative();
        jointDef.collideConnected = false;

        // Set anchors
        Point jointPnt = aView.localToParent(aView.getWidth()/2, aView.getHeight()/2);
        Point jointPntA = viewA.parentToLocal(jointPnt.x, jointPnt.y);
        Point jointPntB = viewB.parentToLocal(jointPnt.x, jointPnt.y);
        jointDef.localAnchorA = convertViewXYToJboxLocal(jointPntA.x, jointPntA.y, viewA);
        jointDef.localAnchorB = convertViewXYToJboxLocal(jointPntB.x, jointPntB.y, viewB);

        // Create joint and return
        return (RevoluteJoint) _world.createJoint(jointDef);
    }

    /**
     * Convert View coord to Box2D.
     */
    public float convertViewCoordToJbox(double aValue)  { return (float) (aValue / _pixelsToMeters); }

    /**
     * Convert View coord to Box2D.
     */
    public Vec2 convertViewXYToJbox(double aX, double aY)
    {
        double jboxX = aX / _pixelsToMeters;
        double jboxY = -aY / _pixelsToMeters;
        return getVec2(jboxX, jboxY);
    }

    /**
     * Convert Box2D coord to View.
     */
    public Point convertJboxXYToView(double aX, double aY)
    {
        double viewX = aX * _pixelsToMeters;
        double viewY = -aY * _pixelsToMeters;
        return new Point(viewX, viewY);
    }

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
     * Returns whether given view is joint.
     */
    protected static boolean isJoint(View aView)
    {
        ViewPhysics<?> viewPhysics = aView.getPhysics(true);
        return viewPhysics.isJoint() || "joint".equals(aView.getName());
    }

    /**
     * Return Vec2 for snap Point.
     */
    private static Vec2 getVec2(Point aPnt)  { return new Vec2((float) aPnt.x, (float) aPnt.y); }
    private static Vec2 getVec2(double aX, double aY)  { return new Vec2((float) aX, (float) aY); }
}