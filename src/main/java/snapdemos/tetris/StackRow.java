package snapdemos.tetris;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snap.util.MathUtils;
import snap.view.*;

/**
 * A class to represent row of block tiles at bottom of PlayView.
 */
public class StackRow extends View {

    // The array of filled tiles
    private Pattern[] _cols = new Pattern[GRID_WIDTH];
    
    // The array of tile rects
    private Rect[] _tileRects;
    
    // The row number
    protected int _rowNum;
    
    // Constants
    private static int TILE_SIZE = Block.TILE_SIZE;
    private static int GRID_WIDTH = PlayView.GRID_WIDTH;

    /**
     * Constructor.
     */
    public StackRow()
    {
        super();
        double rowW = PlayView.GRID_WIDTH * TILE_SIZE;
        double rowH = TILE_SIZE;
        setSize(rowW, rowH);
        setEffect(Block.BLOCK_EFFECT);
    }

    /**
     * Returns the row tile rects in parent coords.
     */
    public Rect[] getTileRectsInParent()
    {
        if(_tileRects != null) return _tileRects;

        List <Rect> tileRects = new ArrayList<>();
        for(int i = 0; i < _cols.length; i++) {
            Pattern col = _cols[i];
            if(col == null)
                continue;
            double tileX = i * TILE_SIZE;
            Rect rect = new Rect(getX() + tileX, getY(), TILE_SIZE, TILE_SIZE);
            tileRects.add(rect);
        }

        // Return array
        return _tileRects = tileRects.toArray(new Rect[0]);
    }

    /**
     * Returns whether block intersects row.
     */
    public boolean intersectsBlock(Block aBlock)
    {
        // If block above row, return false
        if(MathUtils.lt(aBlock.getMaxY(), getY()))
            return false;

        // Iterate over block tiles and see if any intersect row tiles
        for(int i = 0; i < aBlock.getTileCount(); i++) {
            Rect blockTileRect = aBlock.getTileRectInParent(i);
            Rect blockTileRect2 = blockTileRect.getInsetRect(2, .2);
            Rect[] rowTileRectsInParentCoords = getTileRectsInParent();
            for(Rect rowTileRect : rowTileRectsInParentCoords)
                if(rowTileRect.intersects(blockTileRect2))
                    return true;
        }

        // Return false since no block tiles hit row tiles
        return false;
    }

    /**
     * Adds block tiles.
     */
    public void addBlockTiles(Block aBlock)
    {
        for(int i = 0; i < aBlock.getTileCount(); i++) {
            Rect tileBounds = aBlock.getTileRectInParent(i);
            double tileX = tileBounds.getMidX() - getX();
            if(!contains(tileX, tileBounds.getMidY() - getY()))
                continue;
            int colIndex = (int) Math.floor(tileX / TILE_SIZE);
            _cols[colIndex] = aBlock._pattern;
        }

        // Repaint & reset TileRects
        repaint(); _tileRects = null;
    }

    /**
     * Returns whether row is full.
     */
    public boolean isFull()
    {
        for (Pattern col : _cols)
            if (col == null)
                return false;
        return true;
    }

    /**
     * Paint block pattern.
     */
    protected void paintFront(Painter aPntr)
    {
        for (int i = 0; i < _cols.length; i++) {
            Pattern pat = _cols[i];
            if (pat == null)
                continue;
            double tileX = i * TILE_SIZE;
            pat.paintTile(aPntr, tileX, 0);
        }
    }
}