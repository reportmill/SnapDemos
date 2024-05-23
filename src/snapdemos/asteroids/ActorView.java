/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapdemos.asteroids;
import snap.geom.Point;
import snap.geom.Vector;
import snap.view.ImageView;
import snap.view.View;

/**
 * This view class is the base class for individual elements of a game and manages painting, movement and collisions.
 */
public class ActorView extends ImageView {

    // The velocity vector
    private Vector _velocity;

    // Whether to wrap edges
    protected boolean _wrapEdges = true;

    // The exact X/Y
    private double _exactX;
    private double _exactY;

    /**
     * Constructor.
     */
    public ActorView()
    {
        _velocity = new Vector();
    }

    /**
     * Returns the scene that holds this actor.
     */
    public GameView getScene()  { return getParent(GameView.class); }

    /**
     * Returns the velocity vector.
     */
    public Vector getVelocity()  { return _velocity; }

    /**
     * Sets the velocity vector.
     */
    public void setVelocity(Vector aVector)
    {
        _velocity.setXY(aVector);
    }

    /**
     * Adds the given velocity vector to this actor's velocity.
     */
    public void addVelocityVector(Vector velocityVector)
    {
        _velocity.add(velocityVector);
    }

    /**
     * Returns the center X.
     */
    public double getCenterX()
    {
        return getX() + getWidth() / 2;
    }

    /**
     * Returns the center Y.
     */
    public double getCenterY()
    {
        return getY() + getHeight() / 2;
    }

    /**
     * Sets the actor center to given XY.
     */
    public void setCenterXY(double aX, double aY)
    {
        Point centerXY = localToParent(getWidth() / 2, getHeight() / 2);
        double newX = getX() + aX - centerXY.x;
        double newY = getY() + aY - centerXY.y;
        setXY(newX, newY);
    }

    /**
     * Move actor for current velocity. If moved out of scene, wrap to opposite edge.
     */
    public void move()
    {
        // Get new XY for velocity
        double newX = _exactX + _velocity.x;
        double newY = _exactY + _velocity.y;

        // Wrap to opposite edge if out of bounds
        if (_wrapEdges) {
            GameView scene = getScene();
            double sceneW = scene.getWidth();
            double sceneH = scene.getHeight();
            if (newX >= sceneW) newX = 0;
            if (newX < 0) newX = sceneW - 1;
            if (newY >= sceneH) newY = 0;
            if (newY < 0) newY = sceneH - 1;
        }

        // Set XY
        setXY(newX, newY);
    }

    /**
     * Override to set exact X/Y.
     */
    @Override
    public void setXY(double aX, double aY)
    {
        _exactX = aX;
        _exactY = aY;
        super.setXY((int) aX, (int) aY);
    }

    /**
     * Override to constrain rotation to 0 - 360.
     */
    @Override
    public void setRotate(double theDegrees)
    {
        if (theDegrees < 0)
            theDegrees += 360;
        else if (theDegrees > 360)
            theDegrees -= 360;
        super.setRotate(theDegrees);
    }

    /**
     * Returns the first actor in range.
     */
    public <T extends ActorView> T getActorInRange(Class<T> aClass)
    {
        double thisRadius = getWidth() / 2;

        // Iterate over scene children (from front to back)
        for (int i = getScene().getChildCount() - 1; i >= 0; i--) {
            View child = getScene().getChild(i);

            // If child is of class, check distance
            if (aClass == null || aClass.isInstance(child)) {
                double thatRadius = child.getWidth();
                double radiusAll = thisRadius + thatRadius;
                if (getDistance((ActorView) child) <= radiusAll)
                    return (T) child;
            }
        }

        // Return not found
        return null;
    }

    /**
     * Returns the distance to the given actor.
     */
    public double getDistance(ActorView anActor)
    {
        return getDistance(anActor.getCenterX(), anActor.getCenterY());
    }

    /**
     * Returns the distance from actor center to given point.
     */
    public double getDistance(double x2, double y2)
    {
        double x1 = getCenterX();
        double y1 = getCenterY();
        double dx = x2 - x1, dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * The act method.
     */
    protected void act()  { }
}