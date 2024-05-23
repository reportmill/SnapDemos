package snapdemos.asteroids;
import snap.geom.Vector;
import snap.gfx.Color;
import snap.gfx.SoundClip;

/**
 * This class models a bullet.
 */
public class Bullet extends ActorView
{
    // The amount of life left in a bullet (disappears at zero)
    private int _life = 50;

    // The amount of damage bullet take on asteroid
    private static final int BULLET_DAMAGE = 16;

    // The sound of bullet firing
    private static SoundClip BULLET_SOUND = SoundClip.get(Bullet.class, "resources/Bullet.wav");

    /**
     * Constructor.
     */
    public Bullet(Vector speed, int rotation)
    {
        super();
        setFill(Color.get("#EE7F42"));
        setSize(8, 3);
        _wrapEdges = false;

        // Init rotation and velocity
        setRotate(rotation);
        setVelocity(speed);
        addVelocityVector(Vector.getVectorForAngleAndLength(rotation, 15));
    }

    /**
     * Plays sound of bullet firing.
     */
    public void playSound()  { BULLET_SOUND.play(); }

    /**
     * Override to move bullet or remove it when out of time.
     */
    public void act()
    {
        // If bullet out of time, remove and return
        if(_life <= 0) {
            getScene().removeActor(this);
            return;
        }

        // Move bullet and check collisions
        move();
        checkAsteroidHit();
        _life--;
    }
    
    /**
     * Check and handle collisions with asteroids.
     */
    private void checkAsteroidHit()
    {
        Asteroid asteroid = getActorInRange(Asteroid.class);
        if (asteroid != null){
            getScene().removeActor(this);
            asteroid.hit(BULLET_DAMAGE);
        }
    }
}