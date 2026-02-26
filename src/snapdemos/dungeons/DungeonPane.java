package snapdemos.dungeons;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.util.SnapEnv;
import snap.view.*;

/**
 * This class is the main UI controller for the game.
 */
public class DungeonPane extends ViewOwner {

    // The DungeonView
    private DungeonView _dungeonView;

    // The inventory view
    protected ColView _inventoryView;

    @Override
    protected View createUI()
    {
        Label label = new Label("Dungeon Builder");
        label.setPropsString("Font:Times Bold 70; TextColor:WHITE; Margin:14,0,25,0; LeanX:CENTER");
        _dungeonView = new DungeonView();
        _dungeonView.setMargin(4, 4,4, 4);

        BoxView dungeonViewBox = new BoxView(_dungeonView);
        dungeonViewBox.setBorder(Color.get("#550055"), 12);

        ColView colView = new ColView();
        colView.setAlign(Pos.TOP_CENTER);
        colView.setChildren(label, dungeonViewBox);
        colView.setMargin(20, 20, 20, 20);

        ColView menuView = new ColView();
        menuView.setMargin(150, 20, 20, 20);
        menuView.setPrefWidth(100);

        _inventoryView = new ColView();
        _inventoryView.setMargin(150, 20, 20, 20);
        _inventoryView.setPrefWidth(100);

        RowView rowView =  new RowView();
        rowView.setChildren(menuView, colView, _inventoryView);
        rowView.setAlign(Pos.TOP_CENTER);

        ScaleBox scaleBox = new ScaleBox(rowView);
        scaleBox.setFill(Color.get("#440044"));
        scaleBox.setKeepAspect(true);
        return scaleBox;
    }

    @Override
    protected void initUI()
    {
        setFirstFocus(_dungeonView);
    }

    /**
     * Standard main implementation.
     */
    public static void main(String[] args)
    {
        DungeonPane dungeonPane = new DungeonPane();
        dungeonPane.getWindow().setMaximized(SnapEnv.isWebVM);
        dungeonPane.setWindowVisible(true);
    }
}
