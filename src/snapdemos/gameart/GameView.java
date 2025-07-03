package snapdemos.gameart;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx.ImageSet;
import snap.gfx.Painter;
import snap.util.Convert;
import snap.view.*;
import snap.web.WebURL;

/**
 *
 */
public class GameView extends View {

    // The AssetFile
    private AssetFile _assetFile;

    // The ImageSet
    private ImageSet _imageSet;

    // The frame index
    private int _frameIndex;

    // The image size
    private int _imageW, _imageH;

    /**
     * Constructor.
     */
    public GameView()
    {
        super();

        String assetUrlString = "/Users/jeff/Downloads/adventure_girl.zip";
        WebURL assetSourceURL = WebURL.getUrl(assetUrlString);
        _assetFile = new AssetFile(assetSourceURL);

        // Set initial ImageSet
        ImageSet imageSet = _assetFile.getSpriteSets()[0].getImageSet();
        setImageSet(imageSet);

        new ViewTimer(this::repaint, 40).start();

        setPrefSize(500, 500);
        setFill(Color.WHITE);

        enableEvents(KeyPress);
    }

    /**
     * Sets the ImageSet.
     */
    private void setImageSet(ImageSet anImageSet)
    {
        _imageSet = anImageSet;

        // Get image size
        Image image = _imageSet.getImage(0);
        int pixW = image.getPixWidth();
        int pixH = image.getPixHeight();
        _imageW = _imageH = 200;
        if (pixW > pixH)
            _imageH = (int) Math.round(_imageW * pixH / (double) pixW);
        else _imageW = (int) Math.round(_imageH * pixW / (double) pixH);
    }

    @Override
    protected void paintFront(Painter aPntr)
    {
        int imageIndex = _frameIndex++ % _imageSet.getCount();
        Image image = _imageSet.getImage(imageIndex);
        aPntr.drawImage(image, 50, 50, _imageW, _imageH);
    }

    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        if (anEvent.isKeyPress()) {
            String keyCharStr = anEvent.getKeyString();
            int value = Convert.intValue(keyCharStr);
            SpriteSet[] spriteSets = _assetFile.getSpriteSets();
            int spriteSetIndex = value % spriteSets.length;
            ImageSet imageSet = spriteSets[spriteSetIndex].getImageSet();
            setImageSet(imageSet);
        }
    }

    public static void main(String[] args)
    {
        GameView gameView = new GameView();
        new ViewOwner(gameView).setWindowVisible(true);
    }
}
