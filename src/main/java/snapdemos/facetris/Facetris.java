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
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        _playView = new PlayView();
        _playView.setPrefSize(1000,1000);
        return _playView;
    }

    @Override
    protected void initUI()
    {
    }

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
