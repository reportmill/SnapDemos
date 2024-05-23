package snapdemos.facetris;
import snap.gfx.Image;
import snap.gfx.ShadowEffect;
import snap.view.ImageView;
import snap.web.WebURL;

/**
 * A class to hold face information.
 */
public class Face {

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
    public Face(String aFileName)
    {
        // Set filename
        _filename = aFileName;

        // Get name
        _name = aFileName.replace(".jpg", "");
        _name = _name.replace(".jpeg","");
        _name = _name.replace(".JPG","");
        _name = _name.replace("_GmbH", "");
        _name = _name.replace("_Athens", "");
        _name = _name.replace("_Spain", "");
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
     * Returns whether is won.
     */
    public boolean isWon()  { return _status == Status.Won; }

    /**
     * Returns whether is lost.
     */
    public boolean isLost()  { return _status == Status.Lost; }

    /**
     * Returns the image.
     */
    public Image getImage()
    {
        if (_image!=null) return _image;
        String urls = FaceIndex.ROOT + '/' + _filename;
        WebURL url = WebURL.getURL(urls);
        Image img = Image.getImageForSource(url);
        return _image = img;
    }

    /**
     * Returns a view.
     */
    public FaceView getView()
    {
        // If already set, just return
        if (_view!=null) return _view;

        // Create view
        FaceView view = new FaceView(this);
        return _view = view;
    }

    /**
     * Returns a view.
     */
    public ImageView getMiniView()
    {
        // If already set, just return
        if (_miniView!=null) return _miniView;

        // Create image and image view
        Image img = getImage();
        ImageView iview = new ImageView(img);
        iview.setEffect(new ShadowEffect());

        // If not loaded, set to resize when loaded
        if (!img.isLoaded())
            img.addLoadListener(() -> imageLoaded(iview, img));
        else iview.setPrefSize(img.getWidth()/2, img.getHeight()/2);

        // Return image view
        return _miniView = iview;
    }

    void imageLoaded(ImageView iview, Image img)
    {
        iview.setPrefSize(img.getWidth()/2, img.getHeight()/2);
    }
}
