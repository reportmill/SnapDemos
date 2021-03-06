package snapdemos.facetris;
import snap.geom.Pos;
import snap.gfx.*;
import snap.view.*;

/**
 * A View to show a face.
 */
public class FaceView extends StackView {

    // The face
    private Face _face;

    // The ImageView
    private ImageView  _imageView;

    // Whether this face is lost
    private boolean _lost;

    // The lost view
    private View _lostView;

    // The name label
    private Label _nameLabel1, _nameLabel2;

    /**
     * Create FaceView.
     */
    public FaceView(Face aFace)
    {
        _face = aFace;

        setPadding(10,10,10,10);
        setFill(Color.WHITE);
        setBorder(new Color(.98), 1);
        setEffect(new ShadowEffect());

        Image img = aFace.getImage();
        _imageView = new ImageView(img);
        _imageView.setBorder(new Color(.9), 1);
        addChild(_imageView);

        setShowName(Facetris._cheat);

        // If not loaded, set to resize when loaded
        if (!img.isLoaded())
            img.addLoadListener(() -> imageDidLoad());
        else imageDidLoad();
    }

    /**
     * Returns the face.
     */
    public Face getFace()  { return _face; }

    /**
     * Returns whether face is lost.
     */
    public boolean isLost()
    {
        return _lost;
    }

    /**
     * Sets whether face is lost.
     */
    public void setLost(boolean aValue)
    {
        _lost = aValue;

        if (aValue) {
            setShowName(true);
            _lostView = new RectView(0, 0, getWidth(), getHeight());
            _lostView.setFill(new Color("#FFFFFF99"));
            addChild(_lostView);
        }

        else {
            removeChild(_lostView);
        }
    }

    /**
     * Returns whether to show name.
     */
    public boolean isShowName()
    {
        return _nameLabel1!=null;
    }

    /**
     * Sets whether to show name.
     */
    public void setShowName(boolean aValue)
    {
        if (aValue==isShowName()) return;

        if (aValue) {
            _nameLabel1 = new Label(_face.getName());
            _nameLabel1.setFont(Font.Arial12.getBold());
            _nameLabel1.setLean(Pos.BOTTOM_CENTER);
            _nameLabel1.setPadding(2, 2, 10, 2);
            addChild(_nameLabel1);

            _nameLabel2 = new Label(_face.getName());
            _nameLabel2.setFont(Font.Arial12.getBold());
            _nameLabel2.setTextFill(Color.WHITE);
            _nameLabel2.setLean(Pos.BOTTOM_CENTER);
            _nameLabel2.setPadding(2, 4, 11, 2);
            addChild(_nameLabel2);
        }
        else {
            removeChild(_nameLabel1);
            removeChild(_nameLabel2);
            _nameLabel1 = _nameLabel2 = null;
        }
    }

    /**
     * Called when image is loaded.
     */
    private void imageDidLoad()
    {
        double w = Math.round(_imageView.getPrefWidth()*.8);
        double h = Math.round(_imageView.getPrefHeight()*.8);
        _imageView.setPrefSize(w, h);
        setSize(getPrefSize());
    }
}
