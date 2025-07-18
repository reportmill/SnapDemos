package snapdemos.jbox2d;
import snap.gfx.Color;
import snap.util.SnapEnv;
import snap.view.*;

/**
 * This class is a simple test of JBox2D.
 */
public class SimpleTest extends ViewOwner {

    // The world view
    private ChildView _worldView;

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
        _worldView = new ChildView();
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

        // Create physics
        PhysicsRunner physicsRunner = new PhysicsRunner(_worldView);
        physicsRunner.setViewToWorldMeters(viewH / 20);
        physicsRunner.addWallsToWorldView();
        physicsRunner.addPhysicsForWorldViewChildren();
        physicsRunner.setRunning(true);
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
