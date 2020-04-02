package snapdemos.facetris;
import snap.view.View;
import snap.view.ViewOwner;

/**
 *
 */
public class Facetris extends ViewOwner {

    // The PlayView
    private PlayView  _playView;

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        _playView = getView("PlayView", PlayView.class);
        setFirstFocus("NameText");
    }

    /**
     * Called when UI is showing.
     */
    @Override
    protected void showingChanged()
    {
        super.showingChanged();

        if (isShowing())
            _playView.play();
    }

    /**
     * Standard Main implementation.
     */
    public static void main(String args[])
    {
        Facetris game = new Facetris();
        game.setWindowVisible(true);
    }
}
