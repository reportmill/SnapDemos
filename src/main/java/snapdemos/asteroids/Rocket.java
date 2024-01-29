package snapdemos.asteroids;
import snap.geom.Point;
import snap.geom.Vector;
import snap.gfx.Image;
import snap.view.ViewUtils;
import snap.viewx.Explode;

/**
 * This class models the asteroids rocket.
 */
public class Rocket extends ActorView
{
    // Whether exploding
    private boolean _exploding;

    // A frame count down counter to delay firing
    private int _reloadDelayCounter = 0;

    // Rocket images
    private Image rocket = Image.getImageForClassResource(Rocket.class, "resources/Rocket.png");
    private Image rocketWithThrust = Image.getImageForClassResource(Rocket.class, "resources/RocketWithThrust.png");

    /**
     * Constructor.
     */
    public Rocket()
    {
        super();
        setSize(106, 105);
        setImage(rocket);

        // Give rocket a slight drift
        setVelocity(Vector.getVectorForAngleAndLength(13, 0.3));
    }

    /**
     * Sets whether the rocket is thrusting.
     */
    private void setThrusting(boolean aValue)
    {
        // If on, set image and add velocity
        if (aValue) {
            setImage(rocketWithThrust);
            addVelocityVector(Vector.getVectorForAngleAndLength(getRotate(), 0.3));
        }

        // If off, set image
        else setImage(rocket);
    }

    /**
     * Override to move, process user input and handle collisions.
     */
    @Override
    public void act()
    {
        // If already exploding, just return
        if (_exploding) return;

        // Move, handle keys and collisions
        move();
        handleKeys();
        checkCollision();
        _reloadDelayCounter--;
    }
    
    /**
     * Handle user input keys.
     */
    private void handleKeys()
    {
        // Handle thrust key (up)
        GameView scene = getScene();
        boolean isThrusting = scene.isKeyDown("up");
        setThrusting(isThrusting);

        // Handle rotate left/right
        if (scene.isKeyDown("left"))
            setRotate(getRotate() - 5);
        if (scene.isKeyDown("right"))
            setRotate(getRotate() + 5);

        // Handle fire
        if (scene.isKeyDown("space"))
            fire();
    }
    
    /**
     * Check and handle collisions with asteroids.
     */
    private void checkCollision() 
    {
        // If already exploding, just return
        if (_exploding) return;

        // If asteroid is hit, trigger explosion
        Asteroid hitAsteroid = getActorInRange(Asteroid.class);
        if (hitAsteroid != null) {
            _exploding = true;
            new Explode(this, 30, 30, () -> explosionFinished()).setRunTime(4000).play();
            ViewUtils.runDelayed(() -> new Explode(this, 30, 30, null).setRunTime(2000).play(), 100);
            hitAsteroid.hit(100);
        }
    }

    /**
     * Called when explosion is done to remove rocket and end game.
     */
    private void explosionFinished()
    {
        SpaceView spaceView = (SpaceView) getScene();
        spaceView.removeActor(this);
        spaceView.endGame();
    }
    
    /**
     * Fire a bullet.
     */
    private void fire() 
    {
        if (_reloadDelayCounter <= 0) {

            // Create new bullet
            Vector bulletVector = getVelocity().clone();
            Bullet bullet = new Bullet(bulletVector, (int) getRotate());

            // Get bullet point at tip of rocket
            Point bulletPoint = localToParent(getWidth(), getHeight() / 2);

            // Add bullet to scene, move a bit and play sound
            GameView scene = getScene();
            scene.addActorAtXY(bullet, bulletPoint.x, bulletPoint.y);
            bullet.move();
            bullet.playSound();
            _reloadDelayCounter = 5;
        }
    }
}