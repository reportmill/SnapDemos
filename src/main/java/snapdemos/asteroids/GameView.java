/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapdemos.asteroids;
import snap.geom.Point;
import snap.gfx.Color;
import snap.util.ArrayUtils;
import snap.view.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This view class paints the game background and manages child actor views.
 */
public class GameView extends ChildView {

    // Whether mouse was clicked on this frame
    private ViewEvent  _mouseClicked;

    // The pressed key
    private Set<Integer>  _keyDowns = new HashSet<>();

    // The runnable to process next frame
    private Runnable _processNextFrameRun;

    // Constant for frame delay
    private static final int FRAME_DELAY = 40;

    /**
     * Constructor.
     */
    public GameView()
    {
        super();
        setFill(Color.WHITE);
        setBorder(Color.BLACK, 1);
        enableEvents(MouseEvents);
        enableEvents(KeyEvents);
        setFocusable(true);
        setFocusWhenPressed(true);
    }

    /**
     * Adds the given actor to scene.
     */
    public void addActor(ActorView anActor)
    {
        addChild(anActor);
    }

    /**
     * Removes an actor.
     */
    public ActorView removeActor(int anIndex)
    {
        return (ActorView) removeChild(anIndex);
    }

    /**
     * Removes an actor.
     */
    public int removeActor(ActorView anActor)
    {
        int index = indexOfChild(anActor);
        if (index >= 0) removeActor(index);
        return index;
    }

    /**
     * Adds the given actor to scene at given XY.
     */
    public void addActorAtXY(ActorView anActor, double aX, double aY)
    {
        addActor(anActor);
        anActor.setCenterXY(aX, aY);
    }

    /**
     * Returns the actor with given name.
     */
    public ActorView getActorForName(String aName)
    {
        View child = getChildForName(aName);
        return child instanceof ActorView ? (ActorView) child : null;
    }

    /**
     * Returns the scene actor intersecting given point in local coords.
     */
    public <T extends ActorView> T getActorAtXY(double aX, double aY, Class<T> aClass)
    {
        for (View child : getChildren()) {
            if (!(child instanceof ActorView)) continue;
            if (aClass == null || aClass.isInstance(child)) {
                Point point = child.parentToLocal(aX, aY);
                if (child.contains(point.getX(), point.getY()))
                    return (T) child;
            }
        }

        // Return not found
        return null;
    }

    /**
     * Returns the scene actor intersecting given point in local coords.
     */
    public <T extends ActorView> List<T> getActorsAtXY(double aX, double aY, Class<T> aClass)
    {
        List<T> actorsAtXY = new ArrayList<>();

        for (View child : getChildren()) {
            if (!(child instanceof ActorView)) continue;
            if (aClass == null || aClass.isInstance(child)) {
                Point point = child.parentToLocal(aX, aY);
                if (child.contains(point.getX(), point.getY()))
                    actorsAtXY.add((T) child);
            }
        }

        // Return
        return actorsAtXY;
    }

    /**
     * Returns the actors for class.
     */
    public <T> T[] getActorsForClass(Class<T> aClass)
    {
        View[] children = getChildren();
        return ArrayUtils.mapNonNull(children, child -> aClass.isInstance(child) ? (T) child : null, aClass);
    }

    /**
     * Returns whether the mouse was clicked on this frame.
     */
    public boolean isMouseClicked()  { return _mouseClicked != null; }

    /**
     * Returns whether a given key is pressed.
     */
    public boolean isKeyDown(String aKey)
    {
        int kp = KeyCode.get(aKey.toUpperCase());
        return _keyDowns.contains(kp);
    }

    /**
     * Whether game is playing.
     */
    public boolean isPlaying()  { return _processNextFrameRun != null; }

    /**
     * Starts the animation.
     */
    public void startAnim()
    {
        if (_processNextFrameRun == null) {
            _processNextFrameRun = this::processNextFrame;
            ViewUtils.runDelayed(() -> getEnv().runIntervals(_processNextFrameRun, FRAME_DELAY), 800);
        }
    }

    /**
     * Stops the animation.
     */
    public void stopAnim()
    {
        if (_processNextFrameRun != null) {
            getEnv().stopIntervals(_processNextFrameRun);
            _processNextFrameRun = null;
        }
    }

    /**
     * Override to start/stop animation.
     */
    protected void setShowing(boolean aValue)
    {
        // Do normal version
        if (aValue == isShowing()) return;
        super.setShowing(aValue);

        // IF showing, start, otherwise, stop
        if (aValue)
            startAnim();
        else stopAnim();
    }

    /**
     * Process event.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseEvent
        if (anEvent.isMouseEvent()) {
            if (anEvent.isMouseRelease()) {
                if (anEvent.isMouseClick())
                    _mouseClicked = anEvent;
            }
        }

        // Handle KeyEvent: Update KeyDowns and KeyClicks for event
        else if (anEvent.isKeyEvent()) {
            int keyCode = anEvent.getKeyCode();
            if (anEvent.isKeyPress())
                _keyDowns.add(keyCode);
            else if (anEvent.isKeyRelease())
                _keyDowns.remove(keyCode);
        }
    }

    /**
     * Calls the act method and actors act methods.
     */
    protected void processNextFrame()
    {
        act();

        View[] children = getChildren();
        for (View child : children) {
            if (child instanceof ActorView && child.getParent() != null)
                ((ActorView) child).act();
        }

        _mouseClicked = null;
    }

    /**
     * The act method.
     */
    protected void act()  { }
}