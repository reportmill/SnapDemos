package snapdemos.jewels;
import snap.gfx.*;
import snap.view.*;

/**
 * A view class to display a gem.
 */
public class Gem extends ImageView {

    // The Gem id
    private int _gemId;
    
    // The gem grid X/Y
    private int _gridX, _gridY;
    
    // Constants
    static int TILE_SIZE = PlayView.TILE_SIZE;
    
    // The gems
    static Image BLUE_GEM = Image.getImageForClassResource(Gem.class, "pkg.images/BlueGem.png");
    static Image GREEN_GEM = Image.getImageForClassResource(Gem.class, "pkg.images/GreenGem.png");
    static Image ORANGE_GEM = Image.getImageForClassResource(Gem.class, "pkg.images/OrangeGem.png");
    static Image PURPLE_GEM = Image.getImageForClassResource(Gem.class, "pkg.images/PurpleGem.png");
    static Image RED_GEM = Image.getImageForClassResource(Gem.class, "pkg.images/RedGem.png");
    static Image WHITE_GEM = Image.getImageForClassResource(Gem.class, "pkg.images/WhiteGem.png");
    static Image YELLOW_GEM = Image.getImageForClassResource(Gem.class, "pkg.images/YellowGem.png");
    static Image[] ALL_GEMS = new Image[] { BLUE_GEM, GREEN_GEM, ORANGE_GEM, PURPLE_GEM, RED_GEM, WHITE_GEM, YELLOW_GEM };

    /**
     * Create new Gem.
     */
    public Gem()
    {
        _gemId = (int) Math.floor(Math.random() * 7);
        setImage(ALL_GEMS[_gemId]);
        setSize(TILE_SIZE, TILE_SIZE);
        setPrefSize(TILE_SIZE, TILE_SIZE);
    }

    /**
     * Returns the gem id.
     */
    public int getId()  { return _gemId; }

    /**
     * Returns the grid X of gem.
     */
    public int getGridX()  { return _gridX; }

    /**
     * Returns the grid Y of gem.
     */
    public int getGridY()  { return _gridY; }

    /**
     * Sets the gem grid X/Y
     */
    public void setGridXY(int gridX, int gridY)  { _gridX = gridX; _gridY = gridY; }
}