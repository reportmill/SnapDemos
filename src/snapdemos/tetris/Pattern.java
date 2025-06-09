package snapdemos.tetris;
import snap.geom.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to represent a pattern for a game block.
 */
public class Pattern {

    // The number of tiles
    public int tileCount;
    
    // The number of columns, rows and tiles
    public int colCount, rowCount;
    
    // Array of packed (col,row) pairs of tiles for pattern
    public int[] fill;
    
    // The color of pattern tile
    private Color _tileColor;
    
    // An image of pattern tile
    private Image _tileImage;

    // Pattern constants
    public static Pattern SQUARE = new Pattern(2, 2, Color.BLUE.brighter().brighter(), new int[] { 0, 0, 0, 1, 1, 0, 1, 1 });
    public static Pattern STICK = new Pattern(4, 1, Color.MAGENTA, new int[] { 0, 0, 0, 1, 0, 2, 0, 3 });
    public static Pattern BOAT = new Pattern(2, 3, Color.GREEN, new int[] { 0, 0, 1, 0, 2, 0, 1, 1 });
    public static Pattern L1 = new Pattern(3, 2, Color.YELLOW, new int[] { 0, 0, 0, 1, 0, 2, 1, 2 });
    public static Pattern L2 = new Pattern(3, 2, Color.ORANGE, new int[] { 1, 0, 1, 1, 0, 2, 1, 2 });
    public static Pattern S1 = new Pattern(2, 3, Color.PINK, new int[] { 0, 0, 1, 0, 1, 1, 2, 1 });
    public static Pattern S2 = new Pattern(2, 3, Color.CYAN, new int[] { 1, 0, 2, 0, 0, 1, 1, 1 });
    public static Pattern[] ALL_PATTERNS = new Pattern[] { SQUARE, STICK, BOAT, L1, L2, S1, S2 };

    // Tile constants
    private static final Effect TILE_EFFECT = new EmbossEffect(60, 120, 4);
    private static final int TILE_OFFSET = 4; // Due to EmbossEffect radius

    /**
     * Constructor for row/col count, color and tile coords array.
     */
    private Pattern(int aRowCount, int aColCount, Color aColor, int[] fillArray)
    {
        rowCount = aRowCount;
        colCount = aColCount;
        _tileColor = aColor;
        fill = fillArray;
        tileCount = fill.length / 2;
    }

    /**
     * Paints the pattern to given painter.
     */
    public void paintPattern(Painter aPntr)
    {
        // Iterate over fill col/row pairs
        for (int i = 0; i < fill.length; i++) {
            double tileX = fill[i++] * Block.TILE_SIZE;
            double tileY = fill[i] * Block.TILE_SIZE;
            paintTile(aPntr, tileX, tileY);
        }
    }

    /**
     * Paints a tile at given XY.
     */
    public void paintTile(Painter aPntr, double tileX, double tileY)
    {
        if (_tileImage == null) _tileImage = getTileImageForColor(_tileColor);
        aPntr.drawImage(_tileImage, tileX - TILE_OFFSET, tileY - TILE_OFFSET);
    }

    /**
     * Returns the pattern derived by rotating this pattern clockwise.
     */
    public Pattern getRotateRight()
    {
        int[] rotatedFillArray = getRotatedFillArray();
        return new Pattern(colCount, rowCount, _tileColor, rotatedFillArray);
    }

    /**
     * Returns the fill array rotated by 90 degrees counterclockwise.
     */
    private int[] getRotatedFillArray()
    {
        // Get rotation about pattern center
        double midX = colCount / 2d;
        double midY = rowCount / 2d;
        Transform rotateTransform = new Transform(midX, midY);
        rotateTransform.rotate(-90);
        rotateTransform.translate(-midX, -midY);

        // Translate from old upper right point to origin
        Point upperRightCorner = rotateTransform.transformXY(colCount, 0);
        rotateTransform.preTranslate(-upperRightCorner.x, -upperRightCorner.y);

        // Get rotated fill array
        int[] rotatedFillArray = new int[fill.length];
        for (int i = 0; i < fill.length; i += 2) {
            Point rotatedPoint = rotateTransform.transformXY(fill[i] + .5, fill[i+1] + .5);
            rotatedFillArray[i] = (int) Math.round(rotatedPoint.x - .5);
            rotatedFillArray[i + 1] = (int) Math.round(rotatedPoint.y - .5);
        }

        // Return
        return rotatedFillArray;
    }

    /**
     * Creates an image of a tile for given color.
     */
    private static Image getTileImageForColor(Color aColor)
    {
        View view = new BoxView();
        view.setSize(Block.TILE_SIZE, Block.TILE_SIZE);
        view.setPrefSize(Block.TILE_SIZE, Block.TILE_SIZE);
        view.setBorder(aColor.blend(Color.BLACK,.1), 1);
        view.setFill(aColor);
        view.setEffect(TILE_EFFECT);
        return ViewUtils.getImage(view);
    }
}