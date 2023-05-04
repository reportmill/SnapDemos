package snapdemos.gameart;
import snap.gfx.Image;
import snap.web.WebFile;
import snap.web.WebSite;
import snap.web.WebURL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AssetFile {

    // The source
    private WebURL _sourceURL;

    // The source site
    private WebSite _sourceSite;

    // The SpriteSets
    private SpriteSet[] _spriteSets;

    /**
     * Constructor.
     */
    public AssetFile(WebURL aSourceURL)
    {
        super();
        _sourceURL = aSourceURL;
        _sourceSite = aSourceURL.getAsSite();
    }

    /**
     * Returns the SpriteSets.
     */
    public SpriteSet[] getSpriteSets()
    {
        if (_spriteSets != null) return _spriteSets;

        List<SpriteSet> spriteSets = new ArrayList<>();
        spriteSets.add(new SpriteSet(this, "Idle", 10));
        spriteSets.add(new SpriteSet(this, "Run", 8));
        spriteSets.add(new SpriteSet(this, "Jump", 10));
        spriteSets.add(new SpriteSet(this, "Slide", 5));
        spriteSets.add(new SpriteSet(this, "Shoot", 3));
        spriteSets.add(new SpriteSet(this, "Melee", 7));
        spriteSets.add(new SpriteSet(this, "Dead", 10));

        return _spriteSets = spriteSets.toArray(new SpriteSet[0]);
    }

    /**
     * Returns the image file for sprite set name and frame.
     */
    public Image getImageForNameAndFrame(String aName, int aFrame)
    {
        String filePath = "/png/" + aName + " (" + aFrame + ").png";
        WebFile file = _sourceSite.getFileForPath(filePath);
        Image image = Image.getImageForSource(file);
        return image;
    }
}
