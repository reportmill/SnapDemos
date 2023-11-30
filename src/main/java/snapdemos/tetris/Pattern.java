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
    
    // The color of pattern
    public Color color;
    
    // The image
    public Image image;

    // Pattern constants
    public static Pattern SQUARE, STICK, BOAT, L1, L2, S1, S2;
    public static Pattern[] ALL_PATTERNS;

    // Tile constants
    public static final int TILE_SIZE = 32;
    public static final Effect TILE_EFFECT = new EmbossEffect(60, 120, 4);
    private static final int TILE_OFFSET = 4; // Due to EmbossEffect radius

    /**
     * Creates Patterns.
     */
    static
    {
        SQUARE = new Pattern(2, 2, Color.BLUE.brighter().brighter(), new int[] { 0, 0, 0, 1, 1, 0, 1, 1 });
        STICK = new Pattern(4, 1, Color.MAGENTA, new int[] { 0, 0, 0, 1, 0, 2, 0, 3 });
        BOAT = new Pattern(2, 3, Color.GREEN, new int[] { 0, 0, 1, 0, 2, 0, 1, 1 });
        L1 = new Pattern(3, 2, Color.YELLOW, new int[] { 0, 0, 0, 1, 0, 2, 1, 2 });
        L2 = new Pattern(3, 2, Color.ORANGE, new int[] { 1, 0, 1, 1, 0, 2, 1, 2 });
        S1 = new Pattern(2, 3, Color.PINK, new int[] { 0, 0, 1, 0, 1, 1, 2, 1 });
        S2 = new Pattern(2, 3, Color.CYAN, new int[] { 1, 0, 2, 0, 0, 1, 1, 1 });
        ALL_PATTERNS = new Pattern[] { SQUARE, STICK, BOAT, L1, L2, S1, S2 };
    }

    /**
     * Constructor for row/col count, color and tile coords array.
     */
    private Pattern(int aRowCount, int aColCount, Color aColor, int[] fillArray)
    {
        rowCount = aRowCount;
        colCount = aColCount;
        color = aColor;
        fill = fillArray;
        image = getImage(aColor);
        tileCount = fill.length/2;
    }

    /**
     * Paints the pattern to given painter.
     */
    public void paint(Painter aPntr)
    {
        // Iterate over fill col/row pairs
        for (int i = 0; i < fill.length; i++) {
            double tileX = fill[i++] * TILE_SIZE;
            double tileY = fill[i] * TILE_SIZE;
            paintTile(aPntr, tileX, tileY);
        }
    }

    /**
     * Paints a tile at given XY.
     */
    public void paintTile(Painter aPntr, double tileX, double tileY)
    {
        aPntr.drawImage(image, tileX - TILE_OFFSET, tileY - TILE_OFFSET);
    }

    /**
     * Returns the pattern derived by rotating this pattern clockwise.
     */
    public Pattern getRotateRight()
    {
        int[] rotatedFillArray = getRotatedFillArray();
        return new Pattern(colCount, rowCount, color, rotatedFillArray);
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
            Point p = rotateTransform.transformXY(fill[i] + .5, fill[i+1] + .5);
            rotatedFillArray[i] = (int) Math.round(p.x - .5);
            rotatedFillArray[i + 1] = (int) Math.round(p.y - .5);
        }

        // Return
        return rotatedFillArray;
    }

    /**
     * Creates an image of a tile for given color.
     */
    private static Image getImage(Color aColor)
    {
        View view = new View() { };
        view.setSize(TILE_SIZE, TILE_SIZE);
        view.setPrefSize(TILE_SIZE, TILE_SIZE);
        view.setBorder(aColor.blend(Color.BLACK,.1), 1);
        view.setFill(aColor);
        view.setEffect(TILE_EFFECT);
        return ViewUtils.getImage(view);
    }
}