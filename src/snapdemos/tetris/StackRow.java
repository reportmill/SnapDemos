package snapdemos.tetris;
import snap.geom.*;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.util.MathUtils;
import snap.view.*;

/**
 * A class to represent row of block tiles at bottom of PlayView.
 */
public class StackRow extends View {

    // The array of filled tiles
    private Pattern[] _cols;
    
    // The row number
    protected int _rowNum;
    
    /**
     * Constructor.
     */
    public StackRow()
    {
        super();
        _cols = new Pattern[PlayView.GRID_WIDTH];
        double rowW = PlayView.GRID_WIDTH * Block.TILE_SIZE;
        double rowH = Block.TILE_SIZE;
        setSize(rowW, rowH);
        setEffect(Block.BLOCK_EFFECT);
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
            Rect blockTileBounds = aBlock.getTileBoundsInParent(i);
            Rect blockTileBounds2 = blockTileBounds.getInsetRect(2, 2);
            if (intersectsRect(blockTileBounds2))
                return true;
        }

        // Return false since no block tiles hit row tiles
        return false;
    }

    /**
     * Returns whether this row intersects given rect.
     */
    private boolean intersectsRect(Rect aRect)
    {
        for(int i = 0; i < _cols.length; i++) {

            // If column empty, skip
            if(_cols[i] == null)
                continue;

            // If tile rect intersects given rect, return true
            Rect tileRect = new Rect(getX() + i * Block.TILE_SIZE, getY(), Block.TILE_SIZE, Block.TILE_SIZE);
            if (tileRect.intersectsRect(aRect))
                return true;
        }

        // Return no intersection
        return false;
    }

    /**
     * Adds block tiles to this row.
     */
    public void addBlockTilesToRow(Block aBlock)
    {
        for(int i = 0; i < aBlock.getTileCount(); i++) {
            Rect tileBounds = aBlock.getTileBoundsInParent(i);
            double tileX = tileBounds.getMidX() - getX();
            if(!contains(tileX, tileBounds.getMidY() - getY()))
                continue;
            int colIndex = (int) Math.floor(tileX / Block.TILE_SIZE);
            _cols[colIndex] = aBlock._pattern;
        }

        // Repaint
        repaint();
    }

    /**
     * Returns whether row is full.
     */
    public boolean isFull()  { return !ArrayUtils.hasMatch(_cols, col -> col == null); }

    /**
     * Paint block pattern.
     */
    protected void paintFront(Painter aPntr)
    {
        for (int i = 0; i < _cols.length; i++) {
            Pattern pattern = _cols[i];
            if (pattern != null)
                pattern.paintTile(aPntr, i * Block.TILE_SIZE, 0);
        }
    }
}