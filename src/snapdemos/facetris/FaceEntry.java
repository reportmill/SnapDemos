package snapdemos.facetris;
import snap.gfx.Image;
import snap.gfx.ShadowEffect;
import snap.view.ImageView;
import snap.web.WebURL;

/**
 * A class to hold face information.
 */
public class FaceEntry {

    // The name
    private String  _name;

    // The first name
    private String  _first;

    // The filename
    private String _filename;

    // The image
    private Image  _image;

    // The View
    private FaceView  _view;

    // The View
    private ImageView  _miniView;

    // The status of the face
    private Status  _status = Status.InPlay;

    // The Face status
    public enum Status { InPlay, Won, Lost }

    /**
     * Constructor.
     */
    public FaceEntry(String aFileName)
    {
        // Set filename
        _filename = aFileName;

        // Get name
        _name = aFileName.replace(".jpg", "");
        _name = _name.replace(".jpeg","");
        _name = _name.replace("_", " ");

        int parenIndex = _name.indexOf('(');
        if (parenIndex > 0) {
            int endIndex = _name.indexOf(')', parenIndex+1);
            _first = _name.substring(parenIndex+1, endIndex);
            _name = _name.substring(0, parenIndex);
        }
        else {
            parenIndex = _name.indexOf(" ");
            if (parenIndex > 0)
                _first = _name.substring(0, parenIndex);
            else _first = _name;
        }
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Returns the first name.
     */
    public String getFirstName()  { return _first; }

    /**
     * Returns the status.
     */
    public Status getStatus()  { return _status; }

    /**
     * Sets the status.
     */
    public void setStatus(Status aStatus)
    {
        if (aStatus == getStatus()) return;
        _status = aStatus;

        switch (_status) {
            //case Won: _view.setWon(true); break;
            case Lost: _view.setLost(true); break;
        }
    }

    /**
     * Returns whether in play.
     */
    public boolean inPlay()  { return _status == Status.InPlay; }

    /**
     * Returns the image.
     */
    public Image getImage()
    {
        if (_image != null) return _image;
        String imageUrlAddress = FaceIndex.ROOT + '/' + _filename;
        WebURL imageUrl = WebURL.getUrl(imageUrlAddress);
        return _image = Image.getImageForSource(imageUrl);
    }

    /**
     * Returns a view.
     */
    public FaceView getView()
    {
        if (_view != null) return _view;
        return _view = new FaceView(this);
    }

    /**
     * Returns a view.
     */
    public ImageView getMiniView()
    {
        // If already set, just return
        if (_miniView != null) return _miniView;

        // Create image and image view
        Image image = getImage();
        ImageView imageView = new ImageView(image);
        imageView.setMaxSize(100, 100);
        imageView.setEffect(new ShadowEffect());

        // If not loaded, set to resize when loaded
        if (!image.isLoaded())
            image.addLoadListener(imageView::setSizeToBestSize);
        else imageView.setSizeToBestSize();

        // Return image view
        return _miniView = imageView;
    }
}
