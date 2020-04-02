package snapdemos.facetris;

import snap.gfx.Image;
import snap.view.ImageView;
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
        byte bytes[] = url.getBytes();
        Image img = Image.get(bytes);
        return _image = img;
    }

    /**
     * Returns a view.
     */
    public ImageView getView()
    {
        if (_view!=null) return _view;
        Image img = getImage();
        ImageView iview = new ImageView(img);
        iview.setSize(iview.getPrefSize());
        return _view = iview;
    }
}
