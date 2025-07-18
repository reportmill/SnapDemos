package snapdemos.jbox2d;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import snap.geom.*;
import snap.util.ListUtils;
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

    // The Jbox world
    private World _world;

    /**
     * Constructor for given runner.
     */
    public PhysicsBuilder(PhysicsRunner aRunner)
    {
        _runner = aRunner;
        _world = aRunner._world;
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

        // Return body
        phys.setNative(body);
        body.setUserData(aView);
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
        if (aPoly.isSelfIntersecting() || !aPoly.isConvex() || aPoly.getPointCount() > 8) return null;

        // Create Box2D PolygonShape and return
        int pointCount = aPoly.getPointCount();
        Vec2[] vecs = new Vec2[pointCount];
        for (int i = 0; i < pointCount; i++)
            vecs[i] = convertViewXYToJbox(aPoly.getPointX(i), aPoly.getPointY(i));
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.set(vecs, vecs.length);
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
        jointDef.localAnchorA = convertViewXYToJboxLocal(jointPntA.x, jointPntA.y, viewA);
        jointDef.localAnchorB = convertViewXYToJboxLocal(jointPntB.x, jointPntB.y, viewB);
        return (RevoluteJoint) _world.createJoint(jointDef);
    }

    /**
     * Creates a Joint.
     */
    public void createJboxJointForViewAndSet(View aView)
    {
        // Create joint and add to view
        RevoluteJoint joint = createJboxJointForView(aView);
        aView.getPhysics(true).setNative(joint);

        // Remove view for joint
        aView.getParent(ChildView.class).removeChild(aView);
    }

    /**
     * Convert View coord to Box2D.
     */
    public float convertViewCoordToJbox(double aValue)  { return _runner.convertViewCoordToJbox(aValue); }

    /**
     * Convert View coord to Box2D.
     */
    public Vec2 convertViewXYToJbox(double aX, double aY)  { return _runner.convertViewXYToJbox(aX, aY); }

    /**
     * Convert Box2D coord to View.
     */
    public double convertJboxCoordToView(double aValue)  { return _runner.convertJboxCoordToView(aValue); }

    /**
     * Convert Box2D coord to View.
     */
    public Point convertJboxXYToView(double aX, double aY)  { return _runner.convertJboxXYToView(aX, aY); }

    /**
     * Returns transform from View coords to Box coords.
     */
    public Transform getViewToBoxTransform()  { return _runner.getViewToBoxTransform(); }

    /**
     * Returns point in view coords.
     */
    private Vec2 convertViewXYToJboxLocal(double aX, double aY, View aView)  { return _runner.convertViewXYToJboxLocal(aX, aY, aView); }
}
