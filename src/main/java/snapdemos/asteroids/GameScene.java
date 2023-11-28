/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapdemos.asteroids;
import snap.geom.Point;
import snap.gfx.Color;
import snap.view.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The parent and background for SnapActors.
 */
public class GameScene extends ChildView {

    // Whether mouse was clicked on this frame
    private ViewEvent  _mouseClicked;

    // The pressed key
    private Set<Integer>  _keyDowns = new HashSet<>();

    // The animation timer
    private ViewTimer  _timer = new ViewTimer(FRAME_DELAY, t -> processNextFrame());

    // Constant for frame delay
    private static final int FRAME_DELAY = 40;

    /**
     * Create new SnapScene.
     */
    public GameScene()
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
    public void addActor(GameActor anActor)
    {
        addChild(anActor);
    }

    /**
     * Removes an actor.
     */
    public GameActor removeActor(int anIndex)
    {
        return (GameActor) removeChild(anIndex);
    }

    /**
     * Removes an actor.
     */
    public int removeActor(GameActor anActor)
    {
        int index = indexOfChild(anActor);
        if (index >= 0) removeActor(index);
        return index;
    }

    /**
     * Adds the given actor to scene at given XY.
     */
    public void addActorAtXY(GameActor anActor, double aX, double aY)
    {
        addActor(anActor);
        anActor.setCenterXY(aX, aY);
    }

    /**
     * Returns the actor with given name.
     */
    public GameActor getActorForName(String aName)
    {
        View child = getChildForName(aName);
        return child instanceof GameActor ? (GameActor) child : null;
    }

    /**
     * Returns the scene actor intersecting given point in local coords.
     */
    public <T extends GameActor> T getActorAtXY(double aX, double aY, Class<T> aClass)
    {
        for (View child : getChildren()) {
            if (!(child instanceof GameActor)) continue;
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
    public <T extends GameActor> List<T> getActorsAtXY(double aX, double aY, Class<T> aClass)
    {
        List<T> actorsAtXY = new ArrayList<>();

        for (View child : getChildren()) {
            if (!(child instanceof GameActor)) continue;
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
     * Starts the animation.
     */
    public void start()
    {
        _timer.start(800);
    }

    /**
     * Stops the animation.
     */
    public void stop()
    {
        _timer.stop();
    }

    /**
     * Whether scene is playing.
     */
    public boolean isPlaying()
    {
        return _timer.isRunning();
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
            start();
        else stop();
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
            if (child instanceof GameActor && child.getParent() != null)
                ((GameActor) child).act();
        }

        _mouseClicked = null;
    }

    /**
     * The act method.
     */
    protected void act()  { }
}