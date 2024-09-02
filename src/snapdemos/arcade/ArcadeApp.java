package snapdemos.arcade;
import snap.gfx.Color;
import snap.view.Label;
import snap.view.ViewEvent;
import snap.view.ViewOwner;
import snap.view.ViewUtils;
import snapdemos.asteroids.SpaceView;
import snapdemos.jewels.JewelsApp;
import snapdemos.snappybird.SnappyBird;
import snapdemos.tetris.TetrisPane;

/**
 * This app class is a launcher for several of the demos.
 */
public class ArcadeApp extends ViewOwner {

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        getView("TitleLabel", Label.class).setTextColor(Color.WHITE);
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle AsteroidsButton
        if (anEvent.equals("AsteroidsButton")) {
            SpaceView.main(new String[0]);
            getWindow().hide();
        }

        // Handle SnaptrisButton
        if (anEvent.equals("SnaptrisButton")) {
            TetrisPane.main(new String[0]);
            getWindow().hide();
        }

        // Handle SnappyBirdButton
        if (anEvent.equals("SnappyBirdButton")) {
            SnappyBird.main(new String[0]);
            getWindow().hide();
        }

        // Handle JewelsButton
        if (anEvent.equals("JewelsButton")) {
            JewelsApp.main(new String[0]);
            getWindow().hide();
        }

        // Handle SnapShowButton
        if (anEvent.equals("SnapShowButton")) {
            snapdemos.snapshow.SlidePane.main(new String[0]);
            getWindow().hide();
        }
    }

    /**
     * Standard main implementation.
     */
    public static void main(String[] args)
    {
        ViewUtils.runLater(() -> {
            ArcadeApp arcadeApp = new ArcadeApp();
            arcadeApp.setWindowVisible(true);
        });
    }
}
