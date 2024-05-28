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
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = phys.isDynamic() ? BodyType.DYNAMIC : BodyType.KINEMATIC;
        bodyDef.position.set(viewToBox(aView.getMidX(), aView.getMidY()));
        bodyDef.angle = (float) Math.toRadians(-aView.getRotate());
        bodyDef.linearDamping = 10;
        bodyDef.angularDamping = 10;

        // Create Body
        Body body = _world.createBody(bodyDef);

        // Create PolygonShape
        Shape viewShape = aView.getBoundsShape();
        org.jbox2d.collision.shapes.Shape[] jboxShapes = createJboxShapesForShape(viewShape);

        // Create FixtureDef
        for (org.jbox2d.collision.shapes.Shape jboxShape : jboxShapes) {
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = jboxShape;
            fixtureDef.restitution = .25f;
            fixtureDef.density = (float)phys.getDensity();
            fixtureDef.filter.groupIndex = phys.getGroupIndex();
            body.createFixture(fixtureDef);
        }

        // Return body
        phys.setNative(body);
        body.setUserData(aView);
        return body;
    }

    /**
     * Creates a Box2D shape for given snap shape.
     */
    public org.jbox2d.collision.shapes.Shape[] createJboxShapesForShape(Shape aShape)
    {
        // Handle Rect (simple case)
        if (aShape instanceof Rect) {
            Rect rect = (Rect) aShape;
            PolygonShape polygonShape = new PolygonShape();
            float pw = viewToBox(rect.width / 2);
            float ph = viewToBox(rect.height / 2);
            polygonShape.setAsBox(pw, ph);
            return new org.jbox2d.collision.shapes.Shape[] { polygonShape };
        }

        // Handle Ellipse
        if (aShape instanceof Ellipse && aShape.getWidth()==aShape.getHeight()) {
            Ellipse ellipse = (Ellipse) aShape;
            CircleShape circleShape = new CircleShape();
            circleShape.setRadius(viewToBox(ellipse.getWidth() / 2));
            return new org.jbox2d.collision.shapes.Shape[] { circleShape };
        }

        // Handle Arc
        if (aShape instanceof Arc && aShape.getWidth()==aShape.getHeight()) {
            Arc arc = (Arc) aShape;
            if (arc.getSweepAngle() == 360) {
                CircleShape cshape = new CircleShape();
                cshape.setRadius(viewToBox(arc.getWidth()/2));
                return new org.jbox2d.collision.shapes.Shape[] { cshape };
            }
        }

        // Handle Polygon if Simple, Convex and less than 8 points
        if (aShape instanceof Polygon) {
            Polygon poly = (Polygon) aShape;
            org.jbox2d.collision.shapes.Shape pshape = createShape(poly);
            if(pshape!=null) return new org.jbox2d.collision.shapes.Shape[] { pshape };
        }

        // Get shape centered around shape midpoint
        Rect shapeBounds = aShape.getBounds();
        Shape shape = aShape.copyFor(new Transform(-shapeBounds.width / 2, -shapeBounds.height / 2));

        // Get convex Polygons for shape
        Polygon[] convexPolys = Polygon.getConvexPolygonsWithMaxSideCount(shape, 8);
        List<org.jbox2d.collision.shapes.Shape> pshapes = new ArrayList<>();

        // Iterate over polygons
        for (Polygon convexPoly : convexPolys) {

            // Try simple case
            org.jbox2d.collision.shapes.Shape pshp = createShape(convexPoly);
            if (pshp != null)
                pshapes.add(pshp);
            else System.err.println("PhysicsRunner.createShape: failure");
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
        if (aPoly.isSelfIntersecting() || !aPoly.isConvex() || aPoly.getPointCount() > 8) return null;

        // Create Box2D PolygonShape and return
        int pointCount = aPoly.getPointCount();
        Vec2[] vecs = new Vec2[pointCount];
        for (int i = 0; i < pointCount; i++)
            vecs[i] = viewToBox(aPoly.getPointX(i), aPoly.getPointY(i));
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.set(vecs, vecs.length);
        return polygonShape;
    }

    /**
     * Creates a Joint.
     */
    public void createJoint(View aView)
    {
        // Get shapes interesting joint view
        ParentView editor = aView.getParent();
        List <View> hits = new ArrayList<>();
        Rect bnds = aView.getBoundsParent();
        for (View v : editor.getChildren()) {
            if(v != aView && v.getBoundsLocal().intersectsShape(v.parentToLocal(bnds)))
                hits.add(v);
        }

        // if less than two, bail
        if (hits.size() < 2) {
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
