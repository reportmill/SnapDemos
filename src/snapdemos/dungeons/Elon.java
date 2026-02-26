package snapdemos.dungeons;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.gfx.ShadowEffect;
import snap.view.View;
import snap.view.ViewList;
import snap.view.ViewUtils;

/**
 * This class represents monster.
 */
public class Elon extends View {

    // The image state
    private int _imageState;

    // The hero tile location
    private double _tileX, _tileY;

    private static final Image ElonRight = Image.getImageForClassResource(DungeonView.class, "pkg.images/Elon.png");
    private static final Image ElonLeft = ElonRight.copyflippedX();

    /**
     * Constructor.
     */
    public Elon()
    {
        setSize(100, 100);
        setEffect(new ShadowEffect(15, Color.GREEN.darker().darker(), 2, 2));
        runIntervals(this::updateCharacter, 800);
    }

    public void moveRight()
    {
        getAnim(0).clear().getAnim(500).setX(getX() + 100).needsFinish().play();
        getAnim(0).setOnFinish(this::finishedAnim);
        _imageState = 0;
    }

    public void moveLeft()
    {
        getAnim(500).clear().setX(getX() - 100).needsFinish().play();
        getAnim(0).setOnFinish(this::finishedAnim);
        _imageState = 1;
    }

    public void moveUp()
    {
        getAnim(500).clear().setY(getY() - 100).needsFinish().play();
        getAnim(0).setOnFinish(this::finishedAnim);
    }

    public void moveDown()
    {
        getAnim(500).clear().setY(getY() + 100).needsFinish().play();
        getAnim(0).setOnFinish(this::finishedAnim);
    }

    private void updateCharacter()
    {
        _updateState = (_updateState + 1) % 4;
        if (_updateState / 2d <= .5)
            moveRight();
        else moveLeft();
    }
    int _updateState = -1;

    private void finishedAnim()
    {
        ViewList children = getParent().getChildren();
        for (View child : children) {
            if (child == this) continue;
            if (child.getBounds().intersectsRect(getBounds())) {
                ViewUtils.removeChild(getParent(), child);
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
            case 0: return ElonRight;
            case 1: return ElonLeft;
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
