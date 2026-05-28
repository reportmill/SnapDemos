package snapdemos.asteroids;
import snap.games.ActorX;
import snap.games.Stage;
import snap.geom.Point;
import snap.geom.Vector;
import snap.gfx.Image;
import snap.view.ViewUtils;
import snap.viewx.Explode;

/**
 * This class models the asteroids rocket.
 */
public class Rocket extends ActorX
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
        setWrapAtEdges(true);

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
            addVelocity(0.3);
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
        super.act();
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
        Stage stage = getStage();
        boolean isThrusting = stage.isKeyDown("up");
        setThrusting(isThrusting);

        // Handle rotate left/right
        if (stage.isKeyDown("left"))
            setRotate(getRotate() - 5);
        if (stage.isKeyDown("right"))
            setRotate(getRotate() + 5);

        // Handle fire
        if (stage.isKeyDown("space"))
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
        Asteroid hitAsteroid = getHitActorForClass(Asteroid.class);
        if (hitAsteroid != null) {
            _exploding = true;
            new Explode(getActorView(), 30, 30, () -> explosionFinished()).setRunTime(4000).play();
            ViewUtils.runDelayed(this::explodeRocket, 100);
            hitAsteroid.hit(100);
        }
    }

    /**
     * Called to explode rocket.
     */
    public void explodeRocket()
    {
        Explode explode = new Explode(getActorView(), 30, 30);
        explode.setRunTime(2000);
        explode.play();
    }

    /**
     * Called when explosion is done to remove rocket and end game.
     */
    private void explosionFinished()
    {
        SpaceView spaceView = getStage(SpaceView.class);
        spaceView.removeActor(this);
        spaceView.gameOver();
    }
    
    /**
     * Fire a bullet.
     */
    private void fire() 
    {
        if (_reloadDelayCounter <= 0) {

            // Create new bullet
            Vector bulletVector = getVelocity();
            Bullet bullet = new Bullet(bulletVector, (int) getRotate());

            // Get bullet point at tip of rocket
            Point bulletPoint = getActorView().localToParent(getWidth(), getHeight() / 2);

            // Add bullet to stage, move a bit and play sound
            Stage stage = getStage();
            stage.addActorAtXY(bullet, bulletPoint.x, bulletPoint.y);
            bullet.act();
            bullet.playSound();
            _reloadDelayCounter = 5;
        }
    }
}