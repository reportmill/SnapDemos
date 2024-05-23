package snapdemos.snappybird;
import java.util.Random;
import snap.geom.*;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;
import snap.viewx.Explode;

/**
 * A Flappy-Bird app.
 */
public class SnappyBird extends ViewOwner {

    // The main view
    private ChildView _mainView;

    // The flappy view
    private ImageView _flappyView;

    // The view that holds pipes (top/bottom base + caps)
    private ColView _pipesView;

    // The time at last frame
    private long _lastFrameTime;

    // The current vertical speed of flappy (points / sec)
    private double _flappySpeedY;

    // The acceleration of gravity (points / sec^2)
    private double GRAVITY = 75;

    // The image for the pipe
    Image FLAPPY_IMAGE = Image.getImageForClassResource(getClass(), "Snappy.png");
    Image PIPE_IMAGE = Image.getImageForClassResource(getClass(), "SnappyPipe.png");

    /**
     * Constructor.
     */
    public SnappyBird()
    {
        super();
    }

    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Create main view
        _mainView = new ChildView();
        _mainView.setPrefSize(800, 600);
        _mainView.setFill(new Color("#EEFFFF"));
        _mainView.setClipToBounds(true);
        _mainView.setBorder(Color.GRAY, 1);
        _mainView.addEventHandler(e -> flap(), MousePress);
        _mainView.addEventHandler(e -> flap(), KeyPress);
        _mainView.setFocusable(true);
        setFirstFocus(_mainView);

        // Create Flappy Image and ImageView and add
        _flappyView = new ImageView(FLAPPY_IMAGE);
        _flappyView.setBounds(180, 220, 60, 45);
        _mainView.addChild(_flappyView);

        // Create ScaleBox to work with small window sizes
        ScaleBox scaleBox = new ScaleBox(_mainView, true, true);

        // Return
        return scaleBox;
    }

    /**
     * Called when MainView is showing.
     */
    @Override
    protected void initShowing()
    {
        // Create the pipes view and add to MainView
        _pipesView = createPipesView();
        _mainView.addChild(_pipesView);

        // Reset pipe heights and animation
        resetPipesViewGapAndAnimation();

        // Get start time and start calling update frame method
        _lastFrameTime = System.currentTimeMillis();
        getEnv().runIntervals(this::updateFrame, 25);
    }

    /**
     * Creates the view that holds top/bottom pipes.
     */
    private ColView createPipesView()
    {
        // Create 4 pipe parts (top base+cap and bottom base+cap)
        View topPipeBase = createPipePart();
        topPipeBase.setPrefWidth(64);
        View topPipeCap = createPipePart();
        topPipeCap.setPrefSize(90, 40);

        // Create bottom pipe parts (cap and base)
        View bottomPipeCap = createPipePart();
        bottomPipeCap.setPrefSize(90, 40);
        bottomPipeCap.setLeanY(VPos.BOTTOM);
        View bottomPipeBase = createPipePart();
        bottomPipeBase.setPrefWidth(64);

        // Create col view to hold pipe parts
        ColView pipesView = new ColView();
        pipesView.setAlign(Pos.TOP_CENTER);
        pipesView.setPrefHeight(_mainView.getPrefHeight());
        pipesView.setChildren(topPipeBase, topPipeCap, bottomPipeCap, bottomPipeBase);
        pipesView.setSize(pipesView.getBestSize());

        // Return
        return pipesView;
    }

    /**
     * Creates a pipe part.
     */
    private View createPipePart()
    {
        ImageView imageView = new ImageView(PIPE_IMAGE, true, true);
        imageView.setBorder(new Color("#008000"), 2);
        return imageView;
    }

    /**
     * Resets the pipes view pipe gap and animation.
     */
    private void resetPipesViewGapAndAnimation()
    {
        // Reset top/bottom pipe base heights by random offset
        double heightOffset = new Random().nextInt(3) * 100 - 100;
        _pipesView.getChild(0).setPrefHeight(200 + heightOffset);
        _pipesView.getChild(3).setPrefHeight(200 - heightOffset);

        // Reset pipe location
        double backgroundW = _mainView.getPrefWidth();
        _pipesView.setX(backgroundW);

        // Reset pipe animation (from right side of screen to off-screen left)
        ViewAnim anim = _pipesView.getAnimCleared(4000);
        anim.setX(-_pipesView.getWidth());
        anim.setOnFinish(() -> runLater(() -> resetPipesViewGapAndAnimation()));
        anim.play();
    }

    /**
     * Called on MousePress on main view.
     */
    private void flap()
    {
        _flappySpeedY -= 60;
        if (_flappySpeedY < -80)
            _flappySpeedY = -80;
    }

    /**
     * Called repeatedly to update frame.
     */
    private void updateFrame()
    {
        // Get time change since last frame update
        long time = System.currentTimeMillis();
        double dt = (time - _lastFrameTime) / 1000d;
        _lastFrameTime = time;

        // If flappy exploding, just return
        if (_flappyView.getOpacity() != 1)
            return;

        // Calculate change in flappy y and update: distY = accelY * time ^ 2 + speedY * time
        double dy = _flappySpeedY * dt;
        double newY = _flappyView.getY() + dy;
        if (newY > _mainView.getHeight() - 100)
            newY = _mainView.getHeight() - 100;
        _flappyView.setY(newY);

        // Update velocity
        _flappySpeedY += GRAVITY * dt;
        if (_flappySpeedY > 80)
            _flappySpeedY = 80;

        // Update rotation
        _flappyView.setRotate(_flappySpeedY / 80 * 30);

        // If flappy hit pipe, explode and reset attributes
        ViewList viewList = _mainView.getViewList();
        View viewHitByFlappyView = viewList.getHitView(_flappyView, null, 8);
        if (viewHitByFlappyView != null) {
            new Explode(_flappyView, 20, 20, () -> explodeDone()).play();
            _flappySpeedY = 0;
            _flappyView.setRotate(0);
            _flappyView.setY(250);
        }
    }

    /**
     * Called when explode is done.
     */
    private void explodeDone()
    {
        _flappyView.getAnimCleared(800).setOpacity(1).play();
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)
    {
        ViewUtils.runLater(SnappyBird::mainLater);
    }

    /**
     * Standard main method on event thread.
     */
    public static void mainLater()
    {
        SnappyBird snappyBird = new SnappyBird();
        snappyBird.getWindow().setMaximized(SnapUtils.isWebVM);
        snappyBird.setWindowVisible(true);
    }
}