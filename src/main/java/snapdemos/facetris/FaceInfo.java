package snapdemos.facetris;

import snap.gfx.Image;
import snap.view.ImageView;
import snap.view.View;
import snap.web.WebURL;

/**
 * An Inner class to hold face information.
 */
public class FaceInfo {

    public String name;
    public String fname;

    // The image
    private Image _image;

    // The View
    private ImageView  _view;

    /**
     * Returns the image.
     */
    public Image getImage()
    {
        if (_image!=null) return _image;
        String urls = FaceIndex.ROOT + '/' + fname;
        WebURL url = WebURL.getURL(urls);
        Image img = Image.get(url);
        return _image = img;
    }

    /**
     * Returns a view.
     */
    public ImageView getView()
    {
        // If already set, just return
        if (_view!=null) return _view;

        // Create image and image view
        Image img = getImage();
        ImageView iview = new ImageView(img);
        iview.setSize(iview.getPrefSize());

        // If not loaded, set to resize when loaded
        if (!img.isLoaded())
            img.addLoadListener(() -> imageDidLoad());

        // Return image view
        return _view = iview;
    }

    /**
     * Called when image is loaded.
     */
    private void imageDidLoad()
    {
        View view = getView();
        view.setSize(view.getPrefSize());
    }
}
