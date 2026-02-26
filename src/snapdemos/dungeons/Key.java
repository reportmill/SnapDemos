package snapdemos.dungeons;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.gfx.ShadowEffect;
import snap.view.View;

/**
 * This class represents the hero.
 */
public class Key extends View {

    // The image state
    private int _imageState;

    // The hero tile location
    private double _tileX, _tileY;

    private static final Image Key = Image.getImageForClassResource(DungeonView.class, "pkg.images/Key.png");

    /**
     * Constructor.
     */
    public Key()
    {
        super();
        setSize(100, 100);
        setEffect(new ShadowEffect(30, Color.YELLOW, 0, 0));
    }

    /**
     * Returns the tile image for tile id.
     */
    public Image getImageForState(int anId)
    {
        switch(anId) {
            case 0: return Key;
            default: return null;
        }
    }

    @Override
    protected void paintFront(Painter aPntr)
    {
        Image hero = getImageForState(_imageState);
        aPntr.drawImage(hero, 0, 0, 100, 100);
    }
}
