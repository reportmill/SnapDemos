package snapdemos.asteroids;
import snap.geom.Vector;
import snap.gfx.Image;
import snap.gfx.SoundClip;
import snap.viewx.Explode;
import java.util.Random;

/**
 * This class models an asteroid.
 */
public class Asteroid extends ActorView
{
    // The amount of life left
    private int _life;

    // The image for an asteroid
    private static Image AsteroidImage = Image.getImageForClassResource(Asteroid.class, "resources/Asteroid.png");

    // The sound of asteroid exploding
    private static SoundClip EXPLOSION_SOUND = SoundClip.get(Asteroid.class, "resources/Explosion.wav");

    /**
     * Constructor.
     */
    public Asteroid()
    {
        this(50);
    }

    /**
     * Constructor for size.
     */
    public Asteroid(int size)
    {
        super();
        setSize(size);
        setVelocity(Vector.getVectorForAngleAndLength(new Random().nextInt(360), 2));
    }

    /**
     * Constructor for size and speed.
     */
    public Asteroid(int size, Vector speed)
    {
        super();
        setSize(size);
        setVelocity(speed);
    }
    
    public void act()
    {         
        move();
    }

    /**
     * Set the size of this asteroid.
     */
    public void setSize(int size) 
    {
        _life = size;
        setSize(size, size);

        // If size is different than image, get new image size
        Image image = AsteroidImage;
        if (size != AsteroidImage.getWidth() && AsteroidImage.isLoaded())
            image = AsteroidImage.cloneForSize(size, size);
        setImage(image);
    }

    /**
     * Hit this asteroid dealing the given amount of damage.
     */
    public void hit(int damage) 
    {
        _life = _life - damage;
        if(_life <= 0)
            explode();
    }
    
    /**
     * Handles asteroid explosion: Plays sound, trigger explosion animation and remove.
     * If asteroid big enough, create two smaller asteroids.
     */
    private void explode()
    {
        // Play explosion
        EXPLOSION_SOUND.play();

        // Explode
        new Explode(this, 20, 20, null).play();
        
        // Remove this asteroid
        GameView scene = getScene();
        scene.removeActor(this);

        // If not minimal size, create and add two half asteroids
        if (getWidth() > 16)
            subdivide(scene);

        // If no other asteroids left, do game over
        else {
            Asteroid[] asteroids = scene.getActorsForClass(Asteroid.class);
            if (asteroids.length == 0)
                ((SpaceView) scene).gameOver();
        }
    }

    /**
     * Breaks asteroid into two smaller asteroids.
     */
    private void subdivide(GameView scene)
    {
        double angle = getVelocity().getAngle() + new Random().nextInt(45);
        double length = getVelocity().getLength();
        Vector speed1 = Vector.getVectorForAngleAndLength(angle + 60, length * 1.2);
        Vector speed2 = Vector.getVectorForAngleAndLength(angle - 60, length * 1.2);
        int size = (int) getWidth();
        Asteroid a1 = new Asteroid(size / 2, speed1);
        Asteroid a2 = new Asteroid(size / 2, speed2);
        scene.addActorAtXY(a1, getCenterX(), getCenterY());
        scene.addActorAtXY(a2, getCenterX(), getCenterY());
        a1.move();
        a2.move();
    }
}