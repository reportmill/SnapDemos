package snapdemos.gameart;
import snap.gfx.Image;
import snap.gfx.ImageSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SpriteSet {

    // The AssetFile
    private AssetFile _assetFile;

    // The name
    private String _name;

    // The frame count
    private int _frameCount;

    // The ImageSet
    private ImageSet _imageSet;

    /**
     * Constructor.
     */
    public SpriteSet(AssetFile anAssetFile, String aName, int frameCount)
    {
        _assetFile = anAssetFile;
        _name = aName;
        _frameCount = frameCount;
    }

    /**
     * Returns the ImageSet.
     */
    public ImageSet getImageSet()
    {
        if (_imageSet != null) return _imageSet;

        List<Image> images = new ArrayList<>();
        for (int i = 1; i <= _frameCount; i++) {
            Image image = _assetFile.getImageForNameAndFrame(_name, i);
            images.add(image);
        }

        ImageSet imageSet = new ImageSet(images);
        return _imageSet = imageSet;
    }
}
