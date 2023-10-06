package snapdemos.facetris;
import java.util.*;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.*;
import snap.geom.*;
import snap.util.MathUtils;
import snap.view.*;
import snap.view.EventListener;

/**
 * A class to run Box2D physics for a view.
 */
public class PhysicsRunner {
    
    // The Snap View
    ParentView     _view;

    // The Box2D World
    World          _world;
    
    // The ratio of screen points to Box2D world meters.
    double         _scale = 720/10d;
    
    // The Runner
    Runnable       _runner;
    
    // Transforms
    Transform      _localToBox;

    // The builder
    PhysicsBuilder  _builder;
    
    // Listener to handle drags
    EventListener  _dragFilter = e -> handleDrag(e);
    
    // Ground Body
    Body           _groundBody;
    
    // MouseJoint used for dragging
    MouseJoint     _dragJoint;

    // Joints to be created
    List<View>  _joints = new ArrayList<>();

    // The speed
    private int INTERVAL_MILLIS = 25;
    private float INTERVL_SECS = INTERVAL_MILLIS/1000f;

    /**
     * Create new PhysicsRunner.
     */
    public PhysicsRunner(ParentView aView)
    {
        // Set View
        _view = aView;

        // Create world
        _world = new World(new Vec2(0, -9.8f));

        // Create Builder
        _builder = new PhysicsBuilder(this);

        _world.setContactListener(new ViewContactListener());
    }

    /**
     * addWalls.
     */
    public void addWalls()
    {
        // Add sidewalls
        double vw = _view.getWidth();
        double vh = _view.getHeight();

        // Create left wall
        RectView r0 = new RectView(-1, -900, 1, vh+900);
        r0.getPhysics(true);
        _groundBody = _builder.createBody(r0);

        // Create bottom wall
        RectView r1 = new RectView(0, vh+1, vw, 1);
        r1.getPhysics(true);
        _builder.createBody(r1);

        // Create right wall
        RectView r2 = new RectView(vw, -900, 1, vh+900);
        r2.getPhysics(true);
        _builder.createBody(r2);
    }

    /**
     * addPhysForViews.
     */
    public void addPhysForViews()
    {
        // Add bodies for view children
        for (View child : _view.getChildren())
            addPhysForView(child);

        // Add joints
        addJoints();
    }

    /**
     * Adds physics for view.
     */
    public void addPhysForView(View aView)
    {
        ViewPhysics phys = aView.getPhysics(true);
        if(phys.isJoint() || "joint".equals(aView.getName()))
            _joints.add(aView);
        else {
            phys.setDynamic(true);
            _builder.createBody(aView);
            addDragger(aView);
        }
    }

    /**
     * Removes physics for view.
     */
    public void removePhysForView(View aView)
    {
        Body body = (Body)aView.getPhysics().getNative();
        _world.destroyBody(body);
    }

    /**
     * Adds joints.
     */
    public void addJoints()
    {
        for(View v : _joints)
            _builder.createJoint(v);
        _joints.clear();
    }

    /**
     * Returns the scale of the world in screen points to Box2D world meters.
     */
    public double getViewToWorldMeters(double aScale)  { return _scale; }

    /**
     * Sets the scale of the world in screen points to Box2D world meters.
     *
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
        if(aValue==isRunning()) return;

        // Set timer to call timerFired 25 times a second
        if(_runner==null)
            ViewEnv.getEnv().runIntervals(_runner = () -> timerFired(), INTERVAL_MILLIS);
        else {
            ViewEnv.getEnv().stopIntervals(_runner); _runner = null; }
    }

    /**
     * Called when world timer fires.
     */
    void timerFired()
    {
        // Update Statics
        for(int i=0,iMax=_view.getChildCount();i<iMax;i++)
            updateBody(_view.getChild(i));

        // Update world
        _world.step(INTERVL_SECS,20,20);

        // Update Dynamics
        for(int i=0,iMax=_view.getChildCount();i<iMax;i++)
            updateView(_view.getChild(i));
    }

    /**
     * Updates a view from a body.
     */
    public void updateView(View aView)
    {
        // Get ViewPhysics and body
        ViewPhysics <Body> phys = aView.getPhysics(); if (phys==null) return;
        Object ntv = phys.getNative();

        // Handle Body
        if (ntv instanceof Body) { Body body = (Body)ntv; if (!phys.isDynamic()) return;

            // Get/set position
            Vec2 pos = body.getPosition();
            Point posV = boxToView(pos.x, pos.y);
            aView.setXY(posV.x-aView.getWidth()/2, posV.y-aView.getHeight()/2);

            // Get set rotation
            float angle = body.getAngle();
            aView.setRotate(-Math.toDegrees(angle));
        }

        // Handle Joint
        else if (ntv instanceof RevoluteJoint) { RevoluteJoint joint = (RevoluteJoint)ntv;

            // Get/set position
            Vec2 pos = new Vec2(0,0); joint.getAnchorA(pos);
            Point posV = boxToView(pos.x, pos.y);
            aView.setXY(posV.x-aView.getWidth()/2, posV.y-aView.getHeight()/2);

            // Get set rotation
            //float angle = joint.getAngle(); aView.setRotate(-Math.toDegrees(angle));
        }
    }

    /**
     * Updates a body from a view.
     */
    public void updateBody(View aView)
    {
        // Get ViewPhysics and body
        ViewPhysics <Body> phys = aView.getPhysics();
        if (phys==null || phys.isDynamic() || phys.isJoint()) return;
        Body body = phys.getNative();

        // Get/set position
        Vec2 pos0 = body.getPosition();
        Vec2 pos1 = viewToBox(aView.getMidX(), aView.getMidY());
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
     * Adds DragFilter to view.
     */
    void addDragger(View aView)
    {
        aView.addEventFilter(_dragFilter, View.MousePress, View.MouseDrag, View.MouseRelease);
    }

    /**
     * Called when View gets drag event.
     */
    void handleDrag(ViewEvent anEvent)
    {
        // Get View, ViewPhysics, Body and Event point in page view
        View view = anEvent.getView();
        ViewPhysics <Body> phys = view.getPhysics();
        Body body = phys.getNative();
        Point pnt = anEvent.getPoint(view.getParent()); anEvent.consume();

        // Handle MousePress: Create & install drag MouseJoint
        if (anEvent.isMousePress()) {
            MouseJointDef jdef = new MouseJointDef();
            jdef.bodyA = _groundBody;
            jdef.bodyB = body;
            jdef.collideConnected = true;
            jdef.maxForce = 1000f*body.getMass();
            jdef.target.set(viewToBox(pnt.x, pnt.y));
            _dragJoint = (MouseJoint)_world.createJoint(jdef);
            body.setAwake(true);
        }

        // Handle MouseDrag: Update drag MouseJoint
        else if (anEvent.isMouseDrag()) {
            Vec2 target = viewToBox(pnt.x, pnt.y);
            _dragJoint.setTarget(target);
        }

        // Handle MouseRelease: Remove drag MouseJoint
        else if (anEvent.isMouseRelease()) {
            _world.destroyJoint(_dragJoint); _dragJoint = null; }
    }

    /** Called when View gets drag event. */
    void handleDragOld(ViewEvent anEvent)
    {
        // Get View, ViewPhysics, Body and Event point in page view
        View view = anEvent.getView(); ViewPhysics <Body> phys = view.getPhysics();
        Body body = phys.getNative();
        Point pnt = anEvent.getPoint(view.getParent()); anEvent.consume();

        // Handle MousePress
        if (anEvent.isMousePress()) { body.setType(BodyType.KINEMATIC); body.setAngularVelocity(0); }
        else if (anEvent.isMouseDrag()) updateDrag(view, pnt.x, pnt.y);
        else if (anEvent.isMouseRelease()) body.setType(phys.isDynamic()? BodyType.DYNAMIC : BodyType.KINEMATIC);
    }

    /** Updates drag view's body. */
    void updateDrag(View aView, double dragX, double dragY)
    {
        ViewPhysics <Body> phys = aView.getPhysics();
        Body body = phys.getNative();
        Vec2 pos0 = body.getPosition();
        Vec2 pos1 = viewToBox(dragX, dragY);
        double dx = pos1.x - pos0.x;
        double dy = pos1.y - pos0.y;
        double vx = (pos1.x - pos0.x)*25;
        double vy = (pos1.y - pos0.y)*25;
        body.setLinearVelocity(new Vec2((float)vx, (float)vy));
    }

    /**
     * Convert View coord to Box2D.
     */
    public float viewToBox(double aValue)  { return (float)(aValue/_scale); }

    /**
     * Convert View coord to Box2D.
     */
    public Vec2 viewToBox(double aX, double aY)  { return getVec(getViewToBox().transformXY(aX, aY)); }

    /**
     * Convert Box2D coord to View.
     */
    public double boxToView(double aValue)  { return aValue*_scale; }

    /**
     * Convert Box2D coord to View.
     */
    public Point boxToView(double aX, double aY)  { return getBoxToView().transformXY(aX, aY); }

    /**
     * Returns transform from View coords to Box coords.
     */
    public Transform getViewToBox()
    {
        // If already set, just return
        if(_localToBox!=null) return _localToBox;

        // Create transform from WorldView bounds to World bounds
        Rect r0 = _view.getBoundsLocal();
        Rect r1 = new Rect(0, 0, r0.width/_scale, -r0.height/_scale);
        double bw = r0.width, bh = r0.height;
        double sx = bw!=0? r1.width/bw : 0, sy = bh!=0? r1.height/bh : 0;
        Transform trans = Transform.getScale(sx, sy);
        trans.translate(r1.x - r0.x, r1.y - r0.y);
        return trans;
    }

    /**
     * Returns transform from Box coords to View coords.
     */
    public Transform getBoxToView()  { return getViewToBox().getInverse(); }

    /**
     * Converts from View to box coords.
     */
    Vec2 viewToBoxLocal(double aX, double aY, View aView)
    {
        float x = viewToBox(aX - aView.getWidth()/2);
        float y = viewToBox(aView.getHeight()/2 - aY);
        return new Vec2(x,y);
    }

    /**
     * Return Vec2 for snap Point.
     */
    Vec2 getVec(Point aPnt)  { return new Vec2((float)aPnt.x, (float)aPnt.y); }

    private class ViewContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact)
        {
            View viewA = (View)contact.getFixtureA().getBody().getUserData();
            View viewB = (View)contact.getFixtureB().getBody().getUserData();
            if (viewA instanceof FaceView) {
                FaceView fview = (FaceView)viewA;
                ((PlayView)_view).handleFaceCollide(fview.getFace());
            }
            if (viewB instanceof FaceView) {
                FaceView fview = (FaceView)viewB;
                ((PlayView)_view).handleFaceCollide(fview.getFace());
            }
        }

        @Override
        public void endContact(Contact contact)
        {

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold)
        {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse)
        {

        }
    }
}