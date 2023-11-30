package snapdemos.tetris;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snap.util.MathUtils;
import snap.view.*;

/**
 * This class is the main game view.
 */
public class PlayView extends ParentView {
    
    // The current block
    private Block _block;
    
    // The next block
    private Block _nextBlock;
    
    // The timer
    private ViewTimer _timer = new ViewTimer(20, t -> timerFired());
    
    // The list of rows
    private List<StackRow> _rows = new ArrayList<>();
    
    // Whether user has requested block to drop faster
    private boolean _dropFast;
    
    // Whether game is over
    private boolean _gameOver;
    
    // The size of the field
    private static int TILE_SIZE = Block.TILE_SIZE;
    protected static int GRID_WIDTH = 10;
    private static int GRID_HEIGHT = 20;
    private static int BORDER_WIDTH = 2;
    
    // Constants
    static final String NextBlock_Prop = "NextBlock";

    /**
     * Constructor.
     */
    public PlayView()
    {
        super();
        setFill(Color.WHITE);
        setBorder(Color.BLACK, 2);
        enableEvents(KeyPress);
        setFocusable(true);

        // Set size
        double viewW = GRID_WIDTH * TILE_SIZE + BORDER_WIDTH * 2;
        double viewH = GRID_HEIGHT * TILE_SIZE + BORDER_WIDTH * 2;
        setPrefSize(viewW, viewH);

        // Get starting  block
        getNextBlock(true);
    }

    /**
     * Starts play.
     */
    public void startGame()
    {
        // Reset state
        _rows.clear();
        removeChildren();
        _gameOver = false;

        // Start timer, add piece
        _timer.start();
        addPiece();
        requestFocus();
        getRootView().repaint();
    }

    /**
     * Pauses game.
     */
    public void pauseGame()
    {
        if (_timer.isRunning())
            _timer.pause();
        else _timer.start();
    }

    /**
     * Adds a piece.
     */
    public void addPiece()
    {
        // Create block
        _block = getNextBlock(true);

        // Set block XY
        double blockX = (getWidth() - _block.getWidth()) / 2;
        blockX = MathUtils.round(blockX, TILE_SIZE) + BORDER_WIDTH;
        double blockY = BORDER_WIDTH;
        _block.setXY(blockX, blockY);

        // Add block
        addChild(_block);
        _dropFast = false;
    }

    /**
     * Returns the next block with option to reset.
     */
    public Block getNextBlock(boolean doReset)
    {
        Block nextBlock = _nextBlock;
        if (doReset) {
            _nextBlock = Block.getRandomBlock();
            firePropChange(NextBlock_Prop, nextBlock, _nextBlock);
        }

        // Return
        return nextBlock;
    }

    /**
     * Called when timer fires.
     */
    void timerFired()
    {
        // If no block, return
        if(_block == null) return;

        // Update block position
        int dy = 3;
        if (_dropFast)
            dy += 15;
        _block.setY(_block.getY() + dy);

        // If block stopped,
        if(intersectsBlock())
            blockDidHit();
    }

    /**
     * Returns whether block has hit something.
     */
    boolean intersectsBlock()
    {
        double blockMaxY = _block.getMaxY();

        for (int i = _rows.size() - 1; i >= 0; i--) {
            StackRow row = _rows.get(i);
            if (MathUtils.lt(blockMaxY, row.getY()))
                return false;
            if (row.intersectsBlock(_block))
                return true;
        }

        if (MathUtils.lt(blockMaxY, getHeight()))
            return false;
        return true;
    }

    /**
     * Called when block hits something.
     */
    void blockDidHit()
    {
        // Back block up
        while (intersectsBlock() && _block.getY() > BORDER_WIDTH)
            _block.setY(_block.getY() - 1);

        // Add rows to accommodate piece
        addRows();
        if (_gameOver)
            return;
        addBlockToRows();

        // Add new piece
        addPiece();
    }

    /**
     * Adds a row.
     */
    void addRows()
    {
        while (_rows.size() == 0 || _block.getY() + TILE_SIZE / 2 < getTopRow().getY()) {
            addRow();
            if (_gameOver)
                return;
        }
    }

    /**
     * Adds a row.
     */
    void addRow()
    {
        // If all rows full, it's GameOver
        if (_rows.size() >= GRID_HEIGHT - 1) {
            gameOver();
            return;
        }

        // Create new row, position above TopRow and add
        StackRow newRow = new StackRow();
        StackRow topRow = getTopRow();
        double rowY = topRow != null ? topRow.getY() : (getHeight() - BORDER_WIDTH);
        rowY -= TILE_SIZE;
        newRow.setXY(BORDER_WIDTH, rowY);
        newRow._rowNum = _rows.size();
        _rows.add(newRow); addChild(newRow);
    }

    /**
     * Removes row (with explosion) and moves rows above down.
     */
    void removeRow(StackRow aRow)
    {
        // Cache row index, explode row and remove from Rows list
        int rowIndex = _rows.indexOf(aRow);
        snapdemos.shared.Explode.explode(aRow, null, 20, 5, 0);
        _rows.remove(aRow);
        removeChild(aRow);

        // Iterate over rows above and configure to move down
        for (int i = rowIndex; i < _rows.size(); i++) {
            StackRow row = _rows.get(i);
            row.setY(getHeight() - (i + 1) * TILE_SIZE);
            row.setTransY(row.getTransY() - TILE_SIZE);
            row.getAnimCleared(500).setTransY(0).play();
        }
    }

    /**
     * Adds the current block to rows.
     */
    void addBlockToRows()
    {
        // Get block row/col counts
        int rowCount = _block._pattern.rowCount;

        // Iterate over block rows
        for (int i = 0; i < rowCount; i++) {
            double blockY = _block.getY() + i * TILE_SIZE + TILE_SIZE / 2;
            StackRow row = getRowForY(blockY);
            if (row == null)
                continue;
            row.addBlockTiles(_block);
        }

        // Remove block
        removeChild(_block);

        // Remove full rows
        for (int i = _rows.size() - 1; i >= 0; i--) {
            StackRow row = _rows.get(i);
            if (row.isFull())
                removeRow(row);
        }
    }

    /**
     * Returns the top row.
     */
    StackRow getTopRow()
    {
        return _rows.size() > 0 ? _rows.get(_rows.size() - 1) : null;
    }

    /**
     * Returns the row for y value.
     */
    StackRow getRowForY(double aY)
    {
        for (StackRow row : _rows)
            if (row.contains(row.getWidth() / 2, aY - row.getY()))
                return row;
        return null;
    }

    /**
     * Called when game is over.
     */
    void gameOver()
    {
        _gameOver = true;
        _timer.stop();
        for (int i = 0; i < _rows.size(); i++) {
            StackRow row = _rows.get(_rows.size() - i - 1);
            snapdemos.shared.Explode.explode(row, null, 20, 5, i * 150);
        }

        addBlockToRows();

        Label label = new Label("Game Over");
        label.setFont(new Font("Arial Bold", 36));
        label.setTextFill(Color.MAGENTA);
        label.setSize(label.getPrefSize());
        label.setScale(.1);
        label.setOpacity(0);
        addChild(label);
        label.setManaged(false);
        label.setLean(Pos.CENTER);
        int time = _rows.size() * 150;
        label.getAnim(time).getAnim(time + 1200).setScale(1).setOpacity(1).setRotate(360).play();
    }

    /**
     * Handles event.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle LeftArrow, RightArrow, DownArrow, Space
        if (anEvent.isLeftArrow())
            moveLeft();
        else if (anEvent.isRightArrow())
            moveRight();
        else if (anEvent.isDownArrow())
            dropBlock();
        else if (anEvent.isUpArrow() || anEvent.getKeyString().equals(" "))
            rotateBlock();
    }

    /**
     * Move Left.
     */
    public void moveLeft()
    {
        if(_block.getX() <= BORDER_WIDTH) return;

        _block.setX(_block.getX() - TILE_SIZE);

        _block.setTransX(TILE_SIZE);
        _block.getAnimCleared(300).setTransX(0).play();
    }

    /**
     * Move Right.
     */
    public void moveRight()
    {
        if(_block.getMaxX() >= getWidth() - BORDER_WIDTH) return;

        _block.setX(_block.getX() + TILE_SIZE);
        _block.setTransX(-TILE_SIZE);
        _block.getAnimCleared(300).setTransX(0).play();
    }

    /**
     * Drop block.
     */
    public void dropBlock()  { _dropFast = true; }

    /**
     * Rotate block.
     */
    public void rotateBlock()  { _block.rotateRight(); }
}