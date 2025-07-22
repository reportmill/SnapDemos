package snapdemos.jbox2d;
import snap.gfx.Color;
import snap.util.SnapEnv;
import snap.view.*;

/**
 * This class is a simple test of JBox2D.
 */
public class SimpleTest extends ViewOwner {

    // The world view
    private WorldView _worldView;

    /**
     * Constructor.
     */
    public SimpleTest()
    {
        super();
    }

    @Override
    protected View createUI()
    {
        _worldView = new WorldView();
        _worldView.setPrefSize(600, 600);
        return _worldView;
    }

    @Override
    protected void initShowing()
    {
        runLater(this::initShowingImpl);
    }

    protected void initShowingImpl()
    {
        double viewW = getUI().getWidth();
        double viewH = getUI().getHeight();
        double rectSize = 60;

        // Create rect view and add
        RectView rectView = new RectView(viewW / 2 - rectSize / 2, 0, rectSize, rectSize);
        rectView.setFill(Color.BLUE);
        rectView.getPhysics(true).setDensity(1);
        _worldView.addChild(rectView);

        // Configure world
        _worldView.setHeightInMeters(20);
        _worldView.addWalls();
        _worldView.addJBoxNativesForChildren();

        // Create and configure JBoxWorld for world view
        JBoxWorld jboxWorld = _worldView.getJBoxWorld();
        jboxWorld.setRunning(true);
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)
    {
        SimpleTest simpleTest = new SimpleTest();
        simpleTest.getWindow().setMaximized(SnapEnv.isWebVM);
        simpleTest.setWindowVisible(true);
    }
}
