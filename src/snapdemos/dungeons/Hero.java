package snapdemos.dungeons;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.view.View;
import snap.view.ViewList;
import snap.view.ViewUtils;

/**
 * This class represents the hero.
 */
public class Hero extends View {

    // The image state
    private int _imageState;

    // The hero tile location
    private double _tileX, _tileY;

    private static final Image HeroRight = Image.getImageForClassResource(DungeonView.class, "pkg.images/Hero.png");
    private static final Image HeroLeft = HeroRight.copyflippedX();
    private static final Image HeroUp = Image.getImageForClassResource(DungeonView.class, "pkg.images/HeroUp.png");
    private static final Image HeroDown = Image.getImageForClassResource(DungeonView.class, "pkg.images/HeroDown.png");

    /**
     * Constructor.
     */
    public Hero()
    {
        setSize(100, 100);
    }

    public void moveRight()
    {
        _imageState = 0;
        repaint();
        if (getMaxX() + 100 > getParent().getWidth())
            return;
        getAnim(0).clear().getAnim(500).setX(getX() + 100).needsFinish().play();
        getAnim(0).setOnFinish(this::finishedAnim);
    }

    public void moveLeft()
    {
        _imageState = 1;
        repaint();
        if (getX() - 100 < 0)
            return;
        getAnim(500).clear().setX(getX() - 100).needsFinish().play();
        getAnim(0).setOnFinish(this::finishedAnim);
    }

    public void moveUp()
    {
        _imageState = 2;
        repaint();
        if (getY() - 100 < 0)
            return;
        getAnim(500).clear().setY(getY() - 100).needsFinish().play();
        getAnim(0).setOnFinish(this::finishedAnim);
    }

    public void moveDown()
    {
        _imageState = 3;
        repaint();
        if (getMaxY() + 100 > getParent().getHeight())
            return;
        getAnim(500).clear().setY(getY() + 100).needsFinish().play();
        getAnim(0).setOnFinish(this::finishedAnim);
    }

    private void finishedAnim()
    {
        ViewList children = getParent().getChildren();
        for (View child : children) {
            if (child == this) continue;
            if (child.getBounds().intersectsRect(getBounds())) {
                ViewUtils.removeChild(getParent(), child);
                DungeonPane dungeonPane = getParent().getOwner(DungeonPane.class);
                child.setPrefSize(child.getSize());
                dungeonPane._inventoryView.addChild(child);
                return;
            }
        }
    }

    /**
     * Returns the tile image for tile id.
     */
    public Image getImageForState(int anId)
    {
        switch(anId) {
            case 0: return HeroRight;
            case 1: return HeroLeft;
            case 2: return HeroUp;
            case 3: return HeroDown;
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
