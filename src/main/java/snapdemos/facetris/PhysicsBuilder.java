package snapdemos.facetris;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import snap.geom.*;
import snap.view.ChildView;
import snap.view.ParentView;
import snap.view.View;
import snap.view.ViewPhysics;
import java.util.ArrayList;
import java.util.List;

/**
 * A Builder.
 */
public class PhysicsBuilder {

    // The Runner
    private PhysicsRunner _runner;

    private World  _world;

    /**
     * Creates Builder.
     */
    public PhysicsBuilder(PhysicsRunner aRunner)
    {
        _runner = aRunner;
        _world = aRunner._world;
    }

    /**
     * Returns a body for a view.
     */
    public Body createBody(View aView)
    {
        // Create BodyDef
        ViewPhysics<Body> phys = aView.getPhysics();
        BodyDef bdef = new BodyDef();
        bdef.type = phys.isDynamic() ? BodyType.DYNAMIC : BodyType.KINEMATIC;
        bdef.position.set(viewToBox(aView.getMidX(), aView.getMidY()));
        bdef.angle = (float) Math.toRadians(-aView.getRotate());
        bdef.linearDamping = 10;
        bdef.angularDamping = 10;

        // Create Body
        Body body = _world.createBody(bdef);

        // Create PolygonShape
        Shape vshape = aView.getBoundsShape();
        org.jbox2d.collision.shapes.Shape pshapes[] = createShape(vshape);

        // Create FixtureDef
        for (org.jbox2d.collision.shapes.Shape pshp : pshapes) {
            FixtureDef fdef = new FixtureDef();
            fdef.shape = pshp;
            fdef.restitution = .25f;
            fdef.density = (float)phys.getDensity();
            fdef.filter.groupIndex = phys.getGroupIndex();
            body.createFixture(fdef);
        }

        // Return body
        phys.setNative(body);
        body.setUserData(aView);
        return body;
    }

    /**
     * Creates a Box2D shape for given snap shape.
     */
    public org.jbox2d.collision.shapes.Shape[] createShape(Shape aShape)
    {
        // Handle Rect (simple case)
        if (aShape instanceof Rect) { Rect rect = (Rect)aShape;
            PolygonShape pshape = new PolygonShape();
            float pw = viewToBox(rect.width/2);
            float ph = viewToBox(rect.height/2);
            pshape.setAsBox(pw, ph);
            return new org.jbox2d.collision.shapes.Shape[] { pshape };
        }

        // Handle Ellipse
        if (aShape instanceof Ellipse && aShape.getWidth()==aShape.getHeight()) { Ellipse elp = (Ellipse)aShape;
            CircleShape cshape = new CircleShape();
            cshape.setRadius(viewToBox(elp.getWidth()/2));
            return new org.jbox2d.collision.shapes.Shape[] { cshape };
        }

        // Handle Arc
        if (aShape instanceof Arc && aShape.getWidth()==aShape.getHeight()) { Arc arc = (Arc)aShape;
            if(arc.getSweepAngle()==360) {
                CircleShape cshape = new CircleShape();
                cshape.setRadius(viewToBox(arc.getWidth()/2));
                return new org.jbox2d.collision.shapes.Shape[] { cshape };
            }
        }

        // Handle Polygon if Simple, Convex and less than 8 points
        if (aShape instanceof Polygon) { Polygon poly = (Polygon)aShape;
            org.jbox2d.collision.shapes.Shape pshape = createShape(poly);
            if(pshape!=null) return new org.jbox2d.collision.shapes.Shape[] { pshape };
        }

        // Get shape centered around shape midpoint
        Rect bnds = aShape.getBounds();
        Shape shape = aShape.copyFor(new Transform(-bnds.width/2, -bnds.height/2));

        // Get convex Polygons for shape
        Polygon convexPolys[] = Polygon.getConvexPolys(shape, 8);
        List<org.jbox2d.collision.shapes.Shape> pshapes = new ArrayList();

        // Iterate over polygons
        for (Polygon cpoly : convexPolys) {

            // Try simple case
            org.jbox2d.collision.shapes.Shape pshp = createShape(cpoly);
            if (pshp!=null) pshapes.add(pshp);
            else System.err.println("PhysicsRunner:.createShape: failure");
        }

        // Return Box2D shapes array
        return pshapes.toArray(new org.jbox2d.collision.shapes.Shape[0]);
    }

    /**
     * Creates a Box2D shape for given snap shape.
     */
    public org.jbox2d.collision.shapes.Shape createShape(Polygon aPoly)
    {
        // If invalid, just return null
        if (!aPoly.isSimple() || !aPoly.isConvex() || aPoly.getPointCount()>8) return null;

        // Create Box2D PolygonShape and return
        int pc = aPoly.getPointCount();
        Vec2 vecs[] = new Vec2[pc]; for (int i=0;i<pc;i++) vecs[i] = viewToBox(aPoly.getX(i), aPoly.getY(i));
        PolygonShape pshape = new PolygonShape();
        pshape.set(vecs, vecs.length);
        return pshape;
    }

    /**
     * Creates a Joint.
     */
    public void createJoint(View aView)
    {
        // Get shapes interesting joint view
        ParentView editor = aView.getParent();
        List <View> hits = new ArrayList();
        Rect bnds = aView.getBoundsParent();
        for (View v : editor.getChildren()) {
            if(v!=aView && v.getBoundsLocal().intersects(v.parentToLocal(bnds)))
                hits.add(v);
        }

        // if less than two, bail
        if (hits.size()<2) {
            System.out.println("PhysicsRunner.createJoint: 2 Bodies not found for joint: " + aView.getName()); return; }
        View viewA = hits.get(0);
        View viewB = hits.get(1);

        // Create joint def and set body A/B
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = (Body)viewA.getPhysics().getNative();
        jointDef.bodyB = (Body)viewB.getPhysics().getNative();
        jointDef.collideConnected = false;

        // Set anchors
        Point jointPnt = aView.localToParent(aView.getWidth()/2, aView.getHeight()/2);
        Point jointPntA = viewA.parentToLocal(jointPnt.x, jointPnt.y);
        Point jointPntB = viewB.parentToLocal(jointPnt.x, jointPnt.y);
        jointDef.localAnchorA = viewToBoxLocal(jointPntA.x, jointPntA.y, viewA);
        jointDef.localAnchorB = viewToBoxLocal(jointPntB.x, jointPntB.y, viewB);
        RevoluteJoint joint = (RevoluteJoint)_world.createJoint(jointDef);
        aView.getPhysics(true).setNative(joint);

        // Remove view for joint
        aView.getParent(ChildView.class).removeChild(aView);
    }

    /**
     * Convert View coord to Box2D.
     */
    public float viewToBox(double aValue)  { return _runner.viewToBox(aValue); }

    /**
     * Convert View coord to Box2D.
     */
    public Vec2 viewToBox(double aX, double aY)  { return _runner.viewToBox(aX, aY); }

    /**
     * Convert Box2D coord to View.
     */
    public double boxToView(double aValue)  { return _runner.boxToView(aValue); }

    /**
     * Convert Box2D coord to View.
     */
    public Point boxToView(double aX, double aY)  { return _runner.boxToView(aX, aY); }

    /**
     * Returns transform from View coords to Box coords.
     */
    public Transform getViewToBox()  { return _runner.getViewToBox(); }

    /**
     * Returns point in view coords.
     */
    private Vec2 viewToBoxLocal(double aX, double aY, View aView)  { return _runner.viewToBoxLocal(aX, aY, aView); }
}
