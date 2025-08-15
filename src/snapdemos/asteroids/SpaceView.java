package snapdemos.asteroids;
import snap.games.Game;
import snap.games.GameView;
import snap.geom.Ellipse;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.view.Label;
import java.util.Random;

/**
 * This class holds the main game view.
 */
public class SpaceView extends GameView
{
    // Whether the game is running
    private boolean _started;

    // Constants
    private static final int GAME_WIDTH = 700;
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
        setImage(createBackground());

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
    protected void stepGameFrame()
    {
        super.stepGameFrame();
        if (!_started && (isMouseClicked() || isKeyDown("space")))
            runDelayed(this::startGame, 100);
    }

    /**
     * Called when game is over.
     */
    protected void gameOver()
    {
        _started = false;

        // Create 'Game Over' label and animate
        Label label = new Label("Game Over");
        label.setPropsString("Font:Arial Bold 72; TextColor: MAGENTA; Opacity:0");
        label.setSizeToPrefSize();
        label.setManaged(false);
        label.setLean(Pos.CENTER);
        label.setScale(.1);
        addChild(label);
        label.getAnim(1000).getAnim(1000 + 1200).setScale(1).setOpacity(1).setRotate(360).play();
        getEnv().runDelayed(this::stop, 2200);
    }

    /**
     * Create background image with random stars.
     */
    private Image createBackground()
    {
        // Create image for game field
        Image backgroundImage = Image.getImageForSize(GAME_WIDTH, GAME_HEIGHT, false);

        // Paint black
        Painter pntr = backgroundImage.getPainter();
        pntr.setColor(Color.BLACK);
        pntr.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        // Add stars
        Random random = new Random();
        Ellipse starShape = new Ellipse(0, 0, 3, 3);
        for(int i = 0; i < BACKGROUND_STAR_COUNT; i++) {
            int starX = random.nextInt(GAME_WIDTH);
            int starY = random.nextInt(GAME_HEIGHT);
            starShape.setXY(starX, starY);
            int color = 120 - random.nextInt(100);
            pntr.setColor(new Color(color, color, color));
            pntr.fill(starShape);
        }

        // Return
        return backgroundImage;
    }

    /**
     * Standard main implementation.
     */
    public static void main(String[] args)  { Game.showGameForClass(SpaceView.class); }
}