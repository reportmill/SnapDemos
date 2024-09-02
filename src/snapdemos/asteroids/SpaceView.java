package snapdemos.asteroids;
import snap.geom.Ellipse;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.util.SnapUtils;
import snap.view.Label;
import snap.view.ScaleBox;
import snap.view.ViewOwner;
import snap.view.ViewUtils;
import java.util.Random;

/**
 * This class holds the main game view.
 */
public class SpaceView extends GameView
{
    // Whether the game is running
    private boolean _started;

    // The background space view
    private Image _backgroundImage;

    // Constants
    private static final int GAME_WIDTH = 900;
    private static final int GAME_HEIGHT = 650;
    private static final int INITIAL_ASTEROID_COUNT = 4;
    private static final int BACKGROUND_STAR_COUNT = 300;

    /**
     * Constructor.
     */
    public SpaceView()
    {
        super();

        // Set size
        setSize(GAME_WIDTH, GAME_HEIGHT);
        setPrefSize(getSize());
        setClipToBounds(true);

        // Create background
        createBackground();

        // Start game
        startGame();
    }

    /**
     * Starts the game.
     */
    public void startGame()
    {
        // Remove all children
        removeChildren();

        // Create and add rocket
        Rocket rocket = new Rocket();
        addActorAtXY(rocket, getWidth() / 2 + 100, getHeight() / 2);

        // Create and add asteroids
        for(int i = 0; i < INITIAL_ASTEROID_COUNT; i++) {
            Asteroid asteroid = new Asteroid();
            int x = new Random().nextInt((int) getWidth() / 2);
            int y = new Random().nextInt((int) getHeight() / 2);
            addActorAtXY(asteroid, x, y);
        }

        // Set started true
        _started = true;
    }

    /**
     * Override to see if game needs restart.
     */
    @Override
    protected void act()
    {
        if (!_started && (isMouseClicked() || isKeyDown("space")))
            ViewUtils.runDelayed(() -> startGame(), 100);
    }

    /**
     * Called when game is over.
     */
    protected void gameOver()
    {
        _started = false;

        // Create 'Game Over' label and animate
        Label label = new Label("Game Over");
        label.setPropValues(Font_Prop, "Arial Bold 72", Opacity_Prop, 0);
        label.setTextColor(Color.MAGENTA);
        label.setSize(label.getPrefSize());
        label.setScale(.1);
        addChild(label);
        label.setManaged(false);
        label.setLean(Pos.CENTER);
        label.getAnim(1000).getAnim(1000 + 1200).setScale(1).setOpacity(1).setRotate(360).play();
        getEnv().runDelayed(this::stopAnim, 2200);
    }

    /**
     * Create background.
     */
    private void createBackground()
    {
        // Create image for game field
        _backgroundImage = Image.getImageForSize(GAME_WIDTH, GAME_HEIGHT, false);

        // Paint black
        Painter pntr = _backgroundImage.getPainter();
        pntr.setColor(Color.BLACK);
        pntr.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        // Add stars
        Random random = new Random();
        Ellipse oval = new Ellipse(0, 0, 3, 3);
        for(int i = 0; i < BACKGROUND_STAR_COUNT; i++) {
            int x = random.nextInt(GAME_WIDTH);
            int y = random.nextInt(GAME_HEIGHT);
            oval.setXY(x, y);
            int color = 120 - random.nextInt(100);
            pntr.setColor(new Color(color,color,color));
            pntr.fill(oval);
        }
    }

    /**
     * Paint background.
     */
    @Override
    protected void paintBack(Painter aPntr)
    {
        aPntr.drawImage(_backgroundImage, 0, 0);
    }

    /**
     * Standard main implementation.
     */
    public static void main(String[] args)
    {
        ViewUtils.runLater(() -> mainLater(args));
    }

    /**
     * Standard main implementation.
     */
    private static void mainLater(String[] args)
    {
        SpaceView spaceView = new SpaceView();
        ScaleBox scaleBox = new ScaleBox(spaceView, true, true);
        ViewOwner viewOwner = new ViewOwner(scaleBox);
        viewOwner.setFirstFocus(spaceView);
        viewOwner.getWindow().setMaximized(SnapUtils.isWebVM);
        viewOwner.setWindowVisible(true);
    }
}