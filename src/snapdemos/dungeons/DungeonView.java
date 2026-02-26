package snapdemos.dungeons;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.view.ParentView;
import snap.view.ViewEvent;

/**
 * This view class displays a dungeon.
 */
public class DungeonView extends ParentView {

    // The dungeon width / height in tiles
    private int _tileWidth, _tileHeight;

    // The cell size
    private int _tileSize;

    // The tile images
    private Image[][] _tiles;

    // The hero
    private Hero _hero;

    // Tile images
    private static final Image StoneTile1 = Image.getImageForClassResource(DungeonView.class, "pkg.images/StoneTile1.png");
    private static final Image WallCornerFrontLeft = Image.getImageForClassResource(DungeonView.class, "pkg.images/WallCornerFront.png");
    private static final Image WallCornerFrontRight = WallCornerFrontLeft.copyflippedX();
    private static final Image WallCornerBackLeft = Image.getImageForClassResource(DungeonView.class, "pkg.images/WallCornerBack.png");
    private static final Image WallCornerBackRight = WallCornerBackLeft.copyflippedX();
    private static final Image PlateTile1 = Image.getImageForClassResource(DungeonView.class, "pkg.images/PlateTile1.png");

    /**
     * Constructor.
     */
    public DungeonView()
    {
        super();
        _tileWidth = 8;
        _tileHeight = 7;
        _tileSize = 100;

        loadMap();

        Key key1 = new Key();
        key1.setXY(300, 500);
        addChild(key1);
        Key key2 = new Key();
        key2.setXY(500, 200);
        addChild(key2);
        Key key3 = new Key();
        key3.setXY(700, 500);
        addChild(key3);

        _hero = new Hero();
        addChild(_hero);

        Monster monster = new Monster();
        monster.setXY(600, 600);
        addChild(monster);

        Elon elon = new Elon();
        elon.setXY(100, 400);
        addChild(elon);

        enableEvents(KeyPress);
        setFocusable(true);
        setFocusWhenPressed(true);
    }

    /**
     * Returns the number of tiles wide.
     */
    public int getTileWidth()  { return _tileWidth; }

    /**
     * Returns the number of tiles high.
     */
    public int getTileHeight()  { return _tileHeight; }

    /**
     * Returns the tile image for tile id.
     */
    public Image getTileImageForId(int anId)
    {
        switch(anId) {
            case 0: return StoneTile1;
            case 1: return WallCornerFrontLeft;
            case 2: return WallCornerFrontRight;
            case 3: return WallCornerBackLeft;
            case 4: return WallCornerBackRight;
            case 5: return PlateTile1;
            default: return null;
        }
    }

    /**
     * Paint method.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        for (int i = 0; i < _tileWidth; i++) {
            for (int j = 0; j < _tileHeight; j++) {
                double tileX = i * _tileSize;
                double tileY = j * _tileSize;
                aPntr.drawImage(_tiles[i][j], tileX, tileY, _tileSize, _tileSize);
            }
        }
    }

    @Override
    protected double computePrefWidth(double aH)  { return _tileWidth * _tileSize; }

    @Override
    protected double computePrefHeight(double aW)  { return _tileHeight * _tileSize; }

    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        if (anEvent.isRightArrow())
            _hero.moveRight();
        else if (anEvent.isLeftArrow())
            _hero.moveLeft();
        else if (anEvent.isUpArrow())
            _hero.moveUp();
        else if (anEvent.isDownArrow())
            _hero.moveDown();
    }

    /**
     * Loads the map.
     */
    private void loadMap()
    {
        _tiles = new Image[_tileWidth][_tileHeight];

        for (int i = 0; i < _tileWidth; i++) {
            for (int j = 0; j < _tileHeight; j++) {
                int tileId = Map1[j][i];
                _tiles[i][j] = getTileImageForId(tileId);
            }
        }
    }

    private static int[][] Map1 = {
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 3, 4, 0, 0, 5, 0 },
            { 0, 0, 1, 2, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 0, 3, 4, 0 },
            { 0, 5, 0, 0, 0, 1, 2, 0 },
            { 0, 0, 0, 0, 0, 0, 0, 0 }
    };
}
